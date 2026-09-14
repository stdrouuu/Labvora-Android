// ViewModel untuk mengelola logika bisnis dan state terkait data User (Login, Register, CRUD User)
package org.ukrida.labvora.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.User
import org.ukrida.labvora.data.repository.UserRepository

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

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            isDeletingAccount.value = true
            deleteAccountError.value = null
            try {
                repo.delete(userId)
                // Hapus dari daftar lokal
                users.value = users.value.filter { it.id != userId }
                // Bersihkan sesi pengguna
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
            loginError.value = null
            try {
                currentUser.value = repo.login(username, password)
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
            }
        }
    }

    fun insert(user: User) {
        viewModelScope.launch {
            try {
                repo.insert(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Add user to local list so they can log in even if database write fails/is offline
            val list = users.value.toMutableList()
            if (!list.any { it.username == user.username }) {
                list.add(user)
            }
            users.value = list
        }
    }

    fun update(user: User) {
        viewModelScope.launch {
            try {
                repo.update(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            currentUser.value = user
            val list = users.value.map { if (it.id == user.id || it.username == user.username) user else it }
            users.value = list
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                repo.delete(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val list = users.value.filter { it.id != id }
            users.value = list
        }
    }
}
