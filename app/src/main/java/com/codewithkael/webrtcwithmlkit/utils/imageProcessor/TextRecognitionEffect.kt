package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TextRecognitionEffect {

    private val recognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun apply(input: Bitmap): Bitmap =
        suspendCancellableCoroutine { cont ->

            val image = InputImage.fromBitmap(input, 0)

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    if (result.textBlocks.isEmpty()) {
                        cont.resume(input)
                        return@addOnSuccessListener
                    }
                    cont.resume(drawTextBlocks(input, result.textBlocks))
                }
                .addOnFailureListener {
                    cont.resume(input)
                }
        }

    private fun drawTextBlocks(
        source: Bitmap,
        blocks: List<Text.TextBlock>
    ): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val boxPaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.STROKE
            strokeWidth = maxOf(2f, source.width / 300f)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = maxOf(18f, source.width / 28f)
            isAntiAlias = true
        }

        val bgPaint = Paint().apply {
            color = Color.argb(160, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        for (b in blocks) {
            val box = b.boundingBox ?: continue
            canvas.drawRect(box, boxPaint)

            val text = b.text.trim()
            if (text.isBlank()) continue

            val bounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)

            val padding = 8f
            val left = box.left.toFloat().coerceAtLeast(0f)
            val top = (box.top.toFloat() - bounds.height() - padding * 2f).coerceAtLeast(0f)

            canvas.drawRoundRect(
                left,
                top,
                left + bounds.width() + padding * 2f,
                top + bounds.height() + padding * 2f,
                10f,
                10f,
                bgPaint
            )

            canvas.drawText(
                text,
                left + padding,
                top + bounds.height() + padding,
                textPaint
            )
        }

        return out
    }

    fun close() {
        runCatching { recognizer.close() }
    }
}
