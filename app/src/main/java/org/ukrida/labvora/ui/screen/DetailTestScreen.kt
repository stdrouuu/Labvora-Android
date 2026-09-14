package org.ukrida.labvora.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.ukrida.labvora.R
import org.ukrida.labvora.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTestScreen(
    testId: Int,
    bookingViewModel: BookingViewModel,
    onBack: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    // Sync the selected test in ViewModel
    LaunchedEffect(testId) {
         bookingViewModel.selectTestById(testId)
}

val test = bookingViewModel.selectedTest
var showDescriptionModal by remember { mutableStateOf(false) }

if (test == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF3CB7A6))
    }
    return
}

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detail Tes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937),
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1F2937)
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
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp),
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
                        .width(48.dp)
                        .height(2.dp)
                        .background(Color(0xFFDBEAFE))
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2", color = Color(0xFF4B5563), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(Color(0xFFDBEAFE))
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("3", color = Color(0xFF4B5563), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Title & Badges
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = test.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937),
                    lineHeight = 28.sp
                )
                Row(
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = test.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (test.isPuasa) "Wajib Puasa" else "Tanpa Puasa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }

            // Main Image — ponytail: rasio tetap agar tak gepeng di tablet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = test.imageRes),
                    contentDescription = "Sample Darah",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Quick Info Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duration
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFF9FAFB), RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = Color(0xFF3CB7A6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Durasi", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                    Text(test.duration, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                }

                // Biomarker
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFF9FAFB), RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = Color(0xFF3CB7A6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Biomarker", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                    Text("${test.biomarkerCount} Tes", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                }

                // Hasil
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFF9FAFB), RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = Color(0xFF3CB7A6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hasil", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                    Text(test.resultDuration, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                }
            }

            // Benefits Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(test.themeColorHex))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Manfaat Tes",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = test.benefits,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BACA DESKRIPSI LENGKAP >",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { showDescriptionModal = true }
                    )
                }
            }

            // Preparations Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .background(Color(0xFFF0F7F6), RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AssignmentLate,
                            contentDescription = null,
                            tint = Color(0xFF3CB7A6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Persiapan Pre-tes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3CB7A6)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    test.preparations.forEachIndexed { index, preparation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF3CB7A6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = preparation,
                                fontSize = 12.sp,
                                color = Color(0xFF374151),
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // CTA Button
            Button(
                onClick = onNavigateToSchedule,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .heightIn(min = 54.dp)
            ) {
                Text(
                    text = "Cek Jadwal Pemeriksaan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }

    // Slide-up Modal Bottom Sheet
    if (showDescriptionModal) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showDescriptionModal = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deskripsi Lengkap",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )
                    IconButton(onClick = { showDescriptionModal = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Content (Scrollable text)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = test.fullDescription,
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
