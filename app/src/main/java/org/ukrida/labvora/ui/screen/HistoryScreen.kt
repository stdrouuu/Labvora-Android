package org.ukrida.labvora.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ukrida.labvora.viewmodel.HistoryViewModel
import org.ukrida.labvora.data.model.TestHistoryItem
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    userId: Int,
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onNavigateToResult: (Int, Int, String?) -> Unit
) {
    LaunchedEffect(userId) {
        if (userId > 0) {
            viewModel.getHistoryList(userId)
        }
    }
    val historyList = viewModel.historyList.value
    val searchQuery = viewModel.searchQuery.value

    val filteredList = historyList.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.date.contains(searchQuery, ignoreCase = true) ||
                it.clinicName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        // ponytail: inset 0 agar tak dobel dgn BottomNav outer (sumber strip abu)
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Riwayat Pemeriksaan",
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
        ) {
            // Search Bar Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Cari tes atau tanggal...", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                )
            }

            // Title list header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Riwayat Pemeriksaan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }

            // List or Empty state
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF3F4F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pemeriksaan tidak ditemukan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coba gunakan kata kunci tes atau tanggal lain.",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(filteredList) { item ->
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 640.dp)) {
                            HistoryCard(
                                item = item,
                                onClick = { onNavigateToResult(item.id, item.testId, item.date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: TestHistoryItem,
    onClick: () -> Unit
) {
    val themeColor = when (item.testId) {
        1 -> Color(0xFFFA6A71)
        2 -> Color(0xFFFCA434)
        else -> Color(0xFF3CB7A6)
    }

    val containerBg = when (item.testId) {
        1 -> Color(0xFFFFEAEA)
        2 -> Color(0xFFFFF3E5)
        else -> Color(0xFFE6F7F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF9FAFB))
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Drop Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(containerBg, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        // Drawing blood drop path
                        val width = size.width
                        val height = size.height
                        val path = Path().apply {
                            moveTo(width / 2f, 0f)
                            cubicTo(
                                width * 0.85f, height * 0.4f,
                                width * 0.95f, height * 0.65f,
                                width * 0.95f, height * 0.75f
                            )
                            cubicTo(
                                width * 0.95f, height * 0.95f,
                                width * 0.75f, height,
                                width / 2f, height
                            )
                            cubicTo(
                                width * 0.25f, height,
                                width * 0.05f, height * 0.95f,
                                width * 0.05f, height * 0.75f
                            )
                            cubicTo(
                                width * 0.05f, height * 0.65f,
                                width * 0.15f, height * 0.4f,
                                width / 2f, 0f
                            )
                            close()
                        }
                        drawPath(path = path, color = themeColor)

                        // Draw inner highlights
                        val innerPath = Path().apply {
                            moveTo(width * 0.3f, height * 0.75f)
                            cubicTo(
                                width * 0.3f, height * 0.85f,
                                width * 0.4f, height * 0.9f,
                                width * 0.5f, height * 0.9f
                            )
                        }
                        drawPath(
                            path = innerPath,
                            color = Color.White,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // ponytail: tanggal-klinik grup rapat 2.dp, judul renggang 6.dp
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = item.date,
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF3CB7A6),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = item.clinicName,
                                fontSize = 11.sp,
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Detail",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
