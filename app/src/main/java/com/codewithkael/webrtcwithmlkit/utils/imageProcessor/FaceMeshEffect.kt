package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FaceMeshEffect {

    private val detector: FaceMeshDetector by lazy { FaceMeshDetection.getClient() }

    suspend fun apply(input: Bitmap): Bitmap = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(input, 0)

        detector.process(image)
            .addOnSuccessListener { meshes: List<FaceMesh> ->
                if (meshes.isEmpty()) {
                    cont.resume(input); return@addOnSuccessListener
                }

                val out = input.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(out)

                val paint = Paint().apply {
                    color = Color.GREEN
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }

                val r = maxOf(1f, input.width / 360f)

                for (mesh in meshes) {
                    for (p: FaceMeshPoint in mesh.allPoints) {
                        canvas.drawCircle(p.position.x, p.position.y, r, paint)
                    }
                }

                cont.resume(out)
            }
            .addOnFailureListener { cont.resume(input) }
    }

    fun close() = runCatching { detector.close() }.getOrNull()
}
