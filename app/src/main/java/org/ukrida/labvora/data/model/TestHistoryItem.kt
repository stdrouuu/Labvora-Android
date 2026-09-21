package org.ukrida.labvora.data.model

import com.google.gson.annotations.SerializedName

data class TestHistoryItem(
    val id: Int,
    val testId: Int, // references LabTest.id (1, 2, or 3)
    val title: String,
    val date: String,
    val clinicName: String,
    val status: String, // e.g. "Selesai" or "Diproses"
    val referralPhoto: String? = null,
    val cancelReason: String? = null,
    @SerializedName(value = "time", alternate = ["booking_time", "bookingTime"])
    val bookingTime: String? = null,
    val testTitle: String = title
) {
    val time: String? get() = bookingTime
}
