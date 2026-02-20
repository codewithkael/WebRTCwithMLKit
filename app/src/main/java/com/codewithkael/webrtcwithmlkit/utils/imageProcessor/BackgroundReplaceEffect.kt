package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.graphics.scale

class BackgroundReplaceEffect {

    private val segmenter by lazy {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
            .build()
        Segmentation.getClient(options)
    }

    suspend fun apply(
        input: Bitmap,
        background: Bitmap,
        scaleMode: BackgroundScaleMode
    ): Bitmap = suspendCancellableCoroutine { cont ->

        val image = InputImage.fromBitmap(input, 0)

        segmenter.process(image)
            .addOnSuccessListener { mask ->
                val width = input.width
                val height = input.height

                val maskBuffer = mask.buffer
                maskBuffer.rewind()

                val bgPrepared = when (scaleMode) {
                    BackgroundScaleMode.CENTER_CROP -> centerCrop(background, width, height)
                    BackgroundScaleMode.STRETCH -> background.scale(width, height)
                }

                val output = createBitmap(width, height)

                val total = width * height
                val maskArray = FloatArray(total)
                val finalPixels = IntArray(total)

                maskBuffer.asFloatBuffer().get(maskArray)

                val origPix = IntArray(total)
                val bgPix = IntArray(total)

                input.getPixels(origPix, 0, width, 0, 0, width, height)
                bgPrepared.getPixels(bgPix, 0, width, 0, 0, width, height)

                for (i in 0 until total) {
                    val c = maskArray[i]
                    finalPixels[i] = if (c > 0.6f) origPix[i] else bgPix[i]
                }

                output.setPixels(finalPixels, 0, width, 0, 0, width, height)
                cont.resume(output)
            }
            .addOnFailureListener {
                cont.resume(input)
            }
    }

    private fun centerCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val result = createBitmap(targetWidth, targetHeight)
        val canvas = Canvas(result)

        val scale = maxOf(
            targetWidth.toFloat() / source.width,
            targetHeight.toFloat() / source.height
        )

        val scaledWidth = scale * source.width
        val scaledHeight = scale * source.height

        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f

        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(left, top)
        }

        canvas.drawBitmap(source, matrix, null)
        return result
    }

    fun close() = runCatching { segmenter.close() }.getOrNull()
}


enum class BackgroundScaleMode {
    CENTER_CROP,
    STRETCH
}