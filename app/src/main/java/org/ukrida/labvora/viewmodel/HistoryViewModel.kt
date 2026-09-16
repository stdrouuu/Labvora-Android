package org.ukrida.labvora.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.TestHistoryItem

class HistoryViewModel : ViewModel() {

    private val _historyList = mutableStateOf<List<TestHistoryItem>>(emptyList())
    val historyList: State<List<TestHistoryItem>> = _historyList

    private val _pendingOrder = mutableStateOf<TestHistoryItem?>(null)
    val pendingOrder: State<TestHistoryItem?> = _pendingOrder

    private val _pendingOrders = mutableStateOf<List<TestHistoryItem>>(emptyList())
    val pendingOrders: State<List<TestHistoryItem>> = _pendingOrders

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Reset search saat ganti page (jangan simpan di local storage / state permanen)
    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun getHistoryList(userId: Int) {
        viewModelScope.launch {
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
            }
        }
    }

    fun syncWithBooking(bookingViewModel: BookingViewModel) {
        // No-op or custom sync, database refetch will handle it
    }
}

