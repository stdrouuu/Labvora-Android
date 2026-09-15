package org.ukrida.labvora.util

import android.content.Context
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
