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

    data class Enabled(
        val textRecognition: Boolean,
        val watermark: Boolean,
        val faceDetect: Boolean,
        val faceMesh: Boolean,
        val blurBackground: Boolean,
        val imageLabeling: Boolean,

    )

    data class WatermarkParams(
        val bitmap: Bitmap?,
        val location: WatermarkEffect.Location,
        val marginPx: Float,
        val sizeFraction: Float
    )


    suspend fun process(
        input: Bitmap,
        enabled: Enabled,
        wm: WatermarkParams
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

        return out
    }

    fun close() {
        textRecognition.close()
        faceOval.close()
        faceMesh.close()
        blur.close()
        imageLabeling.close()

    }
}
