// ViewModel untuk mengelola proses pemesanan tes lab dan data katalog tes
package org.ukrida.labvora.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.LabTest
import org.ukrida.labvora.R

class BookingViewModel : ViewModel() {

    val allTests = listOf(
        LabTest(
            id = 1,
            title = "Cek Hematologi (Lengkap)",
            description = "tes darah menyeluruh termasuk sel darah merah, sel darah putih, dan trombosit.",
            price = "Rp 2.000.000",
            priceVal = 2000000,
            category = "Hematologi",
            duration = "45 Menit",
            biomarkerCount = 15,
            resultDuration = "24 Jam",
            benefits = "Tes hematologi lengkap bermanfaat untuk mendeteksi anemia, mengidentifikasi infeksi, menilai kemampuan pembekuan darah, serta menyaring risiko penyakit serius seperti kanker darah secara dini.",
            fullDescription = """
                Hematologi Lengkap (Complete Blood Count) adalah tes darah yang paling sering dilakukan untuk mengevaluasi status kesehatan secara keseluruhan dan mendeteksi berbagai macam gangguan medis.
                
                Pemeriksaan ini mencakup evaluasi terhadap komponen penting sel darah Anda:
                • Sel Darah Merah (RBC): Mengangkut oksigen ke seluruh tubuh.
                • Sel Darah Putih (WBC): Sistem pertahanan tubuh melawan infeksi.
                • Trombosit (Platelets): Berperan krusial dalam proses pembekuan darah.
                • Hemoglobin & Hematokrit: Protein pengikat oksigen dan persentase volume sel darah merah.
                
                Sangat direkomendasikan untuk medical check-up rutin atau jika Anda mengalami gejala kelelahan kronis, memar yang tidak biasa, dan demam tanpa sebab yang jelas.
            """.trimIndent(),
            preparations = listOf(
                "Puasa 12 jam sebelum pengambilan darah Anda.",
                "Hanya air putih. Tanpa kopi, teh, atau jus.",
                "Tanpa alkohol selama 24 jam sebelum pengujian."
            ),
            isPuasa = true,
            themeColorHex = 0xFFFA6A71L,
            imageRes = R.drawable.tesdarah
        ),
        LabTest(
            id = 2,
            title = "Hitung Jenis Leukosit",
            description = "Pemeriksaan Basofil, Eosinofil, Neutrofil, Limfosit, dan Monosit.",
            price = "Rp 500.000",
            priceVal = 500000,
            category = "Darah",
            duration = "30 Menit",
            biomarkerCount = 5,
            resultDuration = "24 Jam",
            benefits = "Pemeriksaan hitung jenis leukosit (differential count) dilakukan untuk mengetahui persentase setiap jenis sel darah putih dalam tubuh, membantu diagnosis infeksi, alergi, atau leukemia.",
            fullDescription = """
                Hitung jenis leukosit mengevaluasi 5 komponen sel darah putih utama:
                • Basofil: Berperan dalam respon imun dan reaksi alergi.
                • Eosinofil: Melawan infeksi parasit dan merespon alergi.
                • Neutrofil: Sel imun garis depan untuk membunuh bakteri.
                • Limfosit: Menghasilkan antibodi untuk perlindungan jangka panjang.
                • Monosit: Membersihkan sel mati dan melawan kuman.
                
                Sangat berguna untuk mencari tahu penyebab demam tinggi atau gejala peradangan sistemik lainnya.
            """.trimIndent(),
            preparations = listOf(
                "Tidak wajib puasa, kecuali disarankan dokter.",
                "Cukupi minum air putih sebelum pemeriksaan.",
                "Laporkan obat-obatan yang sedang dikonsumsi."
            ),
            isPuasa = false,
            themeColorHex = 0xFFFCA434L,
            imageRes = R.drawable.leukosit  
        ),
        LabTest(
            id = 3,
            title = "Cek Darah Rutin & Nilai-Nilai MC",
            description = "Pemeriksaan Hemoglobin, Hematokrit, Eritrosit, Leukosit total, Trombosit, serta Nilai-Nilai MC (MCV, MCH, MCHC) dan RDW-CV.",
            price = "Rp 1.500.000",
            priceVal = 1500000,
            category = "Darah",
            duration = "40 Menit",
            biomarkerCount = 10,
            resultDuration = "24 Jam",
            benefits = "Menilai kesehatan darah secara keseluruhan dan secara spesifik mengevaluasi ukuran serta konsentrasi hemoglobin di dalam sel darah merah untuk mendeteksi berbagai jenis anemia.",
            fullDescription = """
                Pemeriksaan ini mencakup parameter darah rutin dan indeks eritrosit (Nilai-Nilai MC):
                • Hemoglobin, Hematokrit, Eritrosit, Leukosit, dan Trombosit.
                • MCV (Mean Corpuscular Volume): Mengukur rata-rata volume/ukuran sel darah merah.
                • MCH (Mean Corpuscular Hemoglobin): Mengukur jumlah rata-rata hemoglobin dalam sel darah merah.
                • MCHC (Mean Corpuscular Hemoglobin Concentration): Konsentrasi rata-rata hemoglobin.
                • RDW-CV: Distribusi variasi ukuran sel darah merah.
            """.trimIndent(),
            preparations = listOf(
                "Tidak diwajibkan puasa.",
                "Minum air putih secukupnya sebelum pengambilan sampel.",
                "Istirahat yang cukup di malam sebelum pemeriksaan."
            ),
            isPuasa = false,
            themeColorHex = 0xFF3CB7A6L,
            imageRes = R.drawable.darahmc
        )
    )

    // Booking Wizard state
    var selectedTest by mutableStateOf<LabTest?>(null)
        private set

    var selectedClinic by mutableStateOf("Klinik Labvora PIK")
    var selectedDate by mutableStateOf("2026-6-11") // default date as string format: YYYY-MM-DD
    var selectedTimeSlot by mutableStateOf("14:00")
    var hasDoctorReferral by mutableStateOf(false)

    // Order status states
    var isConfirmingOrder by mutableStateOf(false)
        private set
    var isOrderCompleted by mutableStateOf(false)
        private set
    var showToastMessage by mutableStateOf(false)
    var showSuccessModal by mutableStateOf(false)

    fun selectTestById(id: Int) {
        selectedTest = allTests.find { it.id == id }
    }

    fun confirmOrder(userId: Int, onComplete: () -> Unit = {}) {
        if (isConfirmingOrder) return
        viewModelScope.launch {
            isConfirmingOrder = true
            try {
                val response = org.ukrida.labvora.data.api.RetrofitInstance.api.createBooking(
                    mapOf(
                        "user_id" to userId,
                        "test_id" to (selectedTest?.id ?: 1),
                        "booking_date" to selectedDate,
                        "booking_time" to selectedTimeSlot,
                        "clinic_name" to selectedClinic,
                        "status" to "Menunggu",
                        "result_status" to "Menunggu Hasil",
                        "referral_photo" to if (hasDoctorReferral) "present" else null
                    )
                )
                if (response.isSuccessful) {
                    isOrderCompleted = true
                    showToastMessage = true
                    showSuccessModal = true
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isConfirmingOrder = false
            }
        }
    }

    fun resetOrderState() {
        isOrderCompleted = false
        showToastMessage = false
        showSuccessModal = false
        hasDoctorReferral = false
    }
}
