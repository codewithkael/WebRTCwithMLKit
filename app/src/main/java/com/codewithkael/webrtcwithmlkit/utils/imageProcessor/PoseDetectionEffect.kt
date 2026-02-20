package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PoseDetectionEffect(
    private val drawSkeleton: Boolean = true,
    private val drawPoints: Boolean = true
) {

    private val detector: PoseDetector by lazy {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            // If you want better accuracy (slower), use:
            // .setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU) // optional (only if available)
            .build()

        PoseDetection.getClient(options)
    }

    suspend fun apply(input: Bitmap): Bitmap =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(input, 0)

            detector.process(image)
                .addOnSuccessListener { pose ->
                    // If no meaningful landmarks, just return input
                    if (pose.allPoseLandmarks.isEmpty()) {
                        cont.resume(input)
                        return@addOnSuccessListener
                    }
                    cont.resume(drawPose(input, pose))
                }
                .addOnFailureListener {
                    cont.resume(input)
                }
        }

    private fun drawPose(source: Bitmap, pose: Pose): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val stroke = maxOf(2f, source.width / 260f)
        val pointR = maxOf(3f, source.width / 180f)

        val linePaint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.STROKE
            strokeWidth = stroke
            isAntiAlias = true
        }

        val pointPaint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        if (drawSkeleton) {
            fun line(a: Int, b: Int) {
                val pa = pose.getPoseLandmark(a)?.position ?: return
                val pb = pose.getPoseLandmark(b)?.position ?: return
                canvas.drawLine(pa.x, pa.y, pb.x, pb.y, linePaint)
            }

            // Torso
            line(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
            line(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
            line(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP)
            line(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP)

            // Left arm
            line(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
            line(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)

            // Right arm
            line(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
            line(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

            // Left leg
            line(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
            line(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)

            // Right leg
            line(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
            line(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
        }

        if (drawPoints) {
            for (lm in pose.allPoseLandmarks) {
                canvas.drawCircle(lm.position.x, lm.position.y, pointR, pointPaint)
            }
        }

        return out
    }

    fun close() {
        runCatching { detector.close() }
    }
}
