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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.viewmodel.HistoryViewModel
import org.ukrida.labvora.viewmodel.UserViewModel
import org.ukrida.labvora.ui.components.EducationDisclaimerBanner
import java.util.Calendar

import org.ukrida.labvora.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScheduleScreen(
    bookingViewModel: BookingViewModel,
    cartViewModel: CartViewModel = remember { CartViewModel() },
    historyViewModel: HistoryViewModel? = null,
    userViewModel: UserViewModel? = null,
    onBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToCart: () -> Unit = {}
) {
    val context = LocalContext.current
    val today = remember { Calendar.getInstance() }
    val todayYear = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    val userId = userViewModel?.currentUser?.value?.id ?: 0
    LaunchedEffect(userId) {
        if (userId > 0) {
            historyViewModel?.getHistoryList(userId)
        }
    }

    val activeOrderCount = historyViewModel?.activeOrderCount ?: 0
    val remainingQuota = historyViewModel?.remainingOrderQuota ?: 3
    var showMaxOrdersDialog by remember { mutableStateOf(false) }

    var currentYear by remember { mutableStateOf(todayYear) }
    var currentMonth by remember { mutableStateOf(todayMonth) } 
    var selectedDay by remember { mutableStateOf(todayDay) } 

    val minYear = todayYear
    val minMonth = todayMonth

    val clinics = listOf(
        "Klinik Labvora PIK",
        "Klinik Labvora Kebon Jeruk",
        "Klinik Labvora Menteng",
        "Klinik Labvora Bintaro"
    )
    var dropdownExpanded by remember { mutableStateOf(false) }

    val monthsIndo = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    val dayNamesIndo = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    // Helper to calculate Indonesian formatted date string
    fun getFormattedDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return "${dayNamesIndo[dayOfWeek - 1]}, $day ${monthsIndo[month]} $year"
    }

    // Set default date state on launch
    LaunchedEffect(currentYear, currentMonth, selectedDay) {
        bookingViewModel.selectedDate = getFormattedDate(currentYear, currentMonth, selectedDay)
    }

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Jadwal Pemeriksaan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
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

            // Monthly Calendar Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Calendar Month/Year Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isPrevDisabled = (currentYear == minYear && currentMonth == minMonth)
                        IconButton(
                            onClick = {
                                if (!isPrevDisabled) {
                                    currentMonth--
                                    if (currentMonth < 0) {
                                        currentMonth = 11
                                        currentYear--
                                    }
                                }
                            },
                            enabled = !isPrevDisabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Bulan Sebelumnya",
                                tint = if (isPrevDisabled) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                            )
                        }
                        Text(
                            text = "${monthsIndo[currentMonth]} $currentYear",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937)
                        )
                        IconButton(
                            onClick = {
                                currentMonth++
                                if (currentMonth > 11) {
                                    currentMonth = 0
                                    currentYear++
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Bulan Selanjutnya",
                                tint = Color(0xFF4B5563)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Days of Week Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val daysHeader = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                        daysHeader.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
                    val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
                    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                    val prevMonthCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth - 1)
                    }
                    val maxDaysPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                    var dayCounter = 1
                    var nextMonthDayCounter = 1

                    // 6 Rows max for calendar grid
                    for (row in 0 until 6) {
                        if (dayCounter > maxDays) break
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                if (cellIndex < offset) {
                                    // Prev Month placeholders
                                    val prevDay = maxDaysPrevMonth - (offset - cellIndex - 1)
                                    Text(
                                        text = "$prevDay",
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD1D5DB)
                                    )
                                } else if (dayCounter <= maxDays) {
                                    val day = dayCounter
                                    val isSelected = (day == selectedDay)
                                    val isPastDay = (currentYear == todayYear && currentMonth == todayMonth && day < todayDay)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) Color(0xFF3CB7A6) else Color.Transparent
                                            )
                                            .clickable(enabled = !isPastDay) {
                                                selectedDay = day
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$day",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isSelected -> Color.White
                                                isPastDay -> Color(0xFFD1D5DB)
                                                else -> Color(0xFF1F2937)
                                            }
                                        )
                                    }
                                    dayCounter++
                                } else {
                                    // Next month placeholders
                                    val nextDay = nextMonthDayCounter
                                    Text(
                                        text = "$nextDay",
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD1D5DB)
                                    )
                                    nextMonthDayCounter++
                                }
                            }
                        }
                    }
                }
            }

            // Time Slots Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section: Pagi (Sunrise)
                TimeSlotSection(
                    title = "Pagi",
                    icon = Icons.Outlined.WbTwilight,
                    slots = listOf("08:00", "09:00", "10:00", "11:00"),
                    selectedSlot = bookingViewModel.selectedTimeSlot,
                    onSlotSelected = { bookingViewModel.selectedTimeSlot = it }
                )

                // Section: Siang (Matahari)
                TimeSlotSection(
                    title = "Siang",
                    icon = Icons.Outlined.WbSunny,
                    slots = listOf("13:00", "14:00", "15:00", "16:00"),
                    selectedSlot = bookingViewModel.selectedTimeSlot,
                    onSlotSelected = { bookingViewModel.selectedTimeSlot = it }
                )

                // Section: Malam (Bulan)
                TimeSlotSection(
                    title = "Malam",
                    icon = Icons.Outlined.DarkMode,
                    slots = listOf("18:00", "19:00", "20:00"),
                    selectedSlot = bookingViewModel.selectedTimeSlot,
                    onSlotSelected = { bookingViewModel.selectedTimeSlot = it }
                )
            }

            // Clinic Dropdown Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "PILIH LOKASI KLINIK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bookingViewModel.selectedClinic,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        clinics.forEach { clinic ->
                            DropdownMenuItem(
                                text = { Text(clinic, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    bookingViewModel.selectedClinic = clinic
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Action Buttons: Tambah ke Keranjang & Konfirmasi Lokasi/Jadwal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeOrderCount >= 3) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Batas Pemesanan Tercapai (3/3 Aktif)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Anda memiliki 3 pesanan aktif yang belum selesai. Anda hanya dapat memesan lagi setelah ada pesanan yang selesai.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (activeOrderCount >= 3) {
                            showMaxOrdersDialog = true
                            return@OutlinedButton
                        }
                        bookingViewModel.selectedTest?.let { test ->
                            cartViewModel.addToCart(
                                test = test,
                                clinicName = bookingViewModel.selectedClinic,
                                bookingDate = bookingViewModel.selectedDate,
                                bookingTime = bookingViewModel.selectedTimeSlot,
                                hasDoctorReferral = bookingViewModel.hasDoctorReferral,
                                context = context
                            )
                            onNavigateToCart()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF3CB7A6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "+ Tambah ke Keranjang Saya",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3CB7A6)
                    )
                }

                Button(
                    onClick = {
                        if (activeOrderCount >= 3) {
                            showMaxOrdersDialog = true
                            return@Button
                        }
                        onNavigateToReview()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Konfirmasi Lokasi & Jadwal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                EducationDisclaimerBanner(
                    text = stringResource(org.ukrida.labvora.R.string.disclaimer_schedule)
                )
            }
        }
    }

    if (showMaxOrdersDialog) {
        AlertDialog(
            onDismissRequest = { showMaxOrdersDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Batas Pemesanan Tercapai",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Setiap pengguna hanya dapat memiliki maksimal 3 pesanan aktif yang belum diselesaikan. Anda baru dapat memesan kembali jika salah satu pesanan Anda telah diselesaikan.",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showMaxOrdersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mengerti", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun TimeSlotSection(
    title: String,
    icon: ImageVector = Icons.Outlined.WbSunny,
    slots: List<String>,
    selectedSlot: String,
    onSlotSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF374151),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151)
            )
        }

        // 3 Column Grid for slots
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val chunkedSlots = slots.chunked(3)
            chunkedSlots.forEach { rowSlots ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSlots.forEach { slot ->
                        val isSelected = (slot == selectedSlot)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(0xFFE6F7F5) else Color.White
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF3CB7A6) else Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSlotSelected(slot) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = slot,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF2C8A7D) else Color(0xFF4B5563)
                            )
                        }
                    }
                    // Placeholders to maintain column sizing if row is incomplete
                    if (rowSlots.size < 3) {
                        for (i in 0 until (3 - rowSlots.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
