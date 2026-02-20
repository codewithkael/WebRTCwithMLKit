package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.hoko.blur.HokoBlur
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BackgroundBlurEffect(private val context: Context) {

    private val segmenter by lazy {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
            .build()
        Segmentation.getClient(options)
    }

    suspend fun apply(input: Bitmap): Bitmap = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(input, 0)

        segmenter.process(image)
            .addOnSuccessListener { mask ->
                val width = input.width
                val height = input.height

                val maskBuffer = mask.buffer
                maskBuffer.rewind()

                val blurred =
                    HokoBlur.with(context)
                        .scheme(HokoBlur.SCHEME_NATIVE)
                        .mode(HokoBlur.MODE_BOX)
                        .radius(width / 32)
                        .sampleFactor(2f)
                        .forceCopy(true)
                        .blur(input)

                val output = createBitmap(width, height)

                val total = width * height
                val maskArray = FloatArray(total)
                val finalPixels = IntArray(total)

                maskBuffer.asFloatBuffer().get(maskArray)

                val origPix = IntArray(total)
                val blurPix = IntArray(total)

                input.getPixels(origPix, 0, width, 0, 0, width, height)
                blurred.getPixels(blurPix, 0, width, 0, 0, width, height)

                for (i in 0 until total) {
                    val c = maskArray[i]
                    finalPixels[i] = if (c > 0.6f) origPix[i] else blurPix[i]
                }

                output.setPixels(finalPixels, 0, width, 0, 0, width, height)
                cont.resume(output)
            }
            .addOnFailureListener { cont.resume(input) }
    }

    fun close() = runCatching { segmenter.close() }.getOrNull()
}
