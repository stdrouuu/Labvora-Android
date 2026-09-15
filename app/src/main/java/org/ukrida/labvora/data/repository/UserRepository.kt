// Repository untuk mengelola sumber data User, menghubungkan ViewModel dengan ApiService
package org.ukrida.labvora.data.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.ukrida.labvora.data.api.ApiService
import org.ukrida.labvora.data.model.UploadPhotoResponse
import org.ukrida.labvora.data.model.User
import java.io.File

class UserRepository(private val api: ApiService) {
    suspend fun getUsers() = api.getUsers()

    suspend fun insert(user: User) = api.insertUser(user)

    suspend fun update(user: User) = api.updateUser(user)

    suspend fun delete(id: Int) = api.deleteUser(mapOf("id" to id))

    suspend fun login(username: String, password: String): User {
        return api.login(request = mapOf(
            "username" to username,
            "password" to password
        ))
    }

    // Upload file foto lokal ke server -> { success, filename, url }.
    // DB hanya menyimpan filename, bukan path lokal.
    suspend fun uploadProfilePhoto(file: File, userId: Int? = null): UploadPhotoResponse {
        val mime = when (file.extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val part = MultipartBody.Part.createFormData(
            "photo", file.name, file.asRequestBody(mime.toMediaType())
        )
        val idBody = userId?.takeIf { it > 0 }
            ?.toString()?.toRequestBody("text/plain".toMediaType())
        return api.uploadProfilePhoto(part, idBody)
    }
}
