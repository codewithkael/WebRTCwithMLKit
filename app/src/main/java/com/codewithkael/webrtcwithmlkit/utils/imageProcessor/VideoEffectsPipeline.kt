package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap

class VideoEffectsPipeline {

    private val textRecognition = TextRecognitionEffect()
    private val watermark = WatermarkEffect()


    data class Enabled(
        val textRecognition: Boolean,
        val watermark: Boolean,
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
        return out
    }

    fun close() {
        textRecognition.close()
    }
}
