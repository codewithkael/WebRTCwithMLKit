package com.codewithkael.webrtcwithmlkit.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

suspend fun ContentResolver.loadAndDownscaleToUnderBytes(
    uri: Uri,
    maxBytes: Int,
    initialMaxDim: Int = 1280
): Bitmap? = withContext(Dispatchers.IO) {

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

    val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, initialMaxDim, initialMaxDim)

    val decodeOpts = BitmapFactory.Options().apply {
        inJustDecodeBounds = false
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    val original = openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
        ?: return@withContext null

    // Fast path
    compressToByteArray(original, preferredFormat(), quality = 95)?.let { bytes ->
        if (bytes.size <= maxBytes) {
            original.recycle()
            return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    var working = original
    var quality = 92
    var bestBytes: ByteArray? = null

    // 1) Try lowering quality first
    while (quality >= 35) {
        val bytes = compressToByteArray(working, preferredFormat(), quality) ?: break
        bestBytes = bytes
        if (bytes.size <= maxBytes) {
            original.recycle()
            return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        quality -= 7
    }

    // 2) If still too big, downscale dimensions
    quality = 85
    var scaleFactor = 0.9f

    while (true) {
        val newW = (working.width * scaleFactor).toInt().coerceAtLeast(320)
        val newH = (working.height * scaleFactor).toInt().coerceAtLeast(320)

        if (newW == working.width && newH == working.height) break

        val resized = working.scale(newW, newH)
        if (resized != working && working != original) working.recycle()
        working = resized

        var q = quality
        while (q >= 35) {
            val bytes = compressToByteArray(working, preferredFormat(), q) ?: break
            bestBytes = bytes
            if (bytes.size <= maxBytes) {
                if (working != original) working.recycle()
                original.recycle()
                return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            q -= 7
        }

        if (max(working.width, working.height) <= 360) break
        scaleFactor *= 0.9f
    }

    // Best-effort fallback (might still be > maxBytes in extreme cases)
    bestBytes?.let { bytes ->
        if (working != original) working.recycle()
        original.recycle()
        return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    // If no bytes produced, return current bitmap (already downscaled as much as we tried)
    // Note: this is a Bitmap in RAM, not a <=maxBytes encoded file.
    return@withContext working
}

private fun calculateInSampleSize(
    srcW: Int,
    srcH: Int,
    reqW: Int,
    reqH: Int
): Int {
    var inSampleSize = 1
    if (srcH > reqH || srcW > reqW) {
        val halfH = srcH / 2
        val halfW = srcW / 2
        while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
            inSampleSize *= 2
        }
    }
    return inSampleSize.coerceAtLeast(1)
}

private fun preferredFormat(): Bitmap.CompressFormat {
    return if (android.os.Build.VERSION.SDK_INT >= 30) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        Bitmap.CompressFormat.JPEG
    }
}

private fun compressToByteArray(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    quality: Int
): ByteArray? {
    val baos = ByteArrayOutputStream()
    val ok = bitmap.compress(format, quality.coerceIn(0, 100), baos)
    return if (ok) baos.toByteArray() else null
}