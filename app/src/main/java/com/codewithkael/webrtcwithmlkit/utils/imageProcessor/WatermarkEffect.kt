package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.scale

class WatermarkEffect {


    enum class Location {
        TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    data class Config(
        val watermark: Bitmap?, val location: Location, val marginPx: Float, val sizeFraction: Float
    )

    fun apply(frame: Bitmap, config: Config): Bitmap {
        val watermark = config.watermark ?: return frame

        val clampedSize = config.sizeFraction.coerceIn(0.01f, 1.0f)

        val targetW = (frame.width * clampedSize).toInt().coerceAtLeast(1)
        val targetH = (frame.height * clampedSize).toInt().coerceAtLeast(1)

        val scale = minOf(
            targetW.toFloat() / watermark.width.toFloat(),
            targetH.toFloat() / watermark.height.toFloat()
        )

        val scaledW = (watermark.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (watermark.height * scale).toInt().coerceAtLeast(1)

        val wm = if (scaledW == watermark.width && scaledH == watermark.height) watermark
        else watermark.scale(scaledW, scaledH)

        val out = frame.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val left: Float
        val top: Float

        when (config.location) {
            Location.TOP_LEFT -> {
                left = config.marginPx
                top = config.marginPx
            }

            Location.TOP_RIGHT -> {
                left = frame.width - scaledW - config.marginPx
                top = config.marginPx
            }

            Location.BOTTOM_LEFT -> {
                left = config.marginPx
                top = frame.height - scaledH - config.marginPx
            }

            Location.BOTTOM_RIGHT -> {
                left = frame.width - scaledW - config.marginPx
                top = frame.height - scaledH - config.marginPx
            }

            Location.CENTER -> {
                left = (frame.width - scaledW) / 2f
                top = (frame.height - scaledH) / 2f + config.marginPx
            }
        }

        val safeLeft = left.coerceIn(0f, (frame.width - scaledW).toFloat())
        val safeTop = top.coerceIn(0f, (frame.height - scaledH).toFloat())

        canvas.drawBitmap(wm, safeLeft, safeTop, null)
        return out
    }
}

enum class WatermarkLocation {
    TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT
}

fun WatermarkLocation.toEffectLocation(): WatermarkEffect.Location {
    return when (this) {
        WatermarkLocation.TOP_LEFT -> WatermarkEffect.Location.TOP_LEFT
        WatermarkLocation.TOP_RIGHT -> WatermarkEffect.Location.TOP_RIGHT
        WatermarkLocation.CENTER -> WatermarkEffect.Location.CENTER
        WatermarkLocation.BOTTOM_LEFT -> WatermarkEffect.Location.BOTTOM_LEFT
        WatermarkLocation.BOTTOM_RIGHT -> WatermarkEffect.Location.BOTTOM_RIGHT
    }
}

