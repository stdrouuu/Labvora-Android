package org.ukrida.labvora.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.ukrida.labvora.data.model.CartItem
import org.ukrida.labvora.ui.components.EducationDisclaimerBanner
import org.ukrida.labvora.ui.components.displayClinicName
import org.ukrida.labvora.viewmodel.CartViewModel
import org.ukrida.labvora.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToListTest: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToOrderStatus: () -> Unit = onNavigateToProfile
) {
    val context = LocalContext.current
    val cartItems = cartViewModel.cartItems.value
    val userId = userViewModel.currentUser.value?.id ?: 0

    var editingCartItem by remember { mutableStateOf<CartItem?>(null) }
    var deletingCartItem by remember { mutableStateOf<CartItem?>(null) }

    LaunchedEffect(cartViewModel.toastMessage.value) {
        cartViewModel.toastMessage.value?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            cartViewModel.clearToast()
        }
    }

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Keranjang Saya (${cartViewModel.cartItemCount})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "Pilih tes lab & atur jadwal pemeriksaan",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                // ponytail: divider flat sama kayak BottomNav, tanpa navigationBarsPadding
                // (inset ditangani BottomNav outer) agar tak ada strip abu dobel di atas nav
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE5E7EB))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                cartViewModel.toggleSelectAll(!cartViewModel.isAllChecked, context)
                            }
                        ) {
                            Checkbox(
                                checked = cartViewModel.isAllChecked,
                                onCheckedChange = { checked ->
                                    cartViewModel.toggleSelectAll(checked, context)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3CB7A6))
                            )
                            Text(
                                text = "Semua",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Harga",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = cartViewModel.totalPriceFormatted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF75F65)
                            )
                            if (cartViewModel.checkedCount > 0) {
                                Text(
                                    text = "(Termasuk Admin ${cartViewModel.adminFeeFormatted})",
                                    fontSize = 8.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                cartViewModel.checkoutCheckedItems(userId, context)
                            },
                            enabled = cartViewModel.checkedCount > 0 && !cartViewModel.isCheckingOut.value,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF75F65),
                                disabledContainerColor = Color(0xFFFDA4AF)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .widthIn(min = 130.dp)
                        ) {
                            if (cartViewModel.isCheckingOut.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Checkout (${cartViewModel.checkedCount})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(paddingValues)
        ) {
            if (cartItems.isEmpty()) {
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
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang Kosong",
                            tint = Color(0xFF3CB7A6),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Keranjang Anda Masih Kosong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jelajahi berbagai layanan pengetesan laboratorium dan tambahkan tes yang Anda butuhkan.",
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemCard(
                            item = item,
                            onToggleCheck = { cartViewModel.toggleItemChecked(item.id, context) },
                            onEditSchedule = { editingCartItem = item },
                            onRemove = { deletingCartItem = item }
                        )
                    }

                    if (cartViewModel.checkedCount > 0) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Rincian Biaya",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Biaya Pengecekan (${cartViewModel.checkedCount} item)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4B5563)
                                        )
                                        Text(
                                            text = cartViewModel.subtotalPriceFormatted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1F2937)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Biaya Layanan & Administrasi",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4B5563)
                                        )
                                        Text(
                                            text = cartViewModel.adminFeeFormatted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF3CB7A6)
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = Color(0xFFE5E7EB)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Total Pembayaran",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F2937)
                                        )
                                        Text(
                                            text = cartViewModel.totalPriceFormatted,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFF75F65)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "* Total sudah termasuk biaya pengecekan dan biaya administrasi Rp 50.000.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF6B7280),
                                        fontStyle = FontStyle.Italic
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EducationDisclaimerBanner(
                                        text = stringResource(org.ukrida.labvora.R.string.disclaimer_cart)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Edit Jadwal (Gambar 3)
    if (editingCartItem != null) {
        EditScheduleDialog(
            item = editingCartItem!!,
            onDismiss = { editingCartItem = null },
            onConfirm = { clinic, date, time ->
                cartViewModel.updateItemSchedule(editingCartItem!!.id, clinic, date, time, context)
                editingCartItem = null
            }
        )
    }

    // Konfirmasi Hapus Item Keranjang
    if (deletingCartItem != null) {
        AlertDialog(
            onDismissRequest = { deletingCartItem = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Hapus dari Keranjang?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus \"${deletingCartItem!!.test.title}\" dari keranjang?",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        cartViewModel.removeFromCart(deletingCartItem!!.id, context)
                        deletingCartItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF75F65)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deletingCartItem = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                }
            }
        )
    }

    var alertStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(cartViewModel.showCheckoutSuccessModal.value) {
        if (cartViewModel.showCheckoutSuccessModal.value) {
            alertStep = 1
        }
    }

    // Modal Success Checkout (2-Step Alert)
    if (cartViewModel.showCheckoutSuccessModal.value) {
        Dialog(
            onDismissRequest = {
                cartViewModel.resetSuccessModal()
                alertStep = 1
                onNavigateToHome()
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (alertStep == 1) {
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
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Pesanan Berhasil Dibuat!",
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
                                .background(Color(0xFFF3F4F6), CircleShape)
                                .border(1.dp, Color(0xFFE5E7EB), CircleShape),
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
                                    cartViewModel.resetSuccessModal()
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
                                    cartViewModel.resetSuccessModal()
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

@Composable
fun CartItemCard(
    item: CartItem,
    onToggleCheck: () -> Unit,
    onEditSchedule: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Checkbox + Test Image + Info + Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggleCheck() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3CB7A6)),
                    modifier = Modifier.padding(end = 4.dp)
                )

                // Test Image / Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(item.test.themeColorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = item.test.imageRes),
                        contentDescription = item.test.title,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Category & Price
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.test.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563),
                            modifier = Modifier
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus Item",
                                tint = Color(0xFFF75F65)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.test.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.test.price,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3CB7A6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Schedule & Location Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Klinik",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayClinicName(item.clinicName),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Tanggal",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.bookingDate,
                            fontSize = 10.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Waktu Pemeriksaan",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Jam ${item.bookingTime}",
                            fontSize = 10.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Edit Schedule Action Button (Gambar 3 Trigger)
                OutlinedButton(
                    onClick = onEditSchedule,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF3CB7A6)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Ubah Jadwal",
                        tint = Color(0xFF3CB7A6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ubah Jadwal",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3CB7A6)
                    )
                }
            }
        }
    }
}

// Modal Ubah Jadwal & Lokasi Klinik (Gambar 3)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onConfirm: (clinic: String, date: String, time: String) -> Unit
) {
    var tempTimeSlot by remember { mutableStateOf(item.bookingTime) }
    var tempClinic by remember { mutableStateOf(item.clinicName) }
    var tempDate by remember { mutableStateOf(item.bookingDate) }
    var isClinicDropdownExpanded by remember { mutableStateOf(false) }

    val clinics = listOf(
        "Klinik Labvora PIK",
        "Klinik Labvora Kebon Jeruk",
        "Klinik Labvora Menteng",
        "Klinik Labvora Bintaro"
    )

    val timeSlotsPagi = listOf("08:00", "09:00", "10:00", "11:00")
    val timeSlotsSiang = listOf("13:00", "14:00", "15:00", "16:00")
    val timeSlotsMalam = listOf("18:00", "19:00", "20:00")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Jadwal Pemeriksaan",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Slot Pagi (Sunrise) — outline konsisten
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WbTwilight,
                    contentDescription = "Pagi",
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pagi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlotsPagi.forEach { time ->
                    ScheduleTimeChip(
                        time = time,
                        isSelected = tempTimeSlot == time,
                        onClick = { tempTimeSlot = time },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slot Siang (Matahari)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = "Siang",
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Siang",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlotsSiang.forEach { time ->
                    ScheduleTimeChip(
                        time = time,
                        isSelected = tempTimeSlot == time,
                        onClick = { tempTimeSlot = time },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slot Malam (Bulan)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.DarkMode,
                    contentDescription = "Malam",
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Malam",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlotsMalam.forEach { time ->
                    ScheduleTimeChip(
                        time = time,
                        isSelected = tempTimeSlot == time,
                        onClick = { tempTimeSlot = time },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dropdown Lokasi Klinik
            Text(
                text = "PILIH LOKASI KLINIK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    onClick = { isClinicDropdownExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tempClinic,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray
                        )
                    }
                }

                DropdownMenu(
                    expanded = isClinicDropdownExpanded,
                    onDismissRequest = { isClinicDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    clinics.forEach { clinic ->
                        DropdownMenuItem(
                            text = { Text(clinic, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                tempClinic = clinic
                                isClinicDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Button Konfirmasi
            Button(
                onClick = {
                    onConfirm(tempClinic, tempDate, tempTimeSlot)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3CB7A6)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Konfirmasi Lokasi & Jadwal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ScheduleTimeChip(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE6F7F5) else Color.White)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF3CB7A6) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF3CB7A6) else Color(0xFF4B5563)
        )
    }
}
