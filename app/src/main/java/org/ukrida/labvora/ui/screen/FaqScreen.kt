// View: Layar FAQ (Pertanyaan yang Sering Ditanyakan) dengan accordion
package org.ukrida.labvora.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FaqItem(
    val question: String,
    val answer: String
)

private val faqList = listOf(
    FaqItem(
        question = "Bagaimana cara memesan tes lab di Labvora?",
        answer = "Pilih menu Tes Lab, pilih jenis pemeriksaan yang diinginkan, tekan Pesan, lalu pilih klinik, tanggal, dan jam pemeriksaan. Setelah itu konfirmasi pesanan di keranjang dan lakukan checkout."
    ),
    FaqItem(
        question = "Apakah saya harus puasa sebelum tes darah?",
        answer = "Tergantung jenis tesnya. Setiap detail tes mencantumkan label Wajib Puasa atau Tanpa Puasa. Jika wajib puasa, disarankan puasa 8-12 jam sebelum pengambilan sampel (tetap boleh minum air putih)."
    ),
    FaqItem(
        question = "Bagaimana cara membayar pesanan saya?",
        answer = "Saat ini pembayaran dilakukan langsung di klinik saat Anda datang sesuai jadwal. Silakan bawa bukti pesanan dan lakukan pembayaran serta administrasi di klinik."
    ),
    FaqItem(
        question = "Di mana saya bisa melihat hasil pemeriksaan?",
        answer = "Hasil yang sudah selesai dapat dilihat di menu Riwayat Pemeriksaan. Pilih riwayat yang berstatus Selesai untuk membuka halaman hasil lengkap."
    ),
    FaqItem(
        question = "Bisakah saya mengubah jadwal atau lokasi klinik setelah memasukkan ke keranjang?",
        answer = "Bisa, selama pesanan masih di keranjang. Buka Keranjang Saya, tekan Ubah Jadwal pada item terkait, lalu pilih ulang jam dan lokasi klinik."
    ),
    FaqItem(
        question = "Apa yang harus saya bawa saat datang ke klinik?",
        answer = "Bawa identitas diri dan bukti pesanan. Jika Anda memiliki surat rujukan dokter, jangan lupa bawa surat rujukan Anda pada hari pemeriksaan."
    ),
    FaqItem(
        question = "Bagaimana jika pesanan saya dibatalkan?",
        answer = "Pesanan yang dibatalkan akan menampilkan alasan pembatalan di halaman Status Pesanan. Silakan buat pesanan baru atau hubungi klinik terkait untuk informasi lebih lanjut."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    onBack: () -> Unit
) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pertanyaan yang Sering Ditanyakan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                        maxLines = 1
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
                    containerColor = Color.White
                ),
                modifier = Modifier.border(0.5.dp, Color(0xFFF3F4F6))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(faqList) { index, faq ->
                val expanded = expandedIndex == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { expandedIndex = if (expanded) null else index },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (expanded) Color(0xFF3CB7A6) else Color(0xFFF3F4F6)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (expanded) Color(0xFF3CB7A6) else Color(0xFFE6F7F5),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = if (expanded) Color.White else Color(0xFF3CB7A6),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = faq.question,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937),
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Tutup" else "Buka",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = faq.answer,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
