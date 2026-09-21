// View: Layar Profil pengguna untuk melihat informasi akun dan riwayat aktivitas
package org.ukrida.labvora.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.ukrida.labvora.R
import org.ukrida.labvora.ui.components.EducationDisclaimerBanner
import org.ukrida.labvora.ui.components.EducationDisclaimerFooter
import org.ukrida.labvora.ui.components.displayClinicName
import org.ukrida.labvora.util.resolvePhotoModel
import org.ukrida.labvora.viewmodel.UserViewModel
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.viewmodel.HistoryViewModel
import java.io.File

import org.ukrida.labvora.ui.components.LabvoraPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    navController: NavHostController,
    bookingViewModel: BookingViewModel = remember { BookingViewModel() },
    historyViewModel: HistoryViewModel = remember { HistoryViewModel() },
    onNavigateToHistory: () -> Unit = {},
    onNavigateToOrderStatus: () -> Unit = {},
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit = {}
) {
    val currentUser = viewModel.currentUser.value
    val userName = currentUser?.name ?: ""
    // ponytail: key = id + photo agar Coil ganti gambar saat pindah akun;
    // hanya file lokal / URL valid yang dipakai, path basi dari server -> default
    val userPhoto: Any = remember(currentUser?.id, currentUser?.photo) {
        when (val m = resolvePhotoModel(currentUser?.photo)) {
            null -> R.drawable.images
            is File -> m
            is String -> if (m.startsWith("http") || m.startsWith("content://") || m.startsWith("file://")) m else R.drawable.images
            else -> R.drawable.images
        }
    }
    val context = LocalContext.current

    var showImagePreview by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm2 by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    // ponytail: version name dibaca dari PackageManager, fallback ke BuildConfig manual
    val appVersion = remember(context) {
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.5"
        } catch (_: Exception) { "1.0.5" }
    }
    val isDeletingAccount = viewModel.isDeletingAccount.value
    // Ringan: spinner hanya saat profil benar-benar belum siap, bukan permanen.
    val isLoadingProfile = viewModel.isLoadingProfile.value
    val showProfileLoading = isLoadingProfile || currentUser == null
    var deleteErrorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            if (userId > 0) {
                historyViewModel.getHistoryList(userId)
            }
        }
    }

    // ponytail: toast hoisted dari Edit agar tetap tampil setelah popBackStack
    val showUpdatedToast = viewModel.showProfileUpdatedToast.value
    LaunchedEffect(showUpdatedToast) {
        if (showUpdatedToast) {
            kotlinx.coroutines.delay(3000)
            viewModel.showProfileUpdatedToast.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Scrollable Content
        LabvoraPullToRefreshBox(
            isRefreshing = historyViewModel.isRefreshing.value,
            onRefresh = {
                currentUser?.id?.let { userId ->
                    if (userId > 0) {
                        historyViewModel.getHistoryList(userId, forceRefresh = true)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 32.dp)
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    // Profile Avatar Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Edit icon on top-right
                        IconButton(
                            onClick = { navController.navigate("profileedit") },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profil",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Image (Clickable for preview)
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .shadow(2.dp, CircleShape)
                                    .clickable { showImagePreview = true }
                            ) {
                                AsyncImage(
                                    model = userPhoto,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.images),
                                    fallback = painterResource(id = R.drawable.images)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Name
                            Text(
                                text = userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1F2937),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                            // Ringan: 1 spinner standar saja, hanya saat profil belum keload.
                            if (showProfileLoading) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF3CB7A6),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Memuat profil...",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9CA3AF),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Member Badge
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFE6F7F5), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF3CB7A6),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "LABVORA MEMBER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF3CB7A6),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Aktivitas Section
                SectionTitle(text = "Aktivitas")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Column(modifier = Modifier.clip(RoundedCornerShape(20.dp))) {
                        // Status Pesanan
                        // Status Pesanan — driven by pendingOrders (not historyList)
                        val pendingOrders = historyViewModel.pendingOrders.value
                        val (badgeText, badgeColor, badgeTextColor) = when {
                            pendingOrders.isEmpty() -> {
                                Triple("Tidak Ada", Color(0xFFF3F4F6), Color(0xFF6B7280))
                            }
                            pendingOrders.size > 1 -> {
                                Triple("Lihat Semua", Color(0xFFE6F7F5), Color(0xFF3CAEA3))
                            }
                            else -> {
                                val status = pendingOrders.first().status
                                val bg = when (status) {
                                    "Menunggu" -> Color(0xFFFEF3C7)
                                    "Dikonfirmasi" -> Color(0xFFE0F2FE)
                                    "Sedang diuji" -> Color(0xFFF3E8FF)
                                    "Dibatalkan" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFF3F4F6)
                                }
                                val fg = when (status) {
                                    "Menunggu" -> Color(0xFFD97706)
                                    "Dikonfirmasi" -> Color(0xFF0369A1)
                                    "Sedang diuji" -> Color(0xFF6B21A8)
                                    "Dibatalkan" -> Color(0xFFEF4444)
                                    else -> Color(0xFF6B7280)
                                }
                                Triple(status, bg, fg)
                            }
                        }

                        ProfileMenuItem(
                            icon = Icons.Default.Inventory,
                            title = "Status Pesanan",
                            badgeText = badgeText,
                            badgeColor = badgeColor,
                            badgeTextColor = badgeTextColor,
                            onClick = { onNavigateToOrderStatus() }
                        )
                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
                        // Riwayat Pesanan — only shows completed orders via history page
                        ProfileMenuItem(
                            icon = Icons.Default.ReceiptLong,
                            title = "Riwayat Pemeriksaan",
                            onClick = { onNavigateToHistory() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Informasi Section
                SectionTitle(text = "Informasi")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Column(modifier = Modifier.clip(RoundedCornerShape(20.dp))) {
                        ProfileMenuItem(
                            icon = Icons.Default.Person,
                            title = "Edit Profil",
                            onClick = {
                                navController.navigate("profileedit")
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
                        ProfileMenuItem(
                            icon = Icons.Default.VerifiedUser,
                            title = "Kebijakan Privasi",
                            isExternal = true,
                            onClick = {
                                // ponytail: external URL replaces in-app screen, no route needed
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://labvora.ifukrida.net/privacy-policy.html")))
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
                        ProfileMenuItem(
                            icon = Icons.Default.HelpOutline,
                            title = "Pertanyaan yang Sering Ditanyakan",
                            onClick = {
                                navController.navigate("faq")
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "Tentang Aplikasi",
                            subtitle = "Versi $appVersion",
                            onClick = {
                                showAboutDialog = true
                            }
                        )
                    }
                }

                // Spacer(modifier = Modifier.height(28.dp))

                // Bantuan Section (Disembunyikan sementara)
                /*
                SectionTitle(text = "Bantuan")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Pusat Bantuan",
                        onClick = {
                            showHelpDialog = true
                        }
                    )
                }
                */

                Spacer(modifier = Modifier.height(32.dp))

                // Keluar Akun Button — double verify tema merah
                Button(
                    onClick = { showLogoutDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF86066),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0xFFF86066))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "KELUAR AKUN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ======= Hapus Akun Button =======
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "HAPUS AKUN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                // Error pesan jika hapus gagal
                deleteErrorMsg?.let { errMsg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errMsg,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                EducationDisclaimerFooter(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            }
        }

    // Toast sukses edit — ponytail: di bawah top nav, overlay konten
    if (showUpdatedToast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .background(Color(0xFF3CB7A6), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Profil berhasil diperbarui!",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        }
    }

    // ================= bigger preview dialog =================
    if (showImagePreview) {
        Dialog(onDismissRequest = { showImagePreview = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = userPhoto,
                        contentDescription = "Avatar Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.images),
                        fallback = painterResource(id = R.drawable.images)
                    )
                    IconButton(
                        onClick = { showImagePreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    // ================= status pesanan dialog (Menunggu) =================
    if (showHistoryDialog) {
        val pendingOrders = historyViewModel.pendingOrders.value
        if (pendingOrders.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = {
                    Text(
                        text = "Status Pesanan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(pendingOrders) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "Tes Lab:",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                order.title,
                                                color = Color.Black,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "Klinik:",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                displayClinicName(order.clinicName),
                                                color = Color.Black,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "Jadwal:",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                order.date,
                                                color = Color.Black,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Status:",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                            val badgeColor = when (order.status) {
                                                "Menunggu" -> Color(0xFFFEF3C7)
                                                "Dikonfirmasi" -> Color(0xFFE0F2FE)
                                                "Sedang diuji" -> Color(0xFFF3E8FF)
                                                "Dibatalkan" -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFF3F4F6)
                                            }
                                            val badgeTextColor = when (order.status) {
                                                "Menunggu" -> Color(0xFFD97706)
                                                "Dikonfirmasi" -> Color(0xFF0369A1)
                                                "Sedang diuji" -> Color(0xFF6B21A8)
                                                "Dibatalkan" -> Color(0xFFEF4444)
                                                else -> Color(0xFF374151)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeColor, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = order.status,
                                                    color = badgeTextColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                        
                                        val statusDesc = when (order.status) {
                                            "Menunggu" -> "Pesanan Anda sedang menunggu konfirmasi dari klinik."
                                            "Dikonfirmasi" -> "Pesanan telah dikonfirmasi. Silakan datang ke klinik sesuai jadwal."
                                            "Sedang diuji" -> "Pemeriksaan sedang berlangsung di laboratorium."
                                            "Dibatalkan" -> "Pemeriksaan ini dibatalkan oleh klinik."
                                            else -> "Pemeriksaan selesai. Hasil dapat diakses di Riwayat."
                                        }
                                        
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = statusDesc,
                                            fontSize = 11.sp,
                                            color = Color(0xFF9CA3AF),
                                            lineHeight = 15.sp
                                        )

                                        if (order.status == "Dibatalkan" && !order.cancelReason.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                                    .border(BorderStroke(0.5.dp, Color(0xFFFCA5A5)), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Alasan Pembatalan:",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB91C1C)
                                                    )
                                                    Text(
                                                        text = order.cancelReason,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF991B1B),
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }
                                        }

                                        val hasReferral = !order.referralPhoto.isNullOrEmpty() && order.referralPhoto != "null"
                                        if (hasReferral) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFFEAEA), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Pengingat: Silakan bawa surat rujukan dokter fisik Anda saat mengunjungi klinik.",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFA6A71),
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHistoryDialog = false }) {
                        Text(text = "Tutup", color = Color(0xFF3CB7A6), fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
    // ================= help dialog =================
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

    // ================= tentang aplikasi dialog =================
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6F7F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.appicon),
                            contentDescription = "Logo Labvora",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Labvora",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Versi $appVersion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3CB7A6),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aplikasi pemesanan tes laboratorium untuk mempermudah pendaftaran, penjadwalan, dan pemantauan hasil pemeriksaan kesehatan Anda.",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    EducationDisclaimerBanner()
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dibuat oleh",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tim Labvora - Informatics Engineering - Universitas Kristen Krida Wacana (UKRIDA)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3CB7A6),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
                    ) {
                        Text("Tutup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ================= logout confirm dialog (double verify, tema merah, compact) =================
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
                            .background(Color(0xFFFEE2E2), CircleShape),
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
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari akun ini?",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
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

    // ================= delete account dialog – step 1 =================
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFFEE2E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Hapus Akun?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tindakan ini akan menghapus akun dan seluruh data Anda secara permanen dari sistem kami, termasuk:",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 20.sp
                    )
                    val bullets = listOf(
                        "Data profil & informasi akun",
                        "Riwayat pemeriksaan",
                        "Riwayat pesanan"
                    )
                    bullets.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFDC2626), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                fontSize = 13.sp,
                                color = Color(0xFF374151),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠ Data yang dihapus tidak dapat dipulihkan kembali.",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        showDeleteConfirm2 = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lanjutkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // ================= delete account dialog – step 2 (konfirmasi akhir) =================
    if (showDeleteConfirm2) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm2 = false },
            title = {
                Text(
                    text = "Konfirmasi Terakhir",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = "Apakah Anda benar-benar yakin ingin menghapus akun ini? Proses ini tidak dapat diurungkan.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm2 = false
                        deleteErrorMsg = null
                        viewModel.deleteAccount(
                            onSuccess = {
                                onDeleteAccount()
                            },
                            onError = { err ->
                                deleteErrorMsg = err
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Hapus Akun", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm2 = false }) {
                    Text("Batal", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // ================= loading dialog saat hapus akun =================
    if (isDeletingAccount) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFDC2626),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Menghapus akun...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1F2937),
        letterSpacing = 1.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, bottom = 12.dp)
    )
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = Color.Transparent,
    badgeTextColor: Color = Color.Transparent,
    isExternal: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE6F7F5), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF3CB7A6),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title (+ optional subtitle, e.g. version under Tentang Aplikasi)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        // Badge if present
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeTextColor,
                    letterSpacing = 0.5.sp
                )
            }
        } else if (isExternal) {
            // Outside-link icon beside external items (e.g. Kebijakan Privasi)
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        } else {
            // Chevron arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
