package org.ukrida.labvora.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import org.ukrida.labvora.data.api.RetrofitInstance
import java.io.File

// ponytail: file path internal unik per foto, hindari 1 file cache dipakai semua akun
fun createProfilePhotoFile(context: Context): File {
    val dir = File(context.filesDir, "profile_photos").apply { if (!exists()) mkdirs() }
    return File(dir, "profile_${System.currentTimeMillis()}.jpg")
}

fun copyUriToProfileFile(context: Context, uri: Uri): String? = try {
    context.contentResolver.openInputStream(uri)?.use { input ->
        val out = createProfilePhotoFile(context)
        out.outputStream().use { input.copyTo(it) }
        out.absolutePath
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

// File lokal sementara yang siap diupload (hasil galeri/kamera).
// Return null bila photo sudah berupa URL/filename server atau tidak ada file-nya.
fun photoPathToUploadFile(photo: String?): File? {
    if (photo.isNullOrBlank() || photo.equals("null", ignoreCase = true)) return null
    if (photo.startsWith("http://") || photo.startsWith("https://")) return null
    if (photo.startsWith("content://") || photo.startsWith("content:")) return null
    // filename bare dari server -> bukan file lokal
    if (!photo.contains('/') && !photo.contains('\\')) return null
    return try {
        val f = File(photo)
        if (f.exists() && f.isFile) f else null
    } catch (_: Exception) {
        null
    }
}

// Resolve model untuk Coil:
// - file lokal yang masih ada -> File (preview sebelum upload / legacy 1 device)
// - filename bare server (profile_5_x.jpg) -> URL penuh uploads/ (SYNC antar device)
// - URL http(s) -> apa adanya
// - content/file Uri -> apa adanya (Coil coba, gagal -> fallback)
// - path absolut basi dari device lain -> null (fallback avatar default)
fun resolvePhotoModel(photo: String?): Any? {
    if (photo.isNullOrBlank() || photo.equals("null", ignoreCase = true)) return null
    val p = photo.trim()
    if (p.startsWith("http://") || p.startsWith("https://")) return p
    if (p.startsWith("content://") || p.startsWith("content:") || p.startsWith("file://")) return p
    if (!p.contains('/') && !p.contains('\\')) {
        if (Regex("^[A-Za-z0-9_\\-]+\\.(jpg|jpeg|png|webp)$", RegexOption.IGNORE_CASE).matches(p)) {
            return RetrofitInstance.uploadsBaseUrl + p
        }
        return null
    }
    return try {
        val f = File(p)
        if (f.exists()) f else null
    } catch (_: Exception) {
        null
    }
}

// PERF: kecilkan foto kamer/galeri SEBELUM upload (3-8MB -> ~100-300KB).
// Upload yang dulu 15-30 detik -> ~1-3 detik. Selalu aman: gagal compress
// = pakai file asli (tidak pernah bikin upload gagal). File hasil memakai
// prefix "upload_" agar bisa dibersihkan pemanggil setelah upload selesai.
fun compressImageForUpload(src: File, maxDim: Int = 1280, quality: Int = 80): File {
    try {
        if (!src.exists() || !src.isFile) return src
        if (src.length() <= 400 * 1024) return src // sudah kecil, hemat CPU
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(src.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return src
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bmp = BitmapFactory.decodeFile(src.absolutePath, opts) ?: return src
        val longest = maxOf(bmp.width, bmp.height).toFloat()
        if (longest > maxDim) {
            val scale = maxDim / longest
            val nw = (bmp.width * scale).toInt().coerceAtLeast(1)
            val nh = (bmp.height * scale).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true)
            if (scaled !== bmp) {
                bmp.recycle()
                bmp = scaled
            }
        }
        // Best-effort: betulkan orientasi foto HP via EXIF (gagal = abaikan)
        try {
            val exif = ExifInterface(src.absolutePath)
            val o = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val m = Matrix()
            when (o) {
                ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
                else -> null
            }?.let {
                val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
                if (rotated !== bmp) {
                    bmp.recycle()
                    bmp = rotated
                }
            }
        } catch (_: Exception) {
        }
        val out = File(src.parentFile, "upload_${System.currentTimeMillis()}.jpg")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        bmp.recycle()
        // Pakai hasil hanya bila valid & lebih kecil; kalau tidak, pakai asli.
        if (out.exists() && out.length() > 0 && out.length() < src.length()) return out
        out.delete()
        return src
    } catch (e: Exception) {
        e.printStackTrace()
        return src
    }
}

// Hapus file sementara hasil compress (prefix "upload_"). File asli jangan dihapus.
fun deleteCompressedTemp(file: File?, original: File?) {
    try {
        if (file != null && file != original && file.name.startsWith("upload_") && file.exists()) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ponytail: hapus file foto lokal lama agar cuma satu file tersimpan (tidak menumpuk).
// Hanya menghapus file di dalam folder profile_photos demi keamanan.
fun deleteProfilePhotoFile(path: String?) {
    if (path.isNullOrBlank()) return
    try {
        val f = File(path)
        if (f.exists() && f.parentFile?.name == "profile_photos") {
            f.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
