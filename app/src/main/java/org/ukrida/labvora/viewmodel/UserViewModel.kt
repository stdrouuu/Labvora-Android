// ViewModel untuk mengelola logika bisnis dan state terkait data User (Login, Register, CRUD User)
package org.ukrida.labvora.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.User
import org.ukrida.labvora.data.repository.UserRepository
import org.ukrida.labvora.util.compressImageForUpload
import org.ukrida.labvora.util.deleteCompressedTemp
import java.io.File

class UserViewModel(private val repo: UserRepository) : ViewModel() {

    var users = mutableStateOf<List<User>>(emptyList())
    var currentUser = mutableStateOf<User?>(null)

    // State untuk notifikasi toast berhasil registrasi
    var showRegisterSuccessToast = mutableStateOf(false)

    // State untuk toast "profil diperbarui" — hoisted agar survive popBackStack ke Profile
    var showProfileUpdatedToast = mutableStateOf(false)

    // State untuk proses hapus akun: null = idle, true = loading, false = selesai
    var isDeletingAccount = mutableStateOf(false)
    var deleteAccountError = mutableStateOf<String?>(null)

    // Ringan: hanya true saat profil benar-benar sedang dimuat/di-refresh.
    // UI menampilkan spinner HANYA bila flag ini true atau currentUser null.
    var isLoadingProfile = mutableStateOf(false)
    var profileError = mutableStateOf<String?>(null)

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            isDeletingAccount.value = true
            deleteAccountError.value = null
            try {
                val response = repo.delete(userId)
                if (!response.isSuccessful) {
                    throw Exception("Server menolak hapus akun (${response.code()})")
                }
                // Hapus dari daftar lokal — hanya jika server sukses
                users.value = users.value.filter { it.id != userId }
                // Bersihkan sesi pengguna
                org.ukrida.labvora.data.api.RetrofitInstance.authToken = null
                currentUser.value = null
                isDeletingAccount.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                isDeletingAccount.value = false
                val errMsg = "Gagal menghapus akun. Periksa koneksi internet Anda."
                deleteAccountError.value = errMsg
                onError(errMsg)
            }
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            try {
                users.value = repo.getUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var loginError = mutableStateOf<String?>(null)
    var isLoggingIn = mutableStateOf(false)

    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoggingIn.value = true
            isLoadingProfile.value = true
            loginError.value = null
            try {
                val loggedInUser = repo.login(username, password)
                org.ukrida.labvora.data.api.RetrofitInstance.authToken = loggedInUser.token
                currentUser.value = loggedInUser
            } catch (e: retrofit2.HttpException) {
                e.printStackTrace()
                if (e.code() == 401) {
                    loginError.value = "Username atau password salah!"
                } else {
                    loginError.value = "Gagal login: server error (${e.code()})"
                }
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                loginError.value = "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                currentUser.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                loginError.value = "Terjadi kesalahan: ${e.localizedMessage ?: "Coba lagi nanti."}"
                currentUser.value = null
            } finally {
                isLoggingIn.value = false
                isLoadingProfile.value = false
            }
        }
    }

    // State register — dipakai jika UI ingin menunggu hasil server
    var isRegistering = mutableStateOf(false)
    var registerError = mutableStateOf<String?>(null)

    fun insert(
        user: User,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isRegistering.value = true
            registerError.value = null
            try {
                val response = repo.insert(user)
                if (!response.isSuccessful) {
                    throw Exception("Server menolak registrasi (${response.code()})")
                }
                // Tambah ke list lokal HANYA jika server sukses.
                // Sebelumnya user ditambah walau DB gagal -> login bypass offline.
                val list = users.value.toMutableList()
                if (!list.any { it.username == user.username }) {
                    list.add(user)
                }
                users.value = list
                isRegistering.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                isRegistering.value = false
                val errMsg = "Registrasi gagal. Periksa koneksi internet Anda."
                registerError.value = errMsg
                onError(errMsg)
            }
        }
    }

    fun update(
        user: User,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repo.update(user)
                if (!response.isSuccessful) {
                    throw Exception("Server menolak update (${response.code()})")
                }
                currentUser.value = user
                val list = users.value.map { if (it.id == user.id || it.username == user.username) user else it }
                users.value = list
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Gagal memperbarui profil. Periksa koneksi internet Anda.")
            }
        }
    }

    // Update profil dengan upload foto dulu bila photoFile adalah file lokal.
    // DB hanya menyimpan filename server -> foto sync antar device.
    fun updateProfileWithPhoto(
        user: User,
        photoFile: File?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            var toUpload: File? = null
            try {
                var finalUser = user
                if (photoFile != null) {
                    // PERF: compress dulu di HP (upload ~1-3 dtk, bukan belasan detik)
                    toUpload = compressImageForUpload(photoFile)
                    val up = repo.uploadProfilePhoto(toUpload, user.id)
                    if (!up.success || up.filename.isNullOrBlank()) {
                        throw Exception(up.message ?: "Upload foto gagal")
                    }
                    finalUser = user.copy(photo = up.filename)
                }
                val response = repo.update(finalUser)
                if (!response.isSuccessful) {
                    throw Exception("Server menolak update (${response.code()})")
                }
                currentUser.value = finalUser
                val list = users.value.map { if (it.id == finalUser.id || it.username == finalUser.username) finalUser else it }
                users.value = list
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Gagal menyimpan foto/profil. Periksa koneksi internet Anda.")
            } finally {
                deleteCompressedTemp(toUpload, photoFile)
            }
        }
    }

    // Registrasi dengan upload foto dulu bila ada file lokal.
    fun registerWithPhoto(
        user: User,
        photoFile: File?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isRegistering.value = true
            registerError.value = null
            var toUpload: File? = null
            try {
                var finalUser = user
                if (photoFile != null) {
                    // PERF: compress dulu di HP (upload ~1-3 dtk, bukan belasan detik)
                    toUpload = compressImageForUpload(photoFile)
                    val up = repo.uploadProfilePhoto(toUpload, null)
                    if (!up.success || up.filename.isNullOrBlank()) {
                        throw Exception(up.message ?: "Upload foto gagal")
                    }
                    finalUser = user.copy(photo = up.filename)
                }
                val response = repo.insert(finalUser)
                if (!response.isSuccessful) {
                    throw Exception("Server menolak registrasi (${response.code()})")
                }
                val list = users.value.toMutableList()
                if (!list.any { it.username == finalUser.username }) {
                    list.add(finalUser)
                }
                users.value = list
                isRegistering.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                isRegistering.value = false
                val errMsg = "Registrasi gagal. Periksa koneksi internet Anda."
                registerError.value = errMsg
                onError(errMsg)
            } finally {
                deleteCompressedTemp(toUpload, photoFile)
            }
        }
    }

    fun delete(
        id: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repo.delete(id)
                if (!response.isSuccessful) {
                    throw Exception("Server menolak hapus (${response.code()})")
                }
                val list = users.value.filter { it.id != id }
                users.value = list
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Gagal menghapus. Periksa koneksi internet Anda.")
            }
        }
    }
}
