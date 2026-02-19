package com.codewithkael.webrtcwithmlkit.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithkael.webrtcwithmlkit.remote.firebase.FirebaseClient
import com.codewithkael.webrtcwithmlkit.remote.firebase.SignalDataModel
import com.codewithkael.webrtcwithmlkit.remote.firebase.SignalDataModelTypes
import com.codewithkael.webrtcwithmlkit.utils.MyApplication
import com.codewithkael.webrtcwithmlkit.utils.MyApplication.Companion.TAG
import com.codewithkael.webrtcwithmlkit.utils.webrt.MyPeerObserver
import com.codewithkael.webrtcwithmlkit.utils.webrt.RTCAudioManager
import com.codewithkael.webrtcwithmlkit.utils.webrt.RTCClient
import com.codewithkael.webrtcwithmlkit.utils.webrt.RTCClientImpl
import com.codewithkael.webrtcwithmlkit.utils.webrt.WebRTCFactory
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val application: Application,
    private val webRTCFactory: WebRTCFactory,
    private val gson: Gson
) : ViewModel() {

    private val rtcAudioManager by lazy { RTCAudioManager.create(application) }
    private var rtcClient: RTCClient? = null
    private val userID: String = MyApplication.UserID
    private var remoteSurface: SurfaceViewRenderer? = null
    private var participantId: String = ""
    var callState: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private set

    init {
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
    }

    fun permissionsGranted() {
        firebaseClient.observeIncomingSignals { signalDataModel ->
            when (signalDataModel.type) {
                SignalDataModelTypes.INCOMING_CALL -> handleIncomingCall(signalDataModel)
                SignalDataModelTypes.ACCEPT_CALL -> handleAcceptCall()
                SignalDataModelTypes.OFFER -> handleReceivedOfferSdp(signalDataModel)
                SignalDataModelTypes.ANSWER -> handleReceivedAnswerSdp(signalDataModel)
                SignalDataModelTypes.ICE -> handleReceivedIceCandidate(signalDataModel)
                null -> Unit
            }
        }
    }

    private fun handleAcceptCall() {
        setupRtcConnection(participantId)?.also {
            it.offer()
        }
    }

    fun sendStartCallSignal(participantId: String) {
        this.participantId = participantId
        viewModelScope.launch {
            callState.emit(true)
        }
        viewModelScope.launch {
            firebaseClient.updateParticipantDataModel(
                participantId = participantId, data = SignalDataModel(
                    type = SignalDataModelTypes.INCOMING_CALL, participantId = userID
                )
            )
        }
    }

    private fun handleIncomingCall(dataModel: SignalDataModel) {

        this.participantId = dataModel.participantId
        viewModelScope.launch {
            callState.emit(true)
        }
        viewModelScope.launch {
            firebaseClient.updateParticipantDataModel(
                participantId = participantId, data = SignalDataModel(
                    type = SignalDataModelTypes.ACCEPT_CALL, participantId = userID
                )
            )
        }
    }

    private fun handleReceivedIceCandidate(signalDataModel: SignalDataModel) {
        runCatching {
            gson.fromJson(
                signalDataModel.data.toString(), IceCandidate::class.java
            )
        }.onSuccess {
            rtcClient?.onIceCandidateReceived(it)
        }.onFailure {
            Log.d(TAG, "handleReceivedIceCandidate: ${it.message}")
        }
    }

    private fun handleReceivedAnswerSdp(signalDataModel: SignalDataModel) {
        rtcClient?.onRemoteSessionReceived(
            SessionDescription(
                SessionDescription.Type.ANSWER, signalDataModel.data.toString()
            )
        )
    }

    private fun handleReceivedOfferSdp(signalDataModel: SignalDataModel) {
        viewModelScope.launch {
            callState.emit(true)
        }
        setupRtcConnection(participantId)?.also {
            it.onRemoteSessionReceived(
                SessionDescription(
                    SessionDescription.Type.OFFER, signalDataModel.data.toString()
                )
            )
            it.answer()

        }
    }

    private fun setupRtcConnection(participant: String): RTCClient? {
        runCatching { rtcClient?.onDestroy() }
        rtcClient = null
        rtcClient = webRTCFactory.createRTCClient(observer = object : MyPeerObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    rtcClient?.onLocalIceCandidateGenerated(it)
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.let {
                    runCatching {
                        Log.d(TAG, "onAddStream: $it")
                        remoteSurface?.let { surface ->
                            it.videoTracks[0]?.addSink(surface)
                        } ?: run {
                            Log.d(TAG, "onAddStream: nulle surface")
                        }
                    }
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    viewModelScope.launch {
                        firebaseClient.removeSelfData()
                    }
                }
            }
        }, listener = object : RTCClientImpl.TransferDataToServerCallback {
            override fun onIceGenerated(iceCandidate: IceCandidate) {
                viewModelScope.launch {
                    firebaseClient.updateParticipantDataModel(
                        participantId = participant, data = SignalDataModel(
                            type = SignalDataModelTypes.ICE,
                            data = gson.toJson(iceCandidate),
                            participantId = userID
                        )
                    )
                }
            }

            override fun onOfferGenerated(sessionDescription: SessionDescription) {
                viewModelScope.launch {
                    firebaseClient.updateParticipantDataModel(
                        participantId = participant, data = SignalDataModel(
                            type = SignalDataModelTypes.OFFER,
                            data = sessionDescription.description,
                            participantId = userID
                        )
                    )
                }
            }

            override fun onAnswerGenerated(sessionDescription: SessionDescription) {
                viewModelScope.launch {
                    firebaseClient.updateParticipantDataModel(
                        participantId = participant, data = SignalDataModel(
                            type = SignalDataModelTypes.ANSWER,
                            data = sessionDescription.description,
                            participantId = userID
                        )
                    )
                }
            }

        })
        return rtcClient
    }

    fun startLocalStream(surface: SurfaceViewRenderer) {
        webRTCFactory.prepareLocalStream(surface)
    }

    fun initRemoteSurfaceView(remoteSurface: SurfaceViewRenderer) {
        this.remoteSurface = remoteSurface
        webRTCFactory.initSurfaceView(remoteSurface)
    }

    fun switchCamera() = webRTCFactory.switchCamera()

    fun reloadFilters() {
        webRTCFactory.reloadFiltersConfig()
    }

    fun reloadWatermark() {
        webRTCFactory.reloadWatermarkConfig()
    }

    override fun onCleared() {
        super.onCleared()
        remoteSurface?.release()
        remoteSurface = null
        firebaseClient.clear()
        webRTCFactory.onDestroy()
        rtcClient?.onDestroy()
    }
}