// View: Layar Registrasi untuk pendaftaran akun pengguna baru
package org.ukrida.labvora.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import org.ukrida.labvora.R
import org.ukrida.labvora.data.model.User
import org.ukrida.labvora.util.copyUriToProfileFile
import org.ukrida.labvora.util.createProfilePhotoFile
import org.ukrida.labvora.util.deleteProfilePhotoFile
import org.ukrida.labvora.util.photoPathToUploadFile
import org.ukrida.labvora.util.resolvePhotoModel
import org.ukrida.labvora.viewmodel.UserViewModel
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: UserViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigatePrivacyPolicy: () -> Unit = {}
) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isTermsChecked by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("Laki-laki") }
    var dob by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }

    // ROLE DEFAULT USER
    val role = "user"

    // URI gambar utama (disimpan sebagai String agar kompatibel dengan rememberSaveable)
    var imageUriString by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    // ponytail: path file unik per foto; resolve ke File agar Coil reload per akun
    val imageModel = remember(imageUriString) { resolvePhotoModel(imageUriString) }

    // URI kamera FileProvider (tidak survive process death, buat ulang tiap launch)
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    // ================= KAMERA =================
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // ponytail: path baru dipakai hanya jika jepretan sukses; batal = kembali ke foto lama.
        // File lama dihapus agar cuma satu foto tersimpan.
        if (success) {
            pendingCameraPath?.let {
                if (it != imageUriString) deleteProfilePhotoFile(imageUriString)
                imageUriString = it
            }
        }
        pendingCameraPath = null
    }

    fun launchCamera() {
        val file = createProfilePhotoFile(context)
        pendingCameraPath = file.absolutePath
        cameraLauncher.launch(FileProvider.getUriForFile(context, "${context.packageName}.provider", file))
    }

    // ================= GALERI =================
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // ponytail: salin ke internal agar foto ikut akun, bukan Uri galeri sementara.
            // File lama dihapus agar cuma satu foto tersimpan.
            val newPath = copyUriToProfileFile(context, it) ?: it.toString()
            if (newPath != imageUriString) deleteProfilePhotoFile(imageUriString)
            imageUriString = newPath
        }
    }

    // ================= PERMISSION =================
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onRegisterSuccess() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF4B5563)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.logoname),
                    contentDescription = "Labvora",
                    modifier = Modifier.height(20.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title "Daftar Akun"
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF3CAEA3))) {
                        append("Daftar ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF86E2D5))) {
                        append("Akun")
                    }
                },
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center
            )

            // Subtitle
            Text(
                text = "Mulai perjalanan kesehatan yang terpersonalisasi bersama kami.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
            )

            // Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(0.2.dp, shape = RoundedCornerShape(40.dp)),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))
                    ),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo Upload Section
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .shadow(0.2.dp, shape = CircleShape)
                            .background(Color.White, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageModel != null) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.images),
                                fallback = painterResource(id = R.drawable.images)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.images),
                                contentDescription = "Foto Profil Placeholder",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Camera & Gallery Buttons Row
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

                        // Hapus foto — kembali ke default images.jpg
                        if (imageModel != null) {
                            OutlinedButton(
                                onClick = {
                                    deleteProfilePhotoFile(imageUriString)
                                    imageUriString = null
                                },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // INPUTS FORM
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Nama Lengkap
                        Column {
                            Text(
                                text = "NAMA LENGKAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            TextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Nama sesuai identitas", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Nama",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF4F5F7),
                                    unfocusedContainerColor = Color(0xFFF4F5F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Username
                        Column {
                            Text(
                                text = "USERNAME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = { Text("username_unik", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AlternateEmail,
                                        contentDescription = "Username",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF4F5F7),
                                    unfocusedContainerColor = Color(0xFFF4F5F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Kata Sandi
                        Column {
                            Text(
                                text = "KATA SANDI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Minimal 8 karakter", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kata Sandi",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                },
                                trailingIcon = {
                                    val image = if (passwordVisible)
                                        Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff

                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Color(0xFF9CA3AF))
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF4F5F7),
                                    unfocusedContainerColor = Color(0xFFF4F5F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                                                }

                        // Alamat Email
                        Column {
                            Text(
                                text = "ALAMAT EMAIL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            TextField(
                                value = email,
                                onValueChange = { email = it.trim() },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                placeholder = { Text("contoh@email.com", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF4F5F7),
                                    unfocusedContainerColor = Color(0xFFF4F5F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Nomor Telepon
                        Column {
                            Text(
                                text = "NOMOR TELEPON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF4F5F7), RoundedCornerShape(16.dp))
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
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    placeholder = { Text("812 3456 7890", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Telepon",
                                            tint = Color(0xFF9CA3AF)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF4F5F7),
                                        unfocusedContainerColor = Color(0xFFF4F5F7),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Jenis Kelamin
                        Column {
                            Text(
                                text = "JENIS KELAMIN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            var genderExpanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4F5F7), RoundedCornerShape(16.dp))
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
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
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
                                        onClick = {
                                            gender = "Laki-laki"
                                            genderExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Perempuan") },
                                        onClick = {
                                            gender = "Perempuan"
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Tanggal Lahir
                        Column {
                            Text(
                                text = "TANGGAL LAHIR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            val calendar = Calendar.getInstance()
                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    dob = "$year-${month + 1}-$dayOfMonth"
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4F5F7), RoundedCornerShape(16.dp))
                                    .clickable { datePickerDialog.show() }
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (dob.isEmpty()) "Pilih Tanggal Lahir" else dob,
                                        color = if (dob.isEmpty()) Color(0xFF9CA3AF) else Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }

                        // Alamat Lengkap
                        Column {
                            Text(
                                text = "ALAMAT LENGKAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            TextField(
                                value = address,
                                onValueChange = { address = it },
                                placeholder = { Text("Alamat lengkap tempat tinggal...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Alamat",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                },
                                singleLine = false,
                                minLines = 3,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF4F5F7),
                                    unfocusedContainerColor = Color(0xFFF4F5F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Terms and Conditions checkbox
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isTermsChecked,
                                onCheckedChange = { isTermsChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF3CAEA3),
                                    uncheckedColor = Color(0xFF9CA3AF)
                                )
                            )
                            // ponytail: link underline + chevron dalam satu Row agar tak
                            // terpisah saat wrap; 1 unit FlowRow = selalu rapi
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { isTermsChecked = !isTermsChecked },
                                horizontalArrangement = Arrangement.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Saya menyetujui ",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onNavigatePrivacyPolicy() }
                                ) {
                                    Text(
                                        text = "Kebijakan Privasi",
                                        fontSize = 12.sp,
                                        color = Color(0xFF3CAEA3),
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Buka Kebijakan Privasi",
                                        tint = Color(0xFF3CAEA3),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = " Labvora.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Error Message validation display
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFF86066),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Button Daftar
            val registerInteractionSource = remember { MutableInteractionSource() }
            val isRegisterPressed by registerInteractionSource.collectIsPressedAsState()

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,8}$".toRegex()
                    val domainPart = trimmedEmail.substringAfter("@", "")
                    val tld = domainPart.substringAfterLast(".", "")
                    val domainName = domainPart.substringBeforeLast(".", "")

                    if (name.isBlank() || username.isBlank() || password.isBlank() || email.isBlank() || phone.isBlank() || dob.isBlank() || address.isBlank()) {
                        errorMessage = "Semua field input harus diisi!"
                    } else if (phone.length < 9 || phone.length > 14) {
                        errorMessage = "Nomor telepon harus berupa 9-14 digit angka!"
                    } else if (!emailPattern.matches(trimmedEmail) || !tld.all { it.isLetter() } || domainName.isBlank() || domainName.all { it.isDigit() } || domainPart.length < 4 || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                        errorMessage = "Format alamat email tidak valid!"
                    } else if (!isTermsChecked) {
                        errorMessage = "Anda harus menyetujui Kebijakan Privasi!"
                    } else if (!isSubmitting) {
                        errorMessage = null
                        isSubmitting = true
                        val user = User(
                            id = 0,
                            name = name,
                            username = username,
                            password = password,
                            role = role,
                            photo = imageUriString,
                            email = email,
                            phone = phone,
                            gender = if (gender == "Laki-laki") "L" else "P",
                            dob = dob,
                            address = address
                        )
                        // Upload foto dulu bila file lokal -> DB simpan filename
                        // server agar sync antar device.
                        viewModel.registerWithPhoto(
                            user,
                            photoPathToUploadFile(imageUriString),
                            onSuccess = {
                                isSubmitting = false
                                viewModel.showRegisterSuccessToast.value = true
                                onRegisterSuccess() // Redirect back to login page
                            },
                            onError = { err ->
                                isSubmitting = false
                                errorMessage = err
                            }
                        )
                    }
                },
                interactionSource = registerInteractionSource,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegisterPressed) Color(0xFF9CA3AF) else Color(0xFF39B3A3),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .shadow(0.2.dp, shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = if (isSubmitting) "Mendaftar..." else "Daftar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Redirect back to login page link
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sudah memiliki akun? ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Masuk di sini",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3CAEA3),
                    modifier = Modifier.clickable { onRegisterSuccess() }
                )
            }
        }
    }
}
