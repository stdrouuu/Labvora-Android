package org.ukrida.labvora.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.ui.components.EducationDisclaimerBanner

import org.ukrida.labvora.ui.components.LabvoraPullToRefreshBox
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

data class LabTest(
    val id: Int,
    val title: String,
    val description: String,
    val price: String,
    val category: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
    val buttonColor: Color
)

@Composable
fun ListTestScreen(
    onNavigateToDetail: (testId: Int) -> Unit = {},
    bookingViewModel: BookingViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val allTests = remember {
        listOf(
            LabTest(
                id = 1,
                title = "Cek Hematologi (Lengkap)",
                description = "tes darah menyeluruh termasuk sel darah merah, sel darah putih, dan trombosit.",
                price = "Rp 2.000.000",
                category = "HEMATOLOGI",
                icon = Icons.Default.WaterDrop,
                iconBg = Color(0xFFFFEAEA),
                iconColor = Color(0xFFFA6A71),
                buttonColor = Color(0xFFFA6A71)
            ),
            LabTest(
                id = 2,
                title = "Hitung Jenis Leukosit",
                description = "Pemeriksaan Basofil, Eosinofil, Neutrofil, Limfosit, dan Monosit.",
                price = "Rp 500.000",
                category = "DARAH",
                icon = Icons.Default.Opacity,
                iconBg = Color(0xFFFFF3E5),
                iconColor = Color(0xFFFCA434),
                buttonColor = Color(0xFFFCA434)
            ),
            LabTest(
                id = 3,
                title = "Cek Darah Rutin & Nilai-Nilai MC",
                description = "Pemeriksaan Hemoglobin, Hematokrit, Eritrosit, Leukosit total, Trombosit, serta Nilai-Nilai MC (MCV, MCH, MCHC) dan RDW-CV.",
                price = "Rp 1.500.000",
                category = "DARAH",
                icon = Icons.Default.CalendarToday,
                iconBg = Color(0xFFE6F7F5),
                iconColor = Color(0xFF3CB7A6),
                buttonColor = Color(0xFF3CB7A6)
            )
        )
    }

    val filteredTests = remember(searchQuery) {
        allTests.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LabvoraPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                delay(600)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Search & Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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

        // Section Title
        Text(
            text = "Tes Lab Populer",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        EducationDisclaimerBanner(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // Cards List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredTests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFFF2F4F7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SentimentDissatisfied,
                                contentDescription = "Pemeriksaan tidak ditemukan",
                                tint = Color(0xFF98A2B3),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Pemeriksaan tidak ditemukan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2939),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Coba gunakan kata kunci tes atau tanggal lain.",
                            fontSize = 14.sp,
                            color = Color(0xFF667085),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                filteredTests.forEach { test ->
                    LabTestCardItem(test, onPesanClick = { onNavigateToDetail(test.id) })
                }
            }

            // Promo banner card (static, info only)
            PromoSpecialBanner()

            // Need help card (static - Disembunyikan sementara)
            // NeedHelpCard(onContactClick = { showHelpDialog = true })
        }
    }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "Pusat Bantuan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = "Layanan Pusat Bantuan sedang dalam pemeliharaan. Silakan hubungi kami kembali nanti.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(text = "Oke", color = Color(0xFF3CB7A6), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun LabTestCardItem(test: LabTest, onPesanClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPesanClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp))) {
            // Background decoration shape at top right
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .offset(x = 64.dp, y = (-48).dp)
                    .background(Color(0xFFFAFAFA), CircleShape)
                    .align(Alignment.TopEnd)
            )

            // Content Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(test.iconBg, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = test.icon,
                            contentDescription = test.title,
                            tint = test.iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = test.category,
                            color = Color(0xFF6B7280),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = test.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = test.description,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF6B7280),
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Harga",
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = test.price,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onPesanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = test.buttonColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
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
    }
}

@Composable
fun PromoSpecialBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3EB3A2)),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "KATA BIJAK HARI INI",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Kesehatan Adalah Harta",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"Kesehatan yang baik bukanlah sesuatu yang dapat kita beli. Namun, sesuatu yang dapat menjadi tabungan yang sangat berharga.\"\n\n~ Anne Wilson Schaef",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun NeedHelpCard(onContactClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFE6F7F5), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HeadsetMic,
                    contentDescription = "Help",
                    tint = Color(0xFF3CB7A6),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Butuh Bantuan?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = "Berbicara dengan Customer Service",
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            OutlinedButton(
                onClick = onContactClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3CB7A6)),
                border = BorderStroke(1.5.dp, Color(0xFF3CB7A6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Hubungi Kami",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
