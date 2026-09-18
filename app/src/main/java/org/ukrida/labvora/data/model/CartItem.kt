package org.ukrida.labvora.data.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val test: LabTest,
    var clinicName: String = "Klinik Labvora PIK",
    var bookingDate: String = "2026-6-11",
    var bookingTime: String = "14:00",
    var hasDoctorReferral: Boolean = false,
    var isChecked: Boolean = true
)
