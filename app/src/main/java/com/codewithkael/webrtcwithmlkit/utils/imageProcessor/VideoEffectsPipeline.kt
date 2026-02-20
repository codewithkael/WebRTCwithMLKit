package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.content.Context
import android.graphics.Bitmap

class VideoEffectsPipeline(context: Context) {

    private val textRecognition = TextRecognitionEffect()
    private val watermark = WatermarkEffect()
    private val faceOval = FaceOvalEffect()
    private val faceMesh = FaceMeshEffect()
    private val blur = BackgroundBlurEffect(context)
    private val imageLabeling = ImageLabelingEffect()
    private val objectDetection = ObjectDetectionEffect()
    private val poseDetection = PoseDetectionEffect()
    private val bgReplace = BackgroundReplaceEffect()

    data class Enabled(
        val textRecognition: Boolean,
        val watermark: Boolean,
        val faceDetect: Boolean,
        val faceMesh: Boolean,
        val blurBackground: Boolean,
        val imageLabeling: Boolean,
        val objectDetection: Boolean,
        val poseDetection: Boolean,
        val replaceBackground: Boolean,
    )

    data class WatermarkParams(
        val bitmap: Bitmap?,
        val location: WatermarkEffect.Location,
        val marginPx: Float,
        val sizeFraction: Float
    )

    data class BackgroundParams(
        val bitmap: Bitmap?,
        val scaleMode: BackgroundScaleMode
    )

    suspend fun process(
        input: Bitmap,
        enabled: Enabled,
        wm: WatermarkParams,
        bg: BackgroundParams
    ): Bitmap {
        var out = input
        if (enabled.textRecognition) out = textRecognition.apply(out)
        if (enabled.watermark) {
            out = watermark.apply(
                out,
                WatermarkEffect.Config(
                    watermark = wm.bitmap,
                    location = wm.location,
                    marginPx = wm.marginPx,
                    sizeFraction = wm.sizeFraction
                )
            )
        }
        if (enabled.faceDetect) out = faceOval.apply(out)
        if (enabled.faceMesh) out = faceMesh.apply(out)
        if (enabled.blurBackground) out = blur.apply(out)
        if (enabled.imageLabeling) out = imageLabeling.apply(out)
        if (enabled.objectDetection) out = objectDetection.apply(out)
        if (enabled.poseDetection) out = poseDetection.apply(out)

        if (enabled.replaceBackground && bg.bitmap != null) {
            out = bgReplace.apply(out, bg.bitmap, bg.scaleMode)
        } else if (enabled.blurBackground) {
            out = blur.apply(out)
        }

        return out
    }

    fun close() {
        textRecognition.close()
        faceOval.close()
        faceMesh.close()
        blur.close()
        imageLabeling.close()
        objectDetection.close()
        poseDetection.close()
        bgReplace.close()
    }
}
