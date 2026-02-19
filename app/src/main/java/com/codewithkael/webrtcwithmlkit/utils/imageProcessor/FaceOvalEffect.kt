package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FaceOvalEffect {

    private val detector: FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()
        )
    }

    suspend fun apply(input: Bitmap): Bitmap = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(input, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    cont.resume(input); return@addOnSuccessListener
                }

                val out = input.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(out)

                val paint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    isAntiAlias = true
                }

                for (face in faces) {
                    val box = face.boundingBox
                    canvas.drawOval(
                        RectF(
                            box.left.toFloat(),
                            box.top.toFloat(),
                            box.right.toFloat(),
                            box.bottom.toFloat()
                        ),
                        paint
                    )
                }

                cont.resume(out)
            }
            .addOnFailureListener { cont.resume(input) }
    }

    fun close() = runCatching { detector.close() }.getOrNull()
}
