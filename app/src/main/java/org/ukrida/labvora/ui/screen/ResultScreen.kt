package org.ukrida.labvora.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import org.ukrida.labvora.data.api.RetrofitInstance
import org.ukrida.labvora.viewmodel.ResultViewModel
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.data.model.TestParameterResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    bookingId: Int,
    testId: Int,
    resultViewModel: ResultViewModel,
    bookingViewModel: BookingViewModel,
    gender: String? = "Perempuan",
    date: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(bookingId) {
        resultViewModel.loadResults(bookingId)
    }

    val test = bookingViewModel.allTests.find { it.id == testId } ?: bookingViewModel.allTests[0]

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pemeriksaan Terakhir",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFAFAFA)
                )
            )
        }
    ) { paddingValues ->
        if (bookingId <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFF3F4F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Pemeriksaan tidak ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anda belum memiliki data hasil pemeriksaan laboratorium.",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                // Title Header
                Text(
                    text = "Hasil Pemeriksaan Lab",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

            // Results Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Test Title Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = test.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3CB7A6)
                            )
                            if (!date.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = date,
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Table with Horizontal Scroll
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 560.dp, max = 720.dp)) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(vertical = 12.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NAMA PEMERIKSAAN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.width(200.dp)
                                )
                                Text(
                                    text = "HASIL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = "NILAI RUJUKAN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    text = "SATUAN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = "KETERANGAN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.width(200.dp)
                                )
                            }

                            // Dynamic tables based on testId
                            when (testId) {
                                1 -> {
                                    // result1: Darah Rutin, Nilai-Nilai MC, Hitung Jenis Leukosit
                                    TableSectionHeader("DARAH RUTIN")
                                    resultViewModel.getDarahRutinList(gender).forEach { TableRow(it) }

                                    TableSectionHeader("NILAI-NILAI MC")
                                    resultViewModel.getNilaiMcList().forEach { TableRow(it) }

                                    TableSectionHeader("HITUNG JENIS LEUKOSIT")
                                    resultViewModel.getHitungJenisLeukositList().forEach { TableRow(it) }
                                }
                                2 -> {
                                    // result2: Hitung Jenis Leukosit only
                                    TableSectionHeader("HITUNG JENIS LEUKOSIT")
                                    resultViewModel.getHitungJenisLeukositList().forEach { TableRow(it) }
                                }
                                3 -> {
                                    // result3: Darah Rutin & Nilai-Nilai MC
                                    TableSectionHeader("DARAH RUTIN")
                                    resultViewModel.getDarahRutinList(gender).forEach { TableRow(it) }

                                    TableSectionHeader("NILAI-NILAI MC")
                                    resultViewModel.getNilaiMcList().forEach { TableRow(it) }
                                }
                                else -> {
                                    // Fallback: Darah Rutin
                                    TableSectionHeader("DARAH RUTIN")
                                    resultViewModel.getDarahRutinList(gender).forEach { TableRow(it) }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Download PDF Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = {
                        val tokenParam = RetrofitInstance.authToken?.let { "&token=${android.net.Uri.encode(it)}" } ?: ""
                        val pdfUrl = "${RetrofitInstance.BASE_URL}results_pdf.php?booking_id=$bookingId$tokenParam"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Unduh PDF",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UNDUH PDF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quote Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(0.5.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(0.dp))
                    .padding(top = 24.dp, bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"Dari semua itu, jika kepuasan tubuh ingin sehat, Anda harus memulainya dengan menyembuhkan jiwa.\"",
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "~ Socrates ~",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}
}

@Composable
fun TableSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6))
            .padding(vertical = 8.dp, horizontal = 20.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF374151),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TableRow(param: TestParameterResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(0.5.dp, Color(0xFFF9FAFB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Name (indented if it is a bullet sub-item)
            Row(
                modifier = Modifier.width(200.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (param.isBullet) {
                    Text(
                        text = "• ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Text(
                    text = param.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
            }

            // Result (bold and red if abnormal)
            Text(
                text = param.resultValue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (param.isOut) Color(0xFFEF4444) else Color(0xFF111827),
                modifier = Modifier.width(80.dp)
            )

            // Reference Range
            Text(
                text = param.referenceRange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                modifier = Modifier.width(100.dp)
            )

            // Unit
            Text(
                text = param.unit,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                modifier = Modifier.width(80.dp)
            )

            // Note/Keterangan
            Text(
                text = param.note,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                modifier = Modifier.width(200.dp),
                lineHeight = 14.sp
            )
        }
    }
}
