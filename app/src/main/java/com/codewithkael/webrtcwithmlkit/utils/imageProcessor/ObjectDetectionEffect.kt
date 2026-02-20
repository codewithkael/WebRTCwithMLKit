package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ObjectDetectionEffect {

    private val detector: ObjectDetector by lazy {
        // STREAM_MODE is best for video frames :contentReference[oaicite:2]{index=2}
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        ObjectDetection.getClient(options) // :contentReference[oaicite:3]{index=3}
    }

    suspend fun apply(input: Bitmap): Bitmap =
        suspendCancellableCoroutine { cont ->

            val image = InputImage.fromBitmap(input, 0)

            detector.process(image)
                .addOnSuccessListener { objects ->
                    if (objects.isEmpty()) {
                        cont.resume(input)
                        return@addOnSuccessListener
                    }
                    cont.resume(drawObjects(input, objects))
                }
                .addOnFailureListener {
                    cont.resume(input)
                }
        }

    private fun drawObjects(source: Bitmap, objects: List<DetectedObject>): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val boxPaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = maxOf(2f, source.width / 250f)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
            textSize = maxOf(18f, source.width / 25f)
            isAntiAlias = true
        }

        val bgPaint = Paint().apply {
            color = Color.argb(140, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        for (obj in objects) {
            val box = obj.boundingBox
            canvas.drawRect(box, boxPaint)

            // Best-effort label
            val label = obj.labels.firstOrNull()
            val text = when {
                label != null -> "${label.text} ${(label.confidence * 100).toInt()}%"
                else -> "Object ${obj.trackingId ?: ""}".trim()
            }

            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, textBounds)

            val padding = 8f
            val left = box.left.toFloat()
            val top = (box.top.toFloat() - textBounds.height() - padding * 2f).coerceAtLeast(0f)

            canvas.drawRoundRect(
                left,
                top,
                left + textBounds.width() + padding * 2f,
                top + textBounds.height() + padding * 2f,
                10f,
                10f,
                bgPaint
            )
            canvas.drawText(text, left + padding, top + textBounds.height() + padding, textPaint)
        }

        return out
    }

    fun close() {
        runCatching { detector.close() }
    }
}
