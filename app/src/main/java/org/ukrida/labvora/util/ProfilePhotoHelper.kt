package org.ukrida.labvora.util

import android.content.Context
import android.net.Uri
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

// ponytail: path file lokal > string mentah; Coil reload saat string beda per akun
fun resolvePhotoModel(photo: String?): Any? {
    if (photo.isNullOrBlank()) return null
    val f = File(photo)
    if (f.exists()) return f
    return photo
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
