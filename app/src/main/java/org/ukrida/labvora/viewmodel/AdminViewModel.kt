package org.ukrida.labvora.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.AdminBooking
import java.util.Calendar
import java.util.Date

class AdminViewModel : ViewModel() {

    // Mock initial data
    private val _bookings = mutableStateOf<List<AdminBooking>>(emptyList())

    init {
        getBookings()
    }

    fun getBookings() {
        viewModelScope.launch {
            try {
                _bookings.value = org.ukrida.labvora.data.api.RetrofitInstance.api.getBookings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val bookings: State<List<AdminBooking>> = _bookings

    // Filters and search states
    val searchQuery = mutableStateOf("")
    val searchDate = mutableStateOf("") // YYYY-MM-DD format from DatePickerDialog
    val activeStatusFilter = mutableStateOf("Semua")

    // Chart States
    val chartTimeframe = mutableStateOf("MingguIni") // "MingguIni" or "MingguLalu"
    val selectedChartIndex = mutableStateOf(3)
    val chartDataSets = mapOf(
        "MingguIni" to listOf(4, 8, 5, 12, 9, 15, 10),
        "MingguLalu" to listOf(6, 4, 11, 7, 14, 8, 12)
    )
    val chartLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    // Detail Bottom Sheet States
    val selectedBookingForDetail = mutableStateOf<AdminBooking?>(null)
    val selectedStatusValue = mutableStateOf("Menunggu")
    val cancelReasonInput = mutableStateOf("")

    // Input Results States
    val selectedBookingForInput = mutableStateOf<AdminBooking?>(null)
    val showInputSuccessModal = mutableStateOf(false)

    // Feedback States
    val toastMessage = mutableStateOf<String?>(null)
    val toastIsError = mutableStateOf(false)

    // Derived Statistics
    val totalBookings: Int get() = _bookings.value.size
    val totalPending: Int get() = _bookings.value.count { it.status == "Menunggu" }
    val totalResultQueue: Int get() = _bookings.value.count { it.status == "Sedang diuji" && it.resultStatus == "Menunggu Hasil" }
    val totalTesting: Int get() = _bookings.value.count { it.status == "Sedang diuji" }
    val totalSuccess: Int get() = _bookings.value.count { it.status == "Selesai" }

    // Actions
    fun filterBookings(): List<AdminBooking> {
        val query = searchQuery.value.lowercase().trim()
        val dateFilter = searchDate.value // Format YYYY-MM-DD
        val filterTab = activeStatusFilter.value

        return _bookings.value.filter { booking ->
            // Filter status tab
            if (filterTab != "Semua" && booking.status != filterTab) return@filter false

            // Filter search text
            if (query.isNotEmpty() &&
                !booking.patientName.lowercase().contains(query) &&
                !booking.id.lowercase().contains(query)
            ) return@filter false

            // Filter date picker format e.g. "20 Jun 2026" vs "2026-06-20" or similar
            if (dateFilter.isNotEmpty()) {
                val formattedBookingDate = convertBookingDateToISO(booking.date)
                if (formattedBookingDate != dateFilter) return@filter false
            }

            true
        }
    }

    private fun monthNameToNumber(token: String): String? {
        return when (token.lowercase().trim()) {
            "januari", "jan", "january" -> "01"
            "februari", "feb", "february" -> "02"
            "maret", "mar", "march" -> "03"
            "april", "apr" -> "04"
            "mei", "may" -> "05"
            "juni", "jun", "june" -> "06"
            "juli", "jul", "july" -> "07"
            "agustus", "agu", "aug", "august" -> "08"
            "september", "sep", "sept" -> "09"
            "oktober", "okt", "oct", "october" -> "10"
            "november", "nov" -> "11"
            "desember", "des", "dec", "december" -> "12"
            else -> null
        }
    }

    private fun convertBookingDateToISO(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        // Buang prefix hari ("Sabtu, 27 Juni 2026") & suffix jam
        var s = dateStr.trim()
        if (s.contains(",")) s = s.substringAfterLast(",").trim()
        s = s.split("·")[0].trim()
        if (s.contains("Jam", ignoreCase = true)) s = s.substringBefore("Jam").trim()
        s = s.replace(",", " ").replace("\\s+".toRegex(), " ").trim()
        if (s.isEmpty()) return ""

        // ISO langsung: 2026-06-28 / 2026/06/28
        val isoMatch = Regex("(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})").find(s)
        if (s.matches(Regex("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}.*"))) {
            isoMatch?.let {
                val y = it.groupValues[1]
                val m = it.groupValues[2].padStart(2, '0')
                val d = it.groupValues[3].padStart(2, '0')
                return "$y-$m-$d"
            }
        }

        // DMY: "27 Juni 2026" / "27 Jun 2026" / "27-06-2026" / "27/06/2026"
        val parts = s.split(" ", "-", "/").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size >= 3) {
            val day = parts[0].filter { it.isDigit() }.padStart(2, '0')
            val monthToken = parts[1]
            val year = parts[2].filter { it.isDigit() }
            if (day.length == 2 && year.length == 4) {
                val monthNum = monthToken.filter { it.isDigit() }.padStart(2, '0')
                    .takeIf { it.length == 2 && it != "00" }
                    ?: monthNameToNumber(monthToken)
                if (monthNum != null) return "$year-$monthNum-$day"
            }
        }
        return ""
    }

    fun selectBookingForDetail(booking: AdminBooking?) {
        selectedBookingForDetail.value = booking
        if (booking != null) {
            selectedStatusValue.value = booking.status
            cancelReasonInput.value = booking.cancelReason ?: ""
        } else {
            cancelReasonInput.value = ""
        }
    }

    fun updateSelectedStatusValue(status: String) {
        val originalStatus = selectedBookingForDetail.value?.status ?: return

        // Safeguard policies
        if (originalStatus == "Menunggu" && (status == "Sedang diuji" || status == "Selesai")) {
            showToast("Tindakan tidak valid! Dari status Menunggu, Anda hanya bisa mengubah ke Dikonfirmasi atau Dibatalkan.", true)
            return
        }

        if ((originalStatus == "Dikonfirmasi" || originalStatus == "Sedang diuji" || originalStatus == "Selesai") && status == "Dibatalkan") {
            showToast("Pembatalan diblokir oleh sistem! Pesanan terkonfirmasi tidak dapat dibatalkan.", true)
            return
        }

        if ((originalStatus == "Dikonfirmasi" || originalStatus == "Sedang diuji" || originalStatus == "Selesai" || originalStatus == "Dibatalkan") && status == "Menunggu") {
            showToast("Tindakan tidak valid! Tidak dapat memindahkan pesanan kembali ke status Menunggu.", true)
            return
        }

        if ((originalStatus == "Sedang diuji" || originalStatus == "Selesai") && status == "Dikonfirmasi") {
            showToast("Tindakan tidak valid! Pesanan sedang diuji atau selesai tidak dapat kembali ke Dikonfirmasi.", true)
            return
        }

        selectedStatusValue.value = status
    }

    fun saveStatusChange() {
        val booking = selectedBookingForDetail.value ?: return
        val newStatus = selectedStatusValue.value
        val cancelReason = if (newStatus == "Dibatalkan") cancelReasonInput.value else null

        viewModelScope.launch {
            try {
                val payload = mutableMapOf<String, Any>(
                    "id" to booking.id.toInt(),
                    "status" to newStatus
                )
                if (cancelReason != null) {
                    payload["cancel_reason"] = cancelReason
                }
                val response = org.ukrida.labvora.data.api.RetrofitInstance.api.updateBookingStatus(payload)
                if (response.isSuccessful) {
                    getBookings()
                    showToast("Status pesanan ${booking.patientName} disimpan menjadi $newStatus.")
                } else {
                    showToast("Gagal merubah status: ${response.message()}", isError = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Terjadi kesalahan koneksi.", isError = true)
            } finally {
                selectBookingForDetail(null)
            }
        }
    }

    fun saveInputResults(bookingId: String, resultsMap: Map<String, String>) {
        viewModelScope.launch {
            try {
                val payload = mutableMapOf<String, Any?>()
                payload["booking_id"] = bookingId.toInt()
                resultsMap.forEach { (key, value) ->
                    val dbKey = key.lowercase().replace("-", "_")
                    payload[dbKey] = value
                }

                val response = org.ukrida.labvora.data.api.RetrofitInstance.api.saveLabResults(payload)
                if (response.isSuccessful) {
                    getBookings()
                    showInputSuccessModal.value = true
                } else {
                    showToast("Gagal menyimpan hasil lab: ${response.message()}", isError = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Terjadi kesalahan koneksi.", isError = true)
            }
        }
    }

    // Upcoming patient feature removed as requested

    fun showToast(message: String, isError: Boolean = false) {
        toastMessage.value = message
        toastIsError.value = isError
    }

    fun clearToast() {
        toastMessage.value = null
    }
}
