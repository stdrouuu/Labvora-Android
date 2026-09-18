package org.ukrida.labvora.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.ui.components.EducationDisclaimerBanner
import org.ukrida.labvora.ui.components.EducationDisclaimerFooter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderReviewScreen(
    bookingViewModel: BookingViewModel,
    userViewModel: org.ukrida.labvora.viewmodel.UserViewModel,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToOrderStatus: () -> Unit = onNavigateToProfile
) {
    BackHandler(enabled = bookingViewModel.isOrderCompleted || bookingViewModel.showSuccessModal) {
        bookingViewModel.resetOrderState()
        onNavigateToHome()
    }

    var alertStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(bookingViewModel.showSuccessModal) {
        if (bookingViewModel.showSuccessModal) {
            alertStep = 1
        }
    }

    val test = bookingViewModel.selectedTest

    if (test == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF3CB7A6))
        }
        return
    }

    val clinicAddress = when (bookingViewModel.selectedClinic) {
        "Klinik Labvora PIK" -> "The Gallery Blok 8, No. EG,\nJl. Pantai Indah Utara"
        "Klinik Labvora Kebon Jeruk" -> "Jl. Panjang No. 18,\nKebon Jeruk, Jakarta Barat"
        "Klinik Labvora Menteng" -> "Jl. Teuku Cik Ditiro No. 25,\nMenteng, Jakarta Pusat"
        "Klinik Labvora Bintaro" -> "Ruko Kebayoran Arcade 1 Blok C1 No. 5,\nBintaro Jaya, Tangerang Selatan"
        else -> "The Gallery Blok 8, No. EG,\nJl. Pantai Indah Utara"
    }

    // Calculate time range, e.g. slot 09:00 -> "09:00 - 10:00 WIB"
    val timeRange = try {
        val hour = bookingViewModel.selectedTimeSlot.split(":")[0].toInt()
        val nextHour = String.format("%02d:00", hour + 1)
        "${bookingViewModel.selectedTimeSlot} - $nextHour WIB"
    } catch (e: Exception) {
        "${bookingViewModel.selectedTimeSlot} - End WIB"
    }

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detail Pemeriksaan Anda",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (bookingViewModel.isOrderCompleted) {
                            bookingViewModel.resetOrderState()
                            onNavigateToHome()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFAFAFA)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // Step Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF3CB7A6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .background(Color(0xFF3CB7A6))
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF3CB7A6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .background(Color(0xFF3CB7A6))
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF3CB7A6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Heading subtitle
                Text(
                    text = "Pastikan semua detail di bawah ini benar sebelum melanjutkan ke pembayaran.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                )
                EducationDisclaimerBanner(
                    text = stringResource(org.ukrida.labvora.R.string.disclaimer_schedule),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Main Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Title & package desc
                        Text(
                            text = test.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Paket pemeriksaan lengkap termasuk " + test.description,
                            fontSize = 9.sp,
                            color = Color(0xFF9CA3AF),
                            fontStyle = FontStyle.Italic,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Clinic Detail
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(18.dp)
                                    .offset(y = 1.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bookingViewModel.selectedClinic,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = clinicAddress,
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B7280),
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Schedule Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(18.dp)
                                    .offset(y = 1.dp)
                            )
                            Column {
                                Text(
                                    text = "Jadwal Pemeriksaan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = bookingViewModel.selectedDate,
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFFE6F7F5), RoundedCornerShape(12.dp))
                                        .border(0.5.dp, Color(0xFFCCF2ED), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF3CB7A6),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = timeRange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3CB7A6)
                                    )
                                }
                            }
                        }
                    }
                }

                // Referral Switch Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF4B5563),
                                modifier = Modifier
                                    .size(20.dp)
                                    .offset(y = 1.dp)
                            )
                            Column {
                                Text(
                                    text = "Memiliki rujukan dokter?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "Jangan lupa membawa surat rujukan anda\npada hari pemeriksaan.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = bookingViewModel.hasDoctorReferral,
                            onCheckedChange = { bookingViewModel.hasDoctorReferral = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3CB7A6),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                    }
                }

                // Billing Receipt Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Rincian Biaya",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Item test base check price (Total - 50.000)
                        val checkFee = if (test.priceVal > 50000) test.priceVal - 50000 else test.priceVal
                        val basePriceFormatted = String.format("%,d", checkFee).replace(",", ".")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Biaya Pengecekan (${test.title})",
                                fontSize = 10.sp,
                                color = Color(0xFF4B5563),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                text = basePriceFormatted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4B5563),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.3f),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Layanan & Administrasi fee
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Biaya Layanan & Administrasi",
                                fontSize = 10.sp,
                                color = Color(0xFF4B5563),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                text = "50.000",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4B5563),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.3f),
                                textAlign = TextAlign.End
                            )
                        }

                        // Dashed line
                        Spacer(modifier = Modifier.height(16.dp))
                        Canvas(modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)) {
                            drawLine(
                                color = Color(0xFFD1D5DB),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Total Price
                        val totalPriceFormatted = String.format("%,d", test.priceVal).replace(",", ".")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Harga",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Rp $totalPriceFormatted",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3CB7A6),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // CTA Button confirm
                EducationDisclaimerFooter(
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val isConfirming = bookingViewModel.isConfirmingOrder
                    val isCompleted = bookingViewModel.isOrderCompleted
                    Button(
                        onClick = {
                            if (!isConfirming && !isCompleted) {
                                val userId = userViewModel.currentUser.value?.id ?: 0
                                bookingViewModel.confirmOrder(userId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color(0xFF9CA3AF) else Color(0xFF3CB7A6)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .widthIn(min = 180.dp),
                        enabled = !isConfirming
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = when {
                                    isCompleted -> "Pesanan Dibuat!"
                                    isConfirming -> "Memproses..."
                                    else -> "Konfirmasi Pesanan"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isConfirming) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }

            // In-app success confirmation Toast Banner
            AnimatedVisibility(
                visible = bookingViewModel.showToastMessage,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF22C55E))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Pesanan Berhasil Dikonfirmasi!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Success Modal Popup Dialog (2-Step Alert)
    if (bookingViewModel.showSuccessModal) {
        Dialog(
            onDismissRequest = {
                bookingViewModel.resetOrderState()
                alertStep = 1
                onNavigateToHome()
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (alertStep == 1) {
                        // Alert 1: Terima Kasih!
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFE6F7F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                tint = Color(0xFF3CB7A6),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Terima Kasih!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pesanan Anda telah kami terima. Status pesanan dapat Anda pantau di halaman profil.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                alertStep = 2
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Lanjut (Petunjuk Pembayaran)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Alert 2: Petunjuk Pembayaran — abu-abu netral
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF3F4F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info Administrasi",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Petunjuk Pembayaran",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Silahkan ke klinik untuk melakukan pembayaran dan melengkapi kebutuhan administrasi di klinik.",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    bookingViewModel.resetOrderState()
                                    alertStep = 1
                                    onNavigateToOrderStatus()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Lihat Status Pesanan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    bookingViewModel.resetOrderState()
                                    alertStep = 1
                                    onNavigateToHome()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9FAFB)),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Tutup",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            TextButton(
                                onClick = {
                                    alertStep = 1
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Kembali ke Info Sebelumnya",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
