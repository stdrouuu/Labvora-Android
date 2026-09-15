package org.ukrida.labvora.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ukrida.labvora.R
import org.ukrida.labvora.util.copyUriToProfileFile
import org.ukrida.labvora.util.createProfilePhotoFile
import org.ukrida.labvora.util.deleteProfilePhotoFile
import org.ukrida.labvora.util.photoPathToUploadFile
import org.ukrida.labvora.util.resolvePhotoModel
import org.ukrida.labvora.viewmodel.UserViewModel
import androidx.navigation.NavHostController
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: UserViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = viewModel.currentUser.value

    var name by remember(currentUser?.id) { mutableStateOf(currentUser?.name ?: "") }
    var email by remember(currentUser?.id) { mutableStateOf(currentUser?.email ?: "") }
    var phone by remember(currentUser?.id) { mutableStateOf(currentUser?.phone ?: "") }
    var gender by remember(currentUser?.id) { mutableStateOf(if (currentUser?.gender == "P") "Perempuan" else if (currentUser?.gender == "L") "Laki-laki" else "Laki-laki") }
    var dob by remember(currentUser?.id) { mutableStateOf(currentUser?.dob ?: "") }
    var address by remember(currentUser?.id) { mutableStateOf(currentUser?.address ?: "") }
    
    // ponytail: path file lokal per akun; Uri galeri disalin agar tidak hilang antar sesi
    var photoPath by remember(currentUser?.id) { mutableStateOf(currentUser?.photo) }
    val photoModel = remember(photoPath) { resolvePhotoModel(photoPath) }
    // ponytail: Hapus hanya muncul bila ada foto custom (file lokal/URL valid),
    // bukan saat default images.jpg
    val hasCustomPhoto = remember(photoPath) {
        when (val m = resolvePhotoModel(photoPath)) {
            is File -> true
            is String -> m.startsWith("http") || m.startsWith("content://") || m.startsWith("file://")
            else -> false
        }
    }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    var isSaving by rememberSaveable { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showDeletePhotoConfirm by remember { mutableStateOf(false) }
    // ponytail: satu requester per field — field yang fokus auto-scroll ke viewport
    // di atas tombol Simpan (tombol sudah naik via imePadding di bottomBar).
    // Konten SENGAJA tanpa imePadding agar tidak dobel inset (sumber kolom
    // ketutup + gap putih raksasa saat keyboard terbuka).
    val scrollState = rememberScrollState()
    val nameBringRequester = remember { BringIntoViewRequester() }
    val emailBringRequester = remember { BringIntoViewRequester() }
    val phoneBringRequester = remember { BringIntoViewRequester() }
    val addressBringRequester = remember { BringIntoViewRequester() }

    // ================= GALERI =================
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val newPath = copyUriToProfileFile(context, it) ?: it.toString()
            if (newPath != photoPath) deleteProfilePhotoFile(photoPath)
            photoPath = newPath
        }
    }

    // ================= KAMERA =================
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // ponytail: path baru dipakai hanya jika sukses; batal = foto lama tetap.
        // File lama dihapus agar cuma satu foto tersimpan.
        if (success) {
            pendingCameraPath?.let {
                if (it != photoPath) deleteProfilePhotoFile(photoPath)
                photoPath = it
            }
        }
        pendingCameraPath = null
    }

    fun launchCamera() {
        val file = createProfilePhotoFile(context)
        pendingCameraPath = file.absolutePath
        cameraLauncher.launch(FileProvider.getUriForFile(context, "${context.packageName}.provider", file))
    }

    // ================= PERMISSION =================
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

    // ponytail: Scaffold mengunci header di atas + tombol di bawah agar
    // susunan tidak bisa bergeser; konten scroll di tengah
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            EditProfileTopBar(onBack = { navController.popBackStack() })
        },
        bottomBar = {
            EditProfileSaveBar(
                isSaving = isSaving,
                onSave = {
                    if (currentUser != null && !isSaving) {
                        isSaving = true
                        saveError = null
                        val updatedUser = currentUser.copy(
                            name = name,
                            email = email,
                            phone = phone,
                            gender = if (gender == "Perempuan") "P" else "L",
                            dob = dob,
                            address = address,
                            photo = photoPath
                        )
                        // Upload dulu bila foto masih file lokal -> DB simpan
                        // filename server agar sync antar device.
                        viewModel.updateProfileWithPhoto(
                            updatedUser,
                            photoPathToUploadFile(photoPath),
                            onSuccess = {
                                isSaving = false
                                // ponytail: toast hoisted ke VM agar tetap tampil setelah balik ke Profil
                                viewModel.showProfileUpdatedToast.value = true
                                navController.popBackStack()
                            },
                            onError = { err ->
                                isSaving = false
                                saveError = err
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                // Profile Avatar Photo Upload Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .shadow(2.dp, shape = CircleShape)
                            .background(Color.White, CircleShape)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                    if (photoModel != null) {
                        AsyncImage(
                            model = photoModel,
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.images),
                            fallback = painterResource(id = R.drawable.images)
                        )
                    } else {
                            AsyncImage(
                                model = R.drawable.images,
                                contentDescription = "Foto Profil Default",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gallery & Camera Buttons Row (SAME styling as in Register screen)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Galeri",
                                tint = Color(0xFF3CAEA3),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galeri", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    launchCamera()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Kamera",
                                tint = Color(0xFF3CAEA3),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kamera", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        // Hapus foto — konfirmasi dulu, lalu kembali ke default images.jpg
                        if (hasCustomPhoto) {
                            OutlinedButton(
                                onClick = { showDeletePhotoConfirm = true },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF86066)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus foto",
                                    tint = Color(0xFFF86066),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hapus", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Error upload/simpan (mis. internet putus saat upload foto)
                saveError?.let { err ->
                    Text(
                        text = err,
                        color = Color(0xFFF86066),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Inputs Forms
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Nama Lengkap
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "NAMA LENGKAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Masukkan nama lengkap", color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color(0xFF3CB7A6),
                                unfocusedIndicatorColor = Color(0xFFE5E7EB),
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(nameBringRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            // tunggu animasi keyboard selesai lalu scroll field ke atas tombol
                                            delay(350)
                                            try { nameBringRequester.bringIntoView() } catch (_: Exception) {}
                                        }
                                    }
                                }
                        )
                    }

                    // Alamat Email
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ALAMAT EMAIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        TextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            placeholder = { Text("Masukkan alamat email", color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color(0xFF3CB7A6),
                                unfocusedIndicatorColor = Color(0xFFE5E7EB),
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(emailBringRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(350)
                                            try { emailBringRequester.bringIntoView() } catch (_: Exception) {}
                                        }
                                    }
                                }
                        )
                    }

                    // Nomor Telepon
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "NOMOR TELEPON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+62",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B5563)
                                )
                            }
                            TextField(
                                value = phone,
                                onValueChange = { input ->
                                    phone = input.filter { it.isDigit() }
                                },
                                placeholder = { Text("812 3456 7890", color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color(0xFF3CB7A6),
                                    unfocusedIndicatorColor = Color(0xFFE5E7EB),
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .bringIntoViewRequester(phoneBringRequester)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            coroutineScope.launch {
                                                delay(350)
                                                try { phoneBringRequester.bringIntoView() } catch (_: Exception) {}
                                            }
                                        }
                                    }
                            )
                        }
                    }

                    // Gender & DOB Grid — ponytail: BoxWithConstraints agar tumpuk di layar sempit
                    androidx.compose.foundation.layout.BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val narrow = maxWidth < 360.dp
                        if (narrow) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                GenderField(gender, { gender = it })
                                DobField(dob, { dob = it }, context)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GenderField(gender, { gender = it })
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    DobField(dob, { dob = it }, context)
                                }
                            }
                        }
                    }

                    // Alamat Lengkap
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ALAMAT LENGKAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        TextField(
                            value = address,
                            onValueChange = { address = it },
                            placeholder = { Text("Masukkan alamat lengkap...", color = Color(0xFF9CA3AF)) },
                            singleLine = false,
                            minLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color(0xFF3CB7A6),
                                unfocusedIndicatorColor = Color(0xFFE5E7EB),
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(addressBringRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            // delay agar bringIntoView jalan setelah keyboard
                                            // terbuka penuh + tombol Simpan selesai naik
                                            delay(350)
                                            try {
                                                addressBringRequester.bringIntoView()
                                            } catch (_: Exception) {}
                                            // ulangi sekali untuk animasi keyboard yang lambat
                                            delay(300)
                                            try {
                                                addressBringRequester.bringIntoView()
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                        )
                    }
                    // spacer bawah agar field alamat bisa discroll penuh ke atas tombol Simpan
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

            }
    // ================= konfirmasi hapus foto profil =================
    if (showDeletePhotoConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoConfirm = false },
            title = {
                Text(
                    text = "Hapus Foto Profil?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    text = "Foto profil akan dihapus dan kembali ke foto default. Lanjutkan?",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteProfilePhotoFile(photoPath)
                        photoPath = null
                        showDeletePhotoConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF86066)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoConfirm = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
        }

@Composable
private fun GenderField(gender: String, onGender: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "JENIS KELAMIN",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6B7280),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        var genderExpanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .clickable { genderExpanded = true }
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gender,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            }
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Laki-laki") },
                    onClick = { onGender("Laki-laki"); genderExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Perempuan") },
                    onClick = { onGender("Perempuan"); genderExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun DobField(dob: String, onDob: (String) -> Unit, context: android.content.Context) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "TANGGAL LAHIR",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6B7280),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> onDob("$year-${month + 1}-$dayOfMonth") },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .clickable { datePickerDialog.show() }
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dob.isEmpty()) "Pilih Tanggal" else dob,
                    color = if (dob.isEmpty()) Color(0xFF9CA3AF) else Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditProfileTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Color(0xFF1E293B),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Edit Profil",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun EditProfileSaveBar(
    isSaving: Boolean,
    onSave: () -> Unit
) {
    // ponytail: imePadding agar bar tombol ikut naik ke atas keyboard bawaan
    // android — field diamankan scroll tertunda + imePadding konten
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E7EB))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3CB7A6),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .heightIn(min = 54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Menyimpan...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Text(
                            text = "Simpan Perubahan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
