// View: Layar Beranda (Home) utama yang menampilkan ringkasan data dan menu cepat
package org.ukrida.labvora.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ukrida.labvora.R
import org.ukrida.labvora.viewmodel.UserViewModel
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.viewmodel.HistoryViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    bookingViewModel: BookingViewModel,
    historyViewModel: HistoryViewModel,
    onNavigateToListTest: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToResult: (Int, Int, String?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrderStatus: () -> Unit = onNavigateToProfile
) {
    val context = LocalContext.current
    val currentUser = userViewModel.currentUser.value
    val userName = currentUser?.name ?: "Guest"

    var searchQuery by remember { mutableStateOf("") }
    // ponytail: promo terpilih untuk modal detail — semua banner wajib ada output
    var selectedPromoIndex by remember { mutableStateOf<Int?>(null) }

    val lastHistoryItem = historyViewModel.historyList.value.firstOrNull()
    val lastTestDate = lastHistoryItem?.date ?: "-"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .align(Alignment.CenterHorizontally)
        ) {
        // Welcome Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "HALO,",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9CA3AF),
                letterSpacing = 1.5.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF3CB7A6),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .offset(y = 4.dp)
                        .background(Color(0xFFF86066), CircleShape)
                )
            }
            Text(
                text = "Siap untuk mengoptimalkan kesehatan Anda hari ini di Labvora?",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 6.dp),
                lineHeight = 16.sp
            )
            Text(
                text = buildAnnotatedString {
                    append("Terakhir Test Lab: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))) {
                        append(lastTestDate)
                    }
                },
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Search Bar Section with Auto-suggestions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari tes lab...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                )
            }

            // Recommendations Dropdown List
            if (searchQuery.trim().isNotEmpty()) {
                val filteredTests = bookingViewModel.allTests.filter {
                    it.title.lowercase().contains(searchQuery.lowercase().trim())
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        if (filteredTests.isNotEmpty()) {
                            filteredTests.forEachIndexed { index, test ->
                                if (index > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFF3F4F6))
                                            .padding(horizontal = 16.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = ""
                                            onNavigateToDetail(test.id)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = test.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F2937)
                                        )
                                        Text(
                                            text = test.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF3CB7A6),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Pilih",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mohon Maaf, Tes Belum Tersedia",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action 1
            QuickActionButton(
                icon = Icons.Default.Inventory,
                label = "Lihat\nStatus Pesanan",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToOrderStatus
            )
            // Action 2
            QuickActionButton(
                icon = Icons.Default.Person,
                label = "Pemeriksaan \nTerakhir",
                modifier = Modifier.weight(1f),
                onClick = {
                    val lastCompletedItem = historyViewModel.historyList.value.firstOrNull { it.status == "Selesai" }
                    if (lastCompletedItem != null) {
                        onNavigateToResult(lastCompletedItem.id, lastCompletedItem.testId, lastCompletedItem.date)
                    } else {
                        onNavigateToResult(0, 0, null)
                    }
                }
            )
            // Action 3
            QuickActionButton(
                icon = Icons.Default.AssignmentInd,
                label = "Riwayat\nPemeriksaan",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHistory
            )
        }

        // Promo Banners Row — fixed card size + 2-dot pagination
        val promoScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(promoScrollState)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner 1
                PromoBannerCard(
                    title = buildAnnotatedString {
                        append("Cek Darah di ")
                        withStyle(style = SpanStyle(color = Color(0xFF3CB7A6), fontWeight = FontWeight.ExtraBold)) {
                            append("Klinik Cinta Kasih!")
                        }
                    },
                    discountText = "Hemat 15%",
                    discountDesc = "Semua Skrining Lab Lengkap",
                    minTransaction = "*Min. Transaksi Rp1.5Jt",
                    imageRes = R.drawable.kesehatan,
                    onClick = { selectedPromoIndex = 0 }
                )
                // Banner 2
                PromoBannerCard(
                    title = buildAnnotatedString {
                        append("Proteksi Keluarga Bersama ")
                        withStyle(style = SpanStyle(color = Color(0xFF3CB7A6), fontWeight = FontWeight.ExtraBold)) {
                            append("Cinta Care")
                        }
                    },
                    discountText = "Hemat 20%",
                    discountDesc = "Paket Pemeriksaan Lansia",
                    minTransaction = "*Termasuk Konsultasi Dokter",
                    imageRes = R.drawable.lansia,
                    onClick = { selectedPromoIndex = 1 }
                )
            }

            // Pagination dots — 2 dots karena iklan cuma 2
            val promoMax = promoScrollState.maxValue
            val promoProgress = if (promoMax > 0) promoScrollState.value.toFloat() / promoMax else 0f
            val promoActiveDot = if (promoProgress < 0.5f) 0 else 1
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (idx == promoActiveDot) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (idx == promoActiveDot) Color(0xFF3CB7A6) else Color(0xFFD1D5DB))
                    )
                }
            }
        }

        // Popular Test Lab Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "PILIHAN TERBAIK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tes Lab Populer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }
                Row(
                    modifier = Modifier.clickable { onNavigateToListTest() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lihat Semua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "See All",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            val popularScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(popularScrollState)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PopularTestCard(
                        title = "Cek Hematologi (Lengkap)",
                        desc = "Total, LDL, HDL & Trigliserida",
                        buttonColor = Color(0xFFF86066),
                        onPesanClick = { onNavigateToDetail(1) }
                    )
                    PopularTestCard(
                        title = "Hitung Jenis Leukosit",
                        desc = "Pemeriksaan Basofil, Eosinofil, Neutrofil, Limfosit, dan Monosit.",
                        buttonColor = Color(0xFFFFA92A),
                        onPesanClick = { onNavigateToDetail(2) }
                    )
                    PopularTestCard(
                        title = "Cek Darah Rutin & Nilai-Nilai MC",
                        desc = "Pemeriksaan Hemoglobin, Hematokrit, Eritrosit, Leukosit total, Trombosit, nilai-nilai MC",
                        buttonColor = Color(0xFF40B5A7),
                        onPesanClick = { onNavigateToDetail(3) }
                    )
                }

                // Pagination / scrollbar hint — minimal track + 3 dots
                val maxVal = popularScrollState.maxValue
                val progress = if (maxVal > 0) popularScrollState.value.toFloat() / maxVal else 0f
                val activeDot = when {
                    progress < 0.33f -> 0
                    progress < 0.66f -> 1
                    else -> 2
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (idx == activeDot) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (idx == activeDot) Color(0xFF3CB7A6) else Color(0xFFD1D5DB))
                        )
                    }
                }
            }
        }
        }
    }

    // ================= promo detail modal — semua iklan wajib ada output =================
    if (selectedPromoIndex != null) {
        val isFirst = selectedPromoIndex == 0
        AlertDialog(
            onDismissRequest = { selectedPromoIndex = null },
            title = {
                Text(
                    text = if (isFirst) "Promo Skrining Lab Lengkap" else "Promo Paket Lansia",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = if (isFirst)
                        "Hemat 15% untuk semua skrining lab lengkap di Klinik Cinta Kasih.\n\n• Min. transaksi Rp1.500.000\n• Berlaku untuk semua cabang Klinik Cinta Kasih\n• Tunjukkan halaman ini saat pembayaran di klinik"
                    else
                        "Hemat 20% untuk paket pemeriksaan lansia Cinta Care.\n\n• Termasuk konsultasi dokter\n• Berlaku untuk semua cabang Klinik Cinta Kasih\n• Tunjukkan halaman ini saat pembayaran di klinik",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedPromoIndex = null }) {
                    Text(text = "Tutup", color = Color(0xFF3CB7A6), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    showGratisBadge: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF3CB7A6),
                    modifier = Modifier.size(28.dp)
                )
            }
            if (showGratisBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp)
                        .background(Color(0xFFF86066), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "GRATIS",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4B5563),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun PromoBannerCard(
    title: androidx.compose.ui.text.AnnotatedString,
    discountText: String,
    discountDesc: String,
    minTransaction: String,
    imageRes: Int,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(152.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = discountText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF86066),
                        lineHeight = 14.sp,
                        maxLines = 1
                    )
                    Text(
                        text = discountDesc,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563),
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = minTransaction,
                        fontSize = 8.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 10.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Promo Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun PopularTestCard(
    title: String,
    desc: String,
    buttonColor: Color,
    onPesanClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(175.dp)
            .height(192.dp)
            .clickable { onPesanClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 13.sp,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onPesanClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(
                    text = "Pesan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
