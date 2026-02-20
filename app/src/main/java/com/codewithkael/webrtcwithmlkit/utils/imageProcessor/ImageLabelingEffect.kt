package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ImageLabelingEffect {

    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.65f)
                .build()
        )
    }

    /**
     * Runs ML Kit labeling and draws the top labels on the frame.
     */
    suspend fun apply(bitmap: Bitmap, maxLabels: Int = 5): Bitmap =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    if (labels.isEmpty()) {
                        cont.resume(bitmap); return@addOnSuccessListener
                    }

                    val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(out)

                    val textPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = (out.width / 22f).coerceAtLeast(28f)
                        isAntiAlias = true
                    }

                    val bgPaint = Paint().apply {
                        color = Color.argb(140, 0, 0, 0)
                        isAntiAlias = true
                    }

                    val top = labels
                        .sortedByDescending { it.confidence }
                        .take(maxLabels)

                    val lines = top.map {
                        "${it.text} ${(it.confidence * 100).toInt()}%"
                    }

                    // simple top-left overlay
                    val padding = (out.width / 60f).coerceAtLeast(16f)
                    val lineH = textPaint.textSize * 1.25f
                    val boxH = padding * 2 + lineH * lines.size
                    val boxW = lines.maxOf { textPaint.measureText(it) } + padding * 2

                    canvas.drawRoundRect(
                        /* left = */ padding,
                        /* top = */ padding,
                        /* right = */ padding + boxW,
                        /* bottom = */ padding + boxH,
                        /* rx = */ 18f,
                        /* ry = */ 18f,
                        bgPaint
                    )

                    var y = padding + padding + textPaint.textSize
                    for (line in lines) {
                        canvas.drawText(line, padding + padding, y, textPaint)
                        y += lineH
                    }

                    cont.resume(out)
                }
                .addOnFailureListener {
                    cont.resume(bitmap)
                }
        }

    fun close() {
        runCatching { labeler.close() }
    }
}
