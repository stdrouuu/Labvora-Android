package org.ukrida.labvora.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.TestHistoryItem

class HistoryViewModel : ViewModel() {

    private var isFetching = false

    private val _historyList = mutableStateOf<List<TestHistoryItem>>(emptyList())
    val historyList: State<List<TestHistoryItem>> = _historyList

    private val _pendingOrder = mutableStateOf<TestHistoryItem?>(null)
    val pendingOrder: State<TestHistoryItem?> = _pendingOrder

    private val _pendingOrders = mutableStateOf<List<TestHistoryItem>>(emptyList())
    val pendingOrders: State<List<TestHistoryItem>> = _pendingOrders

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    // Bump setiap Status Pesanan menandai pesanan sebagai "sudah dilihat",
    // agar badge di Beranda ikut refresh tanpa perlu refetch.
    var orderSeenVersion = mutableStateOf(0)

    // Batas id booking saat pemesanan terakhir dibuat. Selama terisi, Status
    // Pesanan hanya menandai (pill/stripe) order dengan id di atasnya — jadi
    // yang dibuka dari modal habis memesan hanya menampilkan pill pesanan
    // yang baru dipesan. Dibersihkan saat Status Pesanan ditinggalkan.
    var highlightAboveId = mutableStateOf<Int?>(null)

    val isRefreshing = mutableStateOf(false)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Reset search saat ganti page (jangan simpan di local storage / state permanen)
    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun getHistoryList(userId: Int, forceRefresh: Boolean = false) {
        // PERF: cegah tembakan ganda (recompose cepat = request dobel).
        if (isFetching && !forceRefresh) return
        viewModelScope.launch {
            isFetching = true
            if (forceRefresh) isRefreshing.value = true
            try {
                val list = org.ukrida.labvora.data.api.RetrofitInstance.api.getUserBookings(userId)
                // Filter only completed bookings for history list
                _historyList.value = list.filter { it.status == "Selesai" }
                // Set pendingOrder to the first active/non-completed booking
                _pendingOrder.value = list.firstOrNull { it.status != "Selesai" }
                // Set pendingOrders to all active/non-completed bookings
                _pendingOrders.value = list.filter { it.status != "Selesai" }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isFetching = false
                isRefreshing.value = false
            }
        }
    }

    fun syncWithBooking(bookingViewModel: BookingViewModel) {
        // No-op or custom sync, database refetch will handle it
    }
}

