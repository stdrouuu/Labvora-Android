package org.ukrida.labvora.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.window.Dialog
import org.ukrida.labvora.data.model.AdminBooking
import org.ukrida.labvora.viewmodel.AdminViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.getBookings()
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Beranda",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A2E35)
                        )
                        Text(
                            text = "Juli 2026",
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
            AdminBottomNav(navController = navController, currentRoute = "admin-home")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7F6))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentDate = remember {
                val calendar = Calendar.getInstance()
                val dayNamesIndo = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
                val monthsIndo = listOf(
                    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                )
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                "${dayNamesIndo[dayOfWeek - 1]}, $day ${monthsIndo[month]} $year"
            }

            // Welcome Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF42B5A7), Color(0xFF35968A))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat datang kembali,",
                        fontSize = 11.sp,
                        color = Color(0xFFE0F2F1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Administrator",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = Color(0xFFB2DFDB),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stats Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Booking Card (Full Width)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.activeStatusFilter.value = "Semua"
                            navController.navigate("admin-order") {
                                popUpTo("admin-home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF0F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                    contentDescription = "Total",
                                    tint = Color(0xFFFF788F)
                                )
                            }
                            Column {
                                Text(
                                    text = "Total Pesanan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A2E35)
                                )
                                Text(
                                    text = "Akumulasi semua waktu",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Text(
                            text = viewModel.totalBookings.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A2E35)
                        )
                    }
                }

                // Grid of 4 smaller stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Waiting
                    StatGridItem(
                        title = "Menunggu",
                        value = viewModel.totalPending.toString(),
                        tint = Color(0xFFF59E0B),
                        backgroundColor = Color(0xFFFEF3C7),
                        icon = Icons.Default.HourglassEmpty,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.activeStatusFilter.value = "Menunggu"
                            navController.navigate("admin-order") {
                                popUpTo("admin-home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Antrean Hasil
                    StatGridItem(
                        title = "Antrean Hasil",
                        value = viewModel.totalResultQueue.toString(),
                        tint = Color(0xFF3B82F6),
                        backgroundColor = Color(0xFFEFF6FF),
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.activeStatusFilter.value = "Sedang diuji"
                            navController.navigate("admin-order") {
                                popUpTo("admin-home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Sedang Diuji
                    StatGridItem(
                        title = "Sedang Diuji",
                        value = viewModel.totalTesting.toString(),
                        tint = Color(0xFF8B5CF6),
                        backgroundColor = Color(0xFFF5F3FF),
                        icon = Icons.Default.Science,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.activeStatusFilter.value = "Sedang diuji"
                            navController.navigate("admin-order") {
                                popUpTo("admin-home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Selesai
                    StatGridItem(
                        title = "Selesai",
                        value = viewModel.totalSuccess.toString(),
                        tint = Color(0xFF10B981),
                        backgroundColor = Color(0xFFD1FAE5),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.activeStatusFilter.value = "Selesai"
                            navController.navigate("admin-order") {
                                popUpTo("admin-home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }


            // Recent Bookings (Pemesanan Terbaru)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pemesanan Terbaru",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2E35),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "Lihat Semua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF42B5A7),
                        modifier = Modifier
                            .clickable {
                                navController.navigate("admin-order") {
                                    popUpTo("admin-home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 4.dp)
                    )
                }

                // Show top 3 recent bookings
                val recentBookings = viewModel.bookings.value.take(3)
                recentBookings.forEach { booking ->
                    RecentBookingItem(booking = booking)
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
}

@Composable
fun StatGridItem(
    title: String,
    value: String,
    tint: Color,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A2E35)
                )
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentBookingItem(booking: AdminBooking) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.patientName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A2E35)
                )
                Text(
                    text = booking.testName,
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
    }
}

@Composable
fun AdminBottomNav(navController: NavController, currentRoute: String) {
    val items = listOf(
        Triple("admin-home", "Beranda", Icons.Default.Home),
        Triple("admin-order", "Pemesanan", Icons.AutoMirrored.Filled.Assignment),
        Triple("admin-input", "Input Hasil", Icons.Default.Science)
    )

    // Samakan dengan BottomNav user: background putih cover inset + navigationBarsPadding
    // agar tidak terpotong di device dengan gesture nav / tombol sistem.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E7EB))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, title, icon) ->
                val selected = currentRoute == route
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo("admin-home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (selected) Color(0xFF42B5A7) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            color = if (selected) Color(0xFF42B5A7) else Color(0xFF9CA3AF),
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
