package com.codewithkael.webrtcwithmlkit.utils.helpers

import android.graphics.Bitmap
import android.graphics.Matrix
import io.github.zncmn.libyuv.YuvConvert
import io.github.zncmn.libyuv.YuvFormat
import org.webrtc.VideoFrame
import org.webrtc.VideoFrame.I420Buffer
import java.nio.ByteBuffer


class YuvFrame {
    var width: Int = 0
    var height: Int = 0
    var nv21Buffer: ByteArray? = null
    var rotationDegree: Int = 0
    var timestamp: Long = 0

    private val planeLock = Any()

    /**
     * Creates a YuvFrame from the provided VideoFrame. Does no processing, and uses the current time as a timestamp.
     * @param videoFrame Source VideoFrame.
     */
    @Suppress("unused")
    constructor(videoFrame: VideoFrame?) {
        fromVideoFrame(videoFrame, PROCESSING_NONE, System.nanoTime())
    }


    /**
     * Creates a YuvFrame from the provided VideoFrame. Does any processing indicated, and uses the current time as a timestamp.
     * @param videoFrame Source VideoFrame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     */
    @Suppress("unused")
    constructor(videoFrame: VideoFrame?, processingFlags: Int) {
        fromVideoFrame(videoFrame, processingFlags, System.nanoTime())
    }


    /**
     * Creates a YuvFrame from the provided VideoFrame. Does any processing indicated, and uses the given timestamp.
     * @param videoFrame Source VideoFrame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     * @param timestamp The timestamp to give the frame.
     */
    constructor(videoFrame: VideoFrame?, processingFlags: Int, timestamp: Long) {
        fromVideoFrame(videoFrame, processingFlags, timestamp)
    }


    /**
     * Replaces the data in this YuvFrame with the data from the provided frame. Will create new byte arrays to hold pixel data if necessary,
     * or will reuse existing arrays if they're already the correct size.
     * @param videoFrame Source VideoFrame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     * @param timestamp The timestamp to give the frame.
     */
    fun fromVideoFrame(videoFrame: VideoFrame?, processingFlags: Int, timestamp: Long) {
        if (videoFrame == null) {
            return
        }

        synchronized(planeLock) {
            try {
                // Save timestamp
                this.timestamp = timestamp

                // Copy rotation information
                rotationDegree =
                    videoFrame.rotation // Just save rotation info for now, doing actual rotation can wait until per-pixel processing.

                // Copy the pixel data, processing as requested.
                if (PROCESSING_CROP_TO_SQUARE == (processingFlags and PROCESSING_CROP_TO_SQUARE)) {
                    copyPlanesCropped(videoFrame.buffer)
                } else {
                    copyPlanes(videoFrame.buffer)
                }
            } catch (t: Throwable) {
                dispose()
            }
        }
    }


    fun dispose() {
        nv21Buffer = null
    }


    fun hasData(): Boolean {
        return nv21Buffer != null
    }


    /**
     * Copy the Y, V, and U planes from the source I420Buffer.
     * Sets width and height.
     * @param videoFrameBuffer Source frame buffer.
     */
    private fun copyPlanes(videoFrameBuffer: VideoFrame.Buffer?) {
        var i420Buffer: I420Buffer? = null

        if (videoFrameBuffer != null) {
            i420Buffer = videoFrameBuffer.toI420()
        }

        if (i420Buffer == null) {
            return
        }

        synchronized(planeLock) {
            // Set the width and height of the frame.
            width = i420Buffer.width
            height = i420Buffer.height

            // Calculate sizes needed to convert to NV21 buffer format
            val size = width * height
            val chromaStride = width
            val chromaWidth = (width + 1) / 2
            val chromaHeight = (height + 1) / 2
            val nv21Size = size + chromaStride * chromaHeight

            if (nv21Buffer == null || nv21Buffer!!.size != nv21Size) {
                nv21Buffer = ByteArray(nv21Size)
            }

            val yPlane = i420Buffer.dataY
            val uPlane = i420Buffer.dataU
            val vPlane = i420Buffer.dataV
            val yStride = i420Buffer.strideY
            val uStride = i420Buffer.strideU
            val vStride = i420Buffer.strideV

            // Populate a buffer in NV21 format because that's what the converter wants
            for (y in 0 until height) {
                for (x in 0 until width) {
                    nv21Buffer!![y * width + x] = yPlane[y * yStride + x]
                }
            }
            for (y in 0 until chromaHeight) {
                for (x in 0 until chromaWidth) {
                    // Swapping U and V values here because it makes the image the right color

                    // Store V

                    nv21Buffer!![size + y * chromaStride + 2 * x + 1] = uPlane[y * uStride + x]

                    // Store U
                    nv21Buffer!![size + y * chromaStride + 2 * x] = vPlane[y * vStride + x]
                }
            }
        }
    }


    /**
     * Copy the Y, V, and U planes from the source I420Buffer, cropping them to square.
     * Sets width and height.
     * @param videoFrameBuffer Source frame buffer.
     */
    private fun copyPlanesCropped(videoFrameBuffer: VideoFrame.Buffer?) {
        if (videoFrameBuffer == null) {
            return
        }

        synchronized(planeLock) {
            // Verify that the dimensions of the I420Frame are appropriate for cropping
            // If improper dimensions are found, default back to copying the entire frame.
            val width = videoFrameBuffer.width
            val height = videoFrameBuffer.height
            if (width > height) {
                val croppedVideoFrameBuffer = videoFrameBuffer.cropAndScale(
                    (width - height) / 2, 0, height, height, height, height
                )

                copyPlanes(croppedVideoFrameBuffer)

                croppedVideoFrameBuffer.release()
            } else {
                val croppedVideoFrameBuffer = videoFrameBuffer.cropAndScale(
                    0, (height - width) / 2, width, width, width, width
                )

                copyPlanes(croppedVideoFrameBuffer)

                croppedVideoFrameBuffer.release()
            }
        }
    }


    val bitmap: Bitmap?
        /**
         * Converts this YUV frame to an ARGB_8888 Bitmap. Applies stored rotation.
         * @return A new Bitmap containing the converted frame.
         */
        get() {
            if (nv21Buffer == null) {
                return null
            }

            // Calculate the size of the frame
            val size = width * height

            // Allocate an array to hold the ARGB pixel data
            val argbBytes = ByteArray(size * 4)

            // Use the converter (based on WebRTC source) to change to ARGB format
            yuvConverter.toARGB(nv21Buffer, argbBytes, width, height, YuvFormat.NV21)

            // Construct a Bitmap based on the new pixel data
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbBytes))

            // If necessary, generate a rotated version of the Bitmap
            if (rotationDegree == 90 || rotationDegree == -270) {
                val m: Matrix = Matrix()
                m.postRotate(90F)

                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            } else if (rotationDegree == 180 || rotationDegree == -180) {
                val m: Matrix = Matrix()
                m.postRotate(180F)

                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            } else if (rotationDegree == 270 || rotationDegree == -90) {
                val m: Matrix = Matrix()
                m.postRotate(270F)

                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            } else {
                // Don't rotate, just return the Bitmap
                return bitmap
            }
        }

    companion object {
        const val PROCESSING_NONE: Int = 0x00
        const val PROCESSING_CROP_TO_SQUARE: Int = 0x01


        // Converts from NV21 format to ARGB format
        private val yuvConverter = YuvConvert()
    }
}