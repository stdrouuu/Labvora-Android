package org.ukrida.labvora.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import org.ukrida.labvora.ui.components.EducationDisclaimerBanner
import org.ukrida.labvora.ui.components.EducationDisclaimerFooter
import org.ukrida.labvora.util.SeenOrderStore
import org.ukrida.labvora.ui.components.displayClinicName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ukrida.labvora.data.model.TestHistoryItem
import org.ukrida.labvora.viewmodel.HistoryViewModel
import org.ukrida.labvora.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    userViewModel: UserViewModel,
    historyViewModel: HistoryViewModel,
    onBack: () -> Unit,
    onNavigateToListTest: () -> Unit
) {
    val userId = userViewModel.currentUser.value?.id ?: 0

    LaunchedEffect(userId) {
        if (userId > 0) {
            historyViewModel.getHistoryList(userId)
        }
    }

    val pendingOrders = historyViewModel.pendingOrders.value
    val historyList = historyViewModel.historyList.value

    // Gabungkan semua pesanan (pending + selesai) untuk filtering lengkap
    val allOrders = remember(pendingOrders, historyList) {
        (pendingOrders + historyList).distinctBy { it.id }
    }

    var selectedStatus by remember { mutableStateOf("Semua") }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var isNewestFirst by remember { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                selectedDate = formatted
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val filteredSorted = remember(allOrders, selectedStatus, selectedDate, isNewestFirst) {
        val selectedIso = selectedDate?.let { normalizeDateToISO(it) }
        val filtered = allOrders.filter { item ->
            val statusMatch = selectedStatus == "Semua" || item.status.equals(selectedStatus, ignoreCase = true)
            val dateMatch = if (selectedIso == null) true else {
                normalizeDateToISO(item.date) == selectedIso
            }
            statusMatch && dateMatch
        }
        // ponytail: urut-align id booking — booking terbaru (id terbesar) selalu paling atas
        val sorted = filtered.sortedBy { it.id }
        if (isNewestFirst) sorted.reversed() else sorted
    }

    // Stripe/pill "baru" dihitung live dari prefs tiap data berubah, supaya
    // pesanan yang datang telat (fetch selesai setelah halaman kebuka) tetap
    // dapat pill. Ditandai dilihat saat keluar halaman — kunjungan berikutnya
    // hilang, sekaligus badge Beranda ikut nol.
    // fresh = pesanan baru + pesanan yang statusnya pindah (stripe).
    // new = belum pernah dilihat sama sekali (pill "Terbaru dipesan").
    // Jika highlightAboveId terisi (dibuka dari modal habis memesan), hanya
    // order yang id-nya di atas batas itu yang ditandai — pill order
    // terbaru sebelumnya tidak ikut muncul.
    val seenTick = historyViewModel.orderSeenVersion.value
    val highlightAbove = historyViewModel.highlightAboveId.value
    val freshIds = remember(allOrders, userId, seenTick, highlightAbove) {
        val fresh = SeenOrderStore.getFreshIds(context, userId, allOrders)
        if (highlightAbove != null) fresh.filter { it > highlightAbove } else fresh
    }
    val newIds = remember(allOrders, userId, seenTick, highlightAbove) {
        val fresh = SeenOrderStore.getNewIds(context, userId, allOrders)
        if (highlightAbove != null) fresh.filter { it > highlightAbove } else fresh
    }
    val latestOrders = rememberUpdatedState(allOrders)
    DisposableEffect(userId) {
        onDispose {
            if (userId > 0 && latestOrders.value.isNotEmpty()) {
                SeenOrderStore.markSeenItems(context, userId, latestOrders.value)
                historyViewModel.highlightAboveId.value = null
                historyViewModel.orderSeenVersion.value++
            }
        }
    }

    val statusTabs = listOf("Semua", "Menunggu", "Dikonfirmasi", "Sedang diuji", "Selesai", "Dibatalkan")

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Status Pesanan Saya",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.border(0.5.dp, Color(0xFFF3F4F6))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(paddingValues)
        ) {
            if (allOrders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Color(0xFFE6F7F5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Belum Ada Pesanan",
                            tint = Color(0xFF3CB7A6),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Belum Ada Pesanan Aktif",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saat ini Anda tidak memiliki transaksi pemeriksaan laboratorium yang sedang diproses.",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToListTest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Cari Tes Lab Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ===== FILTER CHIPS : STATUS =====
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(statusTabs) { tab ->
                            val isSelected = selectedStatus == tab
                            val bg = if (isSelected) Color(0xFF3CB7A6) else Color.White
                            val fg = if (isSelected) Color.White else Color(0xFF6B7280)
                            val borderColor = if (isSelected) Color(0xFF3CB7A6) else Color(0xFFE5E7EB)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bg)
                                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                    .clickable { selectedStatus = tab }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = fg
                                )
                            }
                        }
                    }

                    // ===== ROW: PILIH TANGGAL + SORT =====
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date filter field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, if (selectedDate != null) Color(0xFF3CB7A6) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Tanggal",
                                        tint = if (selectedDate != null) Color(0xFF3CB7A6) else Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedDate?.let { formatDateDisplay(it) } ?: "Pilih tanggal",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedDate != null) Color(0xFF1F2937) else Color(0xFF9CA3AF)
                                    )
                                }
                                if (selectedDate != null) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus tanggal",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .clickable { selectedDate = null }
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }

                        // Sort dropdown button — single neutral icon
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Urutkan",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isNewestFirst) "Tes Terbaru" else "Tes Terlama",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF374151)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            val active = isNewestFirst
                                            Text(
                                                "Tes Terbaru",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (active) Color(0xFF3CB7A6) else Color(0xFF1F2937)
                                            )
                                            Text("Tes terbaru akan diurutkan ke atas", fontSize = 10.sp, color = if (active) Color(0xFF3CB7A6).copy(alpha = 0.7f) else Color(0xFF9CA3AF))
                                        }
                                    },
                                    onClick = {
                                        isNewestFirst = true
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            val active = !isNewestFirst
                                            Text(
                                                "Tes Terlama",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (active) Color(0xFF3CB7A6) else Color(0xFF1F2937)
                                            )
                                            Text("Tes terlama akan diurutkan ke atas", fontSize = 10.sp, color = if (active) Color(0xFF3CB7A6).copy(alpha = 0.7f) else Color(0xFF9CA3AF))
                                        }
                                    },
                                    onClick = {
                                        isNewestFirst = false
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    EducationDisclaimerBanner(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // ===== LIST =====
                    if (filteredSorted.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 24.dp)
                                    .offset(y = (-32).dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFFF3F4F6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tidak ada pesanan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tidak ditemukan pesanan dengan status \"$selectedStatus\"" + if (selectedDate != null) " pada ${formatDateDisplay(selectedDate!!)}" else "" + ".",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedStatus = "Semua"
                                    selectedDate = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3CB7A6)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3CB7A6))
                            ) {
                                Text("Lihat Semua Pesanan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(filteredSorted) { _, order ->
                                OrderStatusCard(
                                    order = order,
                                    isNew = freshIds.contains(order.id),
                                    showNewPill = newIds.contains(order.id),
                                    showUpdatedPill = freshIds.contains(order.id) && !newIds.contains(order.id)
                                )
                            }
                            item {
                                EducationDisclaimerFooter()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun monthNameToNumber(token: String): String? {
    return when (token.lowercase(Locale("id", "ID")).trim()) {
        "januari", "jan", "january" -> "01"
        "februari", "feb", "february" -> "02"
        "maret", "mar", "march" -> "03"
        "april", "apr" -> "04"
        "mei", "may" -> "05"
        "juni", "jun", "june" -> "06"
        "juli", "jul", "july" -> "07"
        "agustus", "agu", "aug", "august" -> "08"
        "september", "sep", "sept" -> "09"
        "oktober", "okt", "oct", "october" -> "10"
        "november", "nov" -> "11"
        "desember", "des", "dec", "december" -> "12"
        else -> null
    }
}

// Normalisasi semua format tanggal API ke ISO yyyy-MM-dd untuk perbandingan hari.
// Mendukung: "2026-06-26", "26/06/2026", "26 Juni 2026", "26 Jun 2026",
// "Jumat, 26 Juni 2026", "Jumat, 26 Jun 2026", plus varian EN.
private fun normalizeDateToISO(raw: String): String? {
    if (raw.isBlank()) return null
    // Buang prefix nama hari ("Jumat, ...") dan suffix jam ("... · Jam 14:00", "... Jam ...")
    var s = raw.trim()
    if (s.contains(",")) s = s.substringAfterLast(",").trim()
    s = s.split("·")[0].trim()
    if (s.contains("Jam", ignoreCase = true)) s = s.substringBefore("Jam").trim()
    s = s.replace(",", " ").replace("\\s+".toRegex(), " ").trim()
    if (s.isEmpty()) return null

    // 1) ISO: yyyy-MM-dd atau yyyy/MM/dd (+ optional time di belakang)
    val isoMatch = Regex("(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})").find(s)
    if (s.matches(Regex("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}.*"))) {
        isoMatch?.let {
            val y = it.groupValues[1]
            val m = it.groupValues[2].padStart(2, '0')
            val d = it.groupValues[3].padStart(2, '0')
            return "$y-$m-$d"
        }
    }

    // 2) DMY: dd-MM-yyyy / dd/MM/yyyy / dd MMM yyyy / dd MMMM yyyy
    val parts = s.split(" ", "-", "/").map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.size >= 3) {
        val day = parts[0].filter { it.isDigit() }.padStart(2, '0')
        val monthToken = parts[1]
        val year = parts[2].filter { it.isDigit() }
        if (day.length == 2 && year.length == 4) {
            val monthNum = monthToken.filter { it.isDigit() }.padStart(2, '0').takeIf { it.length == 2 && it != "00" }
                ?: monthNameToNumber(monthToken)
            if (monthNum != null) return "$year-$monthNum-$day"
        }
    }
    return null
}

private fun parseDateMillis(dateStr: String): Long {
    if (dateStr.isBlank()) return 0L
    val iso = normalizeDateToISO(dateStr)
    if (iso != null) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.isLenient = false
            val d = sdf.parse(iso)
            if (d != null) return d.time
        } catch (_: Exception) {}
    }
    // Fallback: coba pola lama untuk format tak terduga
    val trimmed = dateStr.trim()
    val patterns = listOf(
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "dd MMM yyyy",
        "dd MMMM yyyy",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss"
    )
    for (pat in patterns) {
        try {
            val sdf = SimpleDateFormat(pat, Locale("id", "ID"))
            sdf.isLenient = false
            val d = sdf.parse(trimmed)
            if (d != null) return d.time
        } catch (_: Exception) {}
        try {
            val sdfEn = SimpleDateFormat(pat, Locale.ENGLISH)
            sdfEn.isLenient = false
            val d = sdfEn.parse(trimmed)
            if (d != null) return d.time
        } catch (_: Exception) {}
    }
    return 0L
}

private fun formatDateDisplay(isoDate: String): String {
    val millis = parseDateMillis(isoDate)
    if (millis == 0L) return isoDate
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        sdf.format(java.util.Date(millis))
    } catch (_: Exception) {
        isoDate
    }
}

@Composable
fun OrderStatusCard(
    order: TestHistoryItem,
    isNew: Boolean = false,
    showNewPill: Boolean = false,
    showUpdatedPill: Boolean = false
) {
    val (statusBg, statusFg) = when (order.status) {
        "Menunggu" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "Dikonfirmasi" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "Sedang diuji" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
        "Dibatalkan" -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
        else -> Color(0xFFECFDF5) to Color(0xFF059669)
    }

    val displayTitle = if (order.title.isNotBlank()) order.title else order.testTitle

    Box(modifier = Modifier.fillMaxWidth()) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Booking ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "No. Booking #${order.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4B5563)
                    )
                    if (showNewPill || showUpdatedPill) {
                        Text(
                            text = if (showNewPill) "Terbaru dipesan" else "Status diperbarui",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier
                                .background(Color(0xFFF65C63), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = order.status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusFg,
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test Title
            Text(
                text = displayTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location & Schedule Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Klinik",
                    tint = Color(0xFF3CB7A6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayClinicName(order.clinicName),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Tanggal",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${order.date} · Jam ${if (!order.bookingTime.isNullOrBlank()) order.bookingTime else "14:00"}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            if (!order.referralPhoto.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Rujukan",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dengan Rujukan Dokter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }

            // Cancellation Banner
            if (order.status == "Dibatalkan") {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Dibatalkan",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pesanan Dibatalkan oleh Admin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        if (!order.cancelReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Alasan: ${order.cancelReason}",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Timeline Progress Bar (4 Steps)
                OrderStatusTimeline(currentStatus = order.status)
            }
        }
    }
        // Stripe hijau "baru dipesan" di sisi kanan kartu — hanya sekali lihat
        if (isNew) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(
                        Color(0xFF15803D),
                        RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                    )
            )
        }
    }
}

@Composable
fun OrderStatusTimeline(currentStatus: String) {
    val steps = listOf("Menunggu", "Dikonfirmasi", "Sedang diuji", "Selesai")
    val activeStepIndex = when (currentStatus) {
        "Menunggu" -> 0
        "Dikonfirmasi" -> 1
        "Sedang diuji" -> 2
        "Selesai" -> 3
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isPassed = index <= activeStepIndex
            val isCurrent = index == activeStepIndex

            val stepColor = if (isPassed) Color(0xFF3CB7A6) else Color(0xFFE5E7EB)
            val textColor = if (isPassed) Color(0xFF1F2937) else Color(0xFF9CA3AF)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (isCurrent) Color(0xFF3CB7A6) else if (isPassed) Color(0xFFE6F7F5) else Color(0xFFF3F4F6),
                            CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = stepColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = label,
                            tint = if (isCurrent) Color.White else Color(0xFF3CB7A6),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFD1D5DB), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
