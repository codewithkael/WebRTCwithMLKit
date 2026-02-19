package com.codewithkael.webrtcwithmlkit.utils.webrt

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.codewithkael.webrtcwithmlkit.R
import com.codewithkael.webrtcwithmlkit.utils.MyApplication
import com.codewithkael.webrtcwithmlkit.utils.helpers.BitmapToVideoFrameConverter
import com.codewithkael.webrtcwithmlkit.utils.helpers.YuvFrame
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.VideoEffectsPipeline
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.WatermarkLocation
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.toEffectLocation
import com.codewithkael.webrtcwithmlkit.utils.persistence.FilterStorage
import com.codewithkael.webrtcwithmlkit.utils.persistence.WatermarkStorage
import com.codewithkael.webrtcwithmlkit.utils.webrt.IceServers.Companion.getIceServers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoFrame
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCFactory @Inject constructor(
    private val application: Application
) {

    // ===== WebRTC core =====
    private val eglBaseContext = EglBase.create().eglBaseContext
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }

    private var videoCapture: CameraVideoCapturer? = null
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private val streamId = "${MyApplication.UserID}_stream"
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private val iceServer = getIceServers()

    // ===== Effects pipeline =====
    private val effectsPipeline = VideoEffectsPipeline()

    // ===== Watermark config (reloaded from prefs) =====
    private var watermarkBitmap: Bitmap? = null
    private var watermarkLocation: WatermarkLocation = WatermarkLocation.TOP_LEFT
    private var watermarkMarginDp: Float = 12f
    private var watermarkSizeFraction: Float = 0.20f

    // ===== Filters config (reloaded from prefs) =====
    @Volatile private var filterTextRecognition: Boolean = false
    @Volatile private var filterWatermark: Boolean = false
    @Volatile private var filterFaceDetect: Boolean = false
    @Volatile private var filterFaceMesh: Boolean = false

    init {
        initPeerConnectionFactory(application)
        reloadWatermarkConfig()
        reloadFiltersConfig()
    }

    //image processing section
    fun reloadFiltersConfig() {
        val cfg = FilterStorage.load(application)
        filterTextRecognition = cfg.textRecognition
        filterWatermark = cfg.watermark
        filterFaceDetect = cfg.faceDetect
        filterFaceMesh = cfg.faceMesh
    }

    fun reloadWatermarkConfig() {
        val cfg = WatermarkStorage.load(application)

        watermarkLocation = cfg.location
        watermarkMarginDp = cfg.marginDp
        watermarkSizeFraction = cfg.sizeFraction

        watermarkBitmap = loadBitmapFromUriOrDefault(cfg.uri)
    }

    // Public API
    fun prepareLocalStream(view: SurfaceViewRenderer) {
        initSurfaceView(view)
        startLocalVideo(view)
    }

    fun initSurfaceView(view: SurfaceViewRenderer) {
        view.run {
            setMirror(false)
            setEnableHardwareScaler(true)
            init(eglBaseContext, null)
        }
    }

    fun createRTCClient(
        observer: PeerConnection.Observer, listener: RTCClientImpl.TransferDataToServerCallback
    ): RTCClient? {
        val connection = peerConnectionFactory.createPeerConnection(
            PeerConnection.RTCConfiguration(iceServer), observer
        )
        localVideoTrack?.let { connection?.addTrack(it) }
        localAudioTrack?.let { connection?.addTrack(it) }
        return connection?.let { RTCClientImpl(it, listener) }
    }

    fun onDestroy() {
        runCatching { videoCapture?.stopCapture() }
        runCatching { videoCapture?.dispose() }
        videoCapture = null

        localAudioTrack?.let {
            it.setEnabled(false)
            it.dispose()
        }
        localAudioTrack = null

        localVideoTrack?.dispose()
        localVideoTrack = null
        effectsPipeline.close()
    }

    private fun startLocalVideo(surface: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglBaseContext)

        videoCapture = getVideoCapture()

        videoCapture?.initialize(
            surfaceTextureHelper, surface.context, object : CapturerObserver {
                override fun onCapturerStarted(success: Boolean) {}
                override fun onCapturerStopped() {}

                override fun onFrameCaptured(frame: VideoFrame) {
                    val yuv = YuvFrame(frame, YuvFrame.PROCESSING_NONE, frame.timestampNs)
                    val bitmap = yuv.bitmap ?: return

                    CoroutineScope(Dispatchers.Default).launch {
                        val processed = runEffects(bitmap)
                        val videoFrame = BitmapToVideoFrameConverter.convert(
                            processed, 0, System.nanoTime()
                        )
                        withContext(Dispatchers.Main) {
                            localVideoSource.capturerObserver.onFrameCaptured(videoFrame)
                        }
                    }
                }
            })

        videoCapture?.startCapture(720, 480, 10)

        localVideoTrack =
            peerConnectionFactory.createVideoTrack("${streamId}_video", localVideoSource)
        localVideoTrack?.addSink(surface)

        localAudioTrack =
            peerConnectionFactory.createAudioTrack("${streamId}_audio", localAudioSource)
    }

    private suspend fun runEffects(input: Bitmap): Bitmap {

        val density = application.resources.displayMetrics.density
        val marginPx = watermarkMarginDp * density

        return effectsPipeline.process(
            input = input, enabled = VideoEffectsPipeline.Enabled(
                textRecognition = filterTextRecognition,
                watermark = filterWatermark,
                faceDetect = filterFaceDetect,
                faceMesh = filterFaceMesh
            ),
            wm = VideoEffectsPipeline.WatermarkParams(
                bitmap = watermarkBitmap,
                location = watermarkLocation.toEffectLocation(),
                marginPx = marginPx,
                sizeFraction = watermarkSizeFraction
            )
        )
    }

    fun switchCamera() {
        videoCapture?.switchCamera(null)
    }

    private fun getVideoCapture(): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(application)
        val deviceName = enumerator.deviceNames.firstOrNull { name ->
            enumerator.isFrontFacing(name)
        } ?: throw IllegalStateException("No camera found for front")

        return enumerator.createCapturer(deviceName, null)
            ?: throw IllegalStateException("Failed to create capturer for $deviceName")
    }


    // PeerConnectionFactory
    private fun initPeerConnectionFactory(application: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setOptions(
                PeerConnectionFactory.Options().apply {
                    disableEncryption = false
                    disableNetworkMonitor = false
                }).createPeerConnectionFactory()
    }

    private fun loadBitmapFromUriOrDefault(uriStr: String?): Bitmap {
        if (uriStr.isNullOrBlank()) {
            return BitmapFactory.decodeResource(application.resources, R.drawable.youtube_logo)
        }

        val uri = uriStr.toUri()
        val bmp = runCatching {
            application.contentResolver.openInputStream(uri).use { input ->
                if (input != null) BitmapFactory.decodeStream(input) else null
            }
        }.getOrNull()

        return bmp ?: BitmapFactory.decodeResource(application.resources, R.drawable.youtube_logo)
    }

}
