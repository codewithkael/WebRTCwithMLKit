package com.codewithkael.webrtcwithmlkit.utils.helpers

import android.graphics.Bitmap
import org.webrtc.JavaI420Buffer
import org.webrtc.VideoFrame
import java.nio.ByteBuffer
class BitmapToVideoFrameConverter {

    companion object {
        fun convert(bitmap: Bitmap, rotation: Int, timestamp: Long): VideoFrame {
            val i420Buffer = convertBitmapToI420Buffer(bitmap)
            return VideoFrame(i420Buffer, rotation, timestamp)
        }

        private fun convertBitmapToI420Buffer(bitmap: Bitmap): JavaI420Buffer {
            val width = bitmap.width
            val height = bitmap.height

            val argbBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(argbBuffer)
            argbBuffer.rewind()

            val ySize = width * height
            val uvSize = width * height / 4
            val yBuffer = ByteBuffer.allocateDirect(ySize)
            val uBuffer = ByteBuffer.allocateDirect(uvSize)
            val vBuffer = ByteBuffer.allocateDirect(uvSize)

            // Convert ARGB to I420 manually
            val argb = IntArray(width * height)
            bitmap.getPixels(argb, 0, width, 0, 0, width, height)
            val y = ByteArray(ySize)
            val u = ByteArray(uvSize)
            val v = ByteArray(uvSize)

            var yIndex = 0
            var uvIndex = 0
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val color = argb[i * width + j]
                    val r = (color shr 16) and 0xff
                    val g = (color shr 8) and 0xff
                    val b = color and 0xff

                    // BT.601 conversion
                    val yValue = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    val uValue = (-0.169 * r - 0.331 * g + 0.5 * b + 128).toInt()
                    val vValue = (0.5 * r - 0.419 * g - 0.081 * b + 128).toInt()

                    y[yIndex++] = yValue.toByte()
                    if (i % 2 == 0 && j % 2 == 0) {
                        u[uvIndex] = uValue.toByte()
                        v[uvIndex] = vValue.toByte()
                        uvIndex++
                    }
                }
            }

            yBuffer.put(y)
            uBuffer.put(u)
            vBuffer.put(v)
            yBuffer.rewind()
            uBuffer.rewind()
            vBuffer.rewind()

            return JavaI420Buffer.wrap(width, height, yBuffer, width, uBuffer, width / 2, vBuffer, width / 2, null)
        }
    }
}