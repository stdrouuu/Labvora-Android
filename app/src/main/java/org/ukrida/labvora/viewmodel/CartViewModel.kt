package org.ukrida.labvora.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.api.RetrofitInstance
import org.ukrida.labvora.data.model.CartItem
import org.ukrida.labvora.data.model.LabTest
import java.text.NumberFormat
import java.util.Locale

class CartViewModel : ViewModel() {

    private val gson = Gson()
    private var currentUserId: Int = 0

    private val _cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val cartItems: State<List<CartItem>> = _cartItems

    val toastMessage = mutableStateOf<String?>(null)
    val showCheckoutSuccessModal = mutableStateOf(false)
    val isCheckingOut = mutableStateOf(false)

    val cartItemCount: Int
        get() = _cartItems.value.size

    val checkedCount: Int
        get() = _cartItems.value.count { it.isChecked }

    val isAllChecked: Boolean
        get() = _cartItems.value.isNotEmpty() && _cartItems.value.all { it.isChecked }

    val adminFeeVal: Int = 50000

    val totalPriceVal: Int
        get() = _cartItems.value.filter { it.isChecked }.sumOf { it.test.priceVal }

    val subtotalCheckFeeVal: Int
        get() {
            val total = totalPriceVal
            return if (total > adminFeeVal) total - adminFeeVal else total
        }

    val subtotalPriceFormatted: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return formatter.format(subtotalCheckFeeVal).replace(",00", "")
        }

    val adminFeeFormatted: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return formatter.format(adminFeeVal).replace(",00", "")
        }

    val totalPriceFormatted: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return formatter.format(totalPriceVal).replace(",00", "")
        }

    fun initCartForUser(context: Context, userId: Int) {
        currentUserId = userId
        val prefs = context.getSharedPreferences("labvora_cart_pref", Context.MODE_PRIVATE)
        val json = prefs.getString("cart_items_$userId", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val items: List<CartItem> = gson.fromJson(json, type) ?: emptyList()
                _cartItems.value = items
            } catch (e: Exception) {
                e.printStackTrace()
                _cartItems.value = emptyList()
            }
        } else {
            _cartItems.value = emptyList()
        }
    }

    private fun persistCart(context: Context) {
        if (currentUserId <= 0) return
        try {
            val prefs = context.getSharedPreferences("labvora_cart_pref", Context.MODE_PRIVATE)
            val json = gson.toJson(_cartItems.value)
            prefs.edit().putString("cart_items_$currentUserId", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addToCart(
        test: LabTest,
        clinicName: String = "Klinik Labvora PIK",
        bookingDate: String = "2026-6-11",
        bookingTime: String = "14:00",
        hasDoctorReferral: Boolean = false,
        context: Context? = null
    ) {
        val currentList = _cartItems.value.toMutableList()
        currentList.add(
            CartItem(
                test = test,
                clinicName = clinicName,
                bookingDate = bookingDate,
                bookingTime = bookingTime,
                hasDoctorReferral = hasDoctorReferral,
                isChecked = true
            )
        )
        _cartItems.value = currentList
        context?.let { persistCart(it) }
        toastMessage.value = "${test.title} berhasil ditambahkan ke Keranjang!"
    }

    fun removeFromCart(cartItemId: String, context: Context? = null) {
        _cartItems.value = _cartItems.value.filter { it.id != cartItemId }
        context?.let { persistCart(it) }
    }

    fun updateItemSchedule(
        cartItemId: String,
        clinicName: String,
        bookingDate: String,
        bookingTime: String,
        context: Context? = null
    ) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.id == cartItemId) {
                item.copy(
                    clinicName = clinicName,
                    bookingDate = bookingDate,
                    bookingTime = bookingTime
                )
            } else {
                item
            }
        }
        context?.let { persistCart(it) }
        toastMessage.value = "Jadwal dan lokasi berhasil diperbarui!"
    }

    fun toggleItemChecked(cartItemId: String, context: Context? = null) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.id == cartItemId) {
                item.copy(isChecked = !item.isChecked)
            } else {
                item
            }
        }
        context?.let { persistCart(it) }
    }

    fun toggleSelectAll(checked: Boolean, context: Context? = null) {
        _cartItems.value = _cartItems.value.map { item ->
            item.copy(isChecked = checked)
        }
        context?.let { persistCart(it) }
    }

    fun checkoutCheckedItems(userId: Int, context: Context? = null, onComplete: () -> Unit = {}) {
        val itemsToCheckout = _cartItems.value.filter { it.isChecked }
        if (itemsToCheckout.isEmpty() || isCheckingOut.value) return

        viewModelScope.launch {
            isCheckingOut.value = true
            var successCount = 0
            try {
                for (item in itemsToCheckout) {
                    val response = RetrofitInstance.api.createBooking(
                        mapOf(
                            "user_id" to userId,
                            "test_id" to item.test.id,
                            "booking_date" to item.bookingDate,
                            "booking_time" to item.bookingTime,
                            "clinic_name" to item.clinicName,
                            "status" to "Menunggu",
                            "result_status" to "Menunggu Hasil",
                            "referral_photo" to if (item.hasDoctorReferral) "present" else null
                        )
                    )
                    if (response.isSuccessful) {
                        successCount++
                    }
                }
                if (successCount > 0) {
                    _cartItems.value = _cartItems.value.filter { !it.isChecked }
                    context?.let { persistCart(it) }
                    showCheckoutSuccessModal.value = true
                    onComplete()
                } else {
                    toastMessage.value = "Gagal memproses checkout. Silakan coba lagi."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toastMessage.value = "Terjadi kesalahan koneksi saat checkout."
            } finally {
                isCheckingOut.value = false
            }
        }
    }

    fun clearToast() {
        toastMessage.value = null
    }

    fun resetSuccessModal() {
        showCheckoutSuccessModal.value = false
    }
}
