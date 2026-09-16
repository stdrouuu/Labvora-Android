package org.ukrida.labvora.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.ukrida.labvora.data.model.AdminBooking
import org.ukrida.labvora.viewmodel.AdminViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(
    viewModel: AdminViewModel,
    navController: NavController,
    onLogout: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.getBookings()
    }
    // Reset isi search bar saat ganti page via bottom nav (jangan simpan permanen)
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { viewModel.clearSearch() }
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val filteredBookings = viewModel.filterBookings()

    // Date picker setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = "$year-${(month + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                viewModel.searchDate.value = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pemesanan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A2E35)
                        )
                        Text(
                            text = "Kelola validasi status & pendaftaran",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFFF788F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            AdminBottomNav(navController = navController, currentRoute = "admin-order")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7F6))
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search inputs panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Text Box
                    val searchInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    androidx.compose.foundation.text.BasicTextField(
                        value = viewModel.searchQuery.value,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = searchInteractionSource,
                        singleLine = true,
                        decorationBox = @Composable { innerTextField ->
                            OutlinedTextFieldDefaults.DecorationBox(
                                value = viewModel.searchQuery.value,
                                innerTextField = innerTextField,
                                enabled = true,
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                                interactionSource = searchInteractionSource,
                                placeholder = { Text("Cari nama pasien...", fontSize = 12.sp, color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Gray) },
                                trailingIcon = {
                                    if (viewModel.searchQuery.value.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color.Gray)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF42B5A7),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                ),
                                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
                                container = {
                                    OutlinedTextFieldDefaults.Container(
                                        enabled = true,
                                        isError = false,
                                        interactionSource = searchInteractionSource,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF42B5A7),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color(0xFFF8FAFC),
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        focusedBorderThickness = 1.dp,
                                        unfocusedBorderThickness = 1.dp
                                    )
                                }
                            )
                        }
                    )

                    // Date Picker Box — seluruh area bisa diklik untuk buka kalender
                    val dateInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { datePickerDialog.show() }
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = viewModel.searchDate.value,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = dateInteractionSource,
                            singleLine = true,
                            decorationBox = @Composable { innerTextField ->
                                OutlinedTextFieldDefaults.DecorationBox(
                                    value = viewModel.searchDate.value,
                                    innerTextField = innerTextField,
                                    enabled = false,
                                    singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                                    interactionSource = dateInteractionSource,
                                    placeholder = { Text("Pilih tanggal...", fontSize = 12.sp, color = Color.Gray) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = "Kalender",
                                            tint = Color.Gray
                                        )
                                    },
                                    trailingIcon = {
                                        if (viewModel.searchDate.value.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.searchDate.value = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color.Gray)
                                            }
                                        }
                                    },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF42B5A7),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                ),
                                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
                                container = {
                                    OutlinedTextFieldDefaults.Container(
                                        enabled = true,
                                        isError = false,
                                        interactionSource = dateInteractionSource,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF42B5A7),
                                            unfocusedBorderColor = Color(0xFFE2E8F0),
                                            focusedContainerColor = Color(0xFFF8FAFC),
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        focusedBorderThickness = 1.dp,
                                        unfocusedBorderThickness = 1.dp
                                    )
                                }
                            )
                        }
                    )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Filter Tabs
            val tabs = listOf("Semua", "Menunggu", "Dikonfirmasi", "Sedang diuji", "Selesai", "Dibatalkan")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(tabs) { tabName ->
                    val selected = viewModel.activeStatusFilter.value == tabName
                    val bg = if (selected) Color(0xFF42B5A7) else Color.White
                    val fg = if (selected) Color.White else Color(0xFF64748B)
                    val elevation = if (selected) 2.dp else 0.5.dp

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(elevation)
                            .background(bg)
                            .clickable { viewModel.activeStatusFilter.value = tabName }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = fg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Booking Count Header
            Text(
                text = "${filteredBookings.size} total dari filter (${viewModel.activeStatusFilter.value})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // Bookings List
            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Empty",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada pesanan yang cocok.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredBookings) { booking ->
                        OrderBookingCard(
                            booking = booking,
                            onDetailClick = { viewModel.selectBookingForDetail(booking) }
                        )
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFEE2E2), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFFF86066),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Keluar Akun?",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1F2937),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari akun admin ini?",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showLogoutDialog = false },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Batal", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF86066),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text("Ya, Keluar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Detail Booking Bottom Sheet (Material 3 ModalBottomSheet)
    if (viewModel.selectedBookingForDetail.value != null) {
        val booking = viewModel.selectedBookingForDetail.value!!
        val coroutineScope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

        ModalBottomSheet(
            onDismissRequest = { viewModel.selectBookingForDetail(null) },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Pemesanan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A2E35)
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                viewModel.selectBookingForDetail(null)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // Patient Information Details
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))),
                        width = 0.5.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow(label = "Pasien", value = booking.patientName, highlight = true)
                        DetailRow(label = "Jenis Kelamin", value = booking.gender)
                        DetailRow(label = "Tanggal Lahir", value = booking.dob)
                        DetailRow(label = "Alamat Lengkap", value = booking.address)
                        DetailRow(label = "Email", value = booking.email)
                        DetailRow(label = "Telepon", value = booking.phone)
                        DetailRow(label = "Tes", value = booking.testName, valueColor = Color(0xFF42B5A7), highlight = true)
                        DetailRow(label = "Klinik Cabang", value = booking.clinicName, highlight = true)
                        DetailRow(label = "Jadwal", value = "${booking.date} · ${booking.time}", highlight = true)
                    }
                }

                // Update Status Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PERBARUI STATUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    // 5 status choices grid
                    val statuses = listOf("Menunggu", "Dikonfirmasi", "Sedang diuji", "Selesai", "Dibatalkan")
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusOptionButton(
                                statusName = "Menunggu",
                                activeStatus = viewModel.selectedStatusValue.value,
                                originalStatus = booking.status,
                                onSelect = { viewModel.updateSelectedStatusValue("Menunggu") },
                                modifier = Modifier.weight(1f)
                            )
                            StatusOptionButton(
                                statusName = "Dikonfirmasi",
                                activeStatus = viewModel.selectedStatusValue.value,
                                originalStatus = booking.status,
                                onSelect = { viewModel.updateSelectedStatusValue("Dikonfirmasi") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusOptionButton(
                                statusName = "Sedang diuji",
                                activeStatus = viewModel.selectedStatusValue.value,
                                originalStatus = booking.status,
                                onSelect = { viewModel.updateSelectedStatusValue("Sedang diuji") },
                                modifier = Modifier.weight(1f)
                            )
                            StatusOptionButton(
                                statusName = "Selesai",
                                activeStatus = viewModel.selectedStatusValue.value,
                                originalStatus = booking.status,
                                onSelect = { viewModel.updateSelectedStatusValue("Selesai") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusOptionButton(
                                statusName = "Dibatalkan",
                                activeStatus = viewModel.selectedStatusValue.value,
                                originalStatus = booking.status,
                                onSelect = { viewModel.updateSelectedStatusValue("Dibatalkan") },
                                modifier = Modifier.weight(0.5f)
                            )
                            Spacer(modifier = Modifier.weight(0.5f))
                        }
                    }
                }

                val originalStatus = booking.status
                val selectedStatus = viewModel.selectedStatusValue.value

                if (selectedStatus == "Dibatalkan") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ALASAN PEMBATALAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = viewModel.cancelReasonInput.value,
                        onValueChange = { viewModel.cancelReasonInput.value = it },
                        placeholder = { Text("Tuliskan alasan pembatalan...", fontSize = 12.sp, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFFEE2E2).copy(alpha = 0.2f),
                            unfocusedContainerColor = Color(0xFFFEE2E2).copy(alpha = 0.2f)
                        ),
                        maxLines = 2
                    )
                }

                if (originalStatus == "Sedang diuji") {
                    WarningBox(message = "Untuk merilis hasil pemeriksaan ini, silakan input hasil lab melalui menu Input Hasil Lab.")
                } else if (originalStatus == "Selesai") {
                    WarningBox(message = "Pemeriksaan lab ini telah selesai dirilis.", isSuccess = true)
                } else if (originalStatus == "Dibatalkan") {
                    WarningBox(message = "Pemeriksaan lab ini telah dibatalkan.")
                }

                // Actions Button
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveStatusChange() },
                        enabled = (originalStatus == "Menunggu" || originalStatus == "Dikonfirmasi") && 
                                  (selectedStatus != originalStatus) && 
                                  (selectedStatus != "Dibatalkan" || viewModel.cancelReasonInput.value.trim().isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF42B5A7),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Simpan Perubahan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((originalStatus == "Menunggu" || originalStatus == "Dikonfirmasi") && 
                                       (selectedStatus != originalStatus) && 
                                       (selectedStatus != "Dibatalkan" || viewModel.cancelReasonInput.value.trim().isNotEmpty())) 
                                           Color.White else Color(0xFF94A3B8)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                viewModel.selectBookingForDetail(null)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))),
                            width = 1.dp
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Tutup", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderBookingCard(booking: AdminBooking, onDetailClick: () -> Unit) {
    val statusColor = when (booking.status) {
        "Menunggu" -> Color(0xFFF59E0B)
        "Dikonfirmasi" -> Color(0xFF3B82F6)
        "Sedang diuji" -> Color(0xFF8B5CF6)
        "Selesai" -> Color(0xFF10B981)
        else -> Color(0xFFEF4444)
    }

    val statusBg = when (booking.status) {
        "Menunggu" -> Color(0xFFFEF3C7)
        "Dikonfirmasi" -> Color(0xFFEFF6FF)
        "Sedang diuji" -> Color(0xFFF5F3FF)
        "Selesai" -> Color(0xFFD1FAE5)
        else -> Color(0xFFFEE2E2)
    }

    val resultBadgeBg = if (booking.status == "Selesai" || booking.resultStatus == "Hasil Siap") Color(0xFFD1FAE5) else Color(0xFFF1F5F9)
    val resultBadgeColor = if (booking.status == "Selesai" || booking.resultStatus == "Hasil Siap") Color(0xFF10B981) else Color(0xFF94A3B8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.patientName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2E35)
                    )
                    Text(
                        text = "${booking.testName} · ${booking.clinicName}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = booking.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .drawWithBorderAbove(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${booking.date} · ${booking.time}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(resultBadgeBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = booking.resultStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = resultBadgeColor
                    )
                }
            }

            Button(
                onClick = onDetailClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42B5A7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lihat Detail", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

private fun Modifier.drawWithBorderAbove() = this.then(
    Modifier
        .padding(top = 8.dp)
        .border(
            width = 0.5.dp,
            color = Color(0xFFF1F5F9)
        )
        .padding(top = 8.dp)
)

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color(0xFF475569), highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = valueColor,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold,
            modifier = Modifier.weight(0.6f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatusOptionButton(
    statusName: String,
    activeStatus: String,
    originalStatus: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClickable = when (originalStatus) {
        "Menunggu" -> statusName == "Dikonfirmasi" || statusName == "Dibatalkan" || statusName == "Menunggu"
        "Dikonfirmasi" -> statusName == "Sedang diuji" || statusName == "Dikonfirmasi"
        else -> false
    }

    val isGreyedOut = !isClickable && statusName != originalStatus
    val selected = activeStatus == statusName

    val activeColor = when (statusName) {
        "Menunggu" -> Color(0xFFF59E0B)
        "Dikonfirmasi" -> Color(0xFF3B82F6)
        "Sedang diuji" -> Color(0xFF8B5CF6)
        "Selesai" -> Color(0xFF10B981)
        else -> Color(0xFFEF4444)
    }

    val activeBg = when (statusName) {
        "Menunggu" -> Color(0xFFFEF3C7)
        "Dikonfirmasi" -> Color(0xFFEFF6FF)
        "Sedang diuji" -> Color(0xFFF5F3FF)
        "Selesai" -> Color(0xFFD1FAE5)
        else -> Color(0xFFFEE2E2)
    }

    val bg = when {
        isGreyedOut -> Color(0xFFF1F5F9)
        selected -> activeBg
        else -> Color(0xFFF8FAFC)
    }

    val border = when {
        isGreyedOut -> Color(0xFFE2E8F0)
        selected -> activeColor
        else -> Color(0xFFE2E8F0)
    }

    val fg = when {
        isGreyedOut -> Color(0xFFCBD5E1)
        selected -> activeColor
        else -> Color(0xFF94A3B8)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(enabled = isClickable) { onSelect() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusName,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
fun WarningBox(message: String, isSuccess: Boolean = false) {
    val bgColor = if (isSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val borderColor = if (isSuccess) Color(0xFFA7F3D0) else Color(0xFFFCA5A5)
    val textColor = if (isSuccess) Color(0xFF059669) else Color(0xFFDC2626)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = message,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
