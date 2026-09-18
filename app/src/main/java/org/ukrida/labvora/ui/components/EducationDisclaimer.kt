package org.ukrida.labvora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ukrida.labvora.R

// App-side mapping: data lama dari server yang masih "Klinik Cinta Kasih"
// ditampilkan sebagai "Klinik Labvora" tanpa mengubah database.
fun displayClinicName(raw: String): String {
    if (raw.isBlank()) return "Klinik Labvora PIK"
    return raw
        .replace("Klinik Cinta Kasih", "Klinik Labvora")
        .replace("Cinta Care", "Labvora Care")
}

@Composable
fun EducationDisclaimerBanner(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.disclaimer_education_long)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = Color(0xFFB45309),
            modifier = Modifier.size(16.dp).padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF92400E),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EducationDisclaimerFooter(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.disclaimer_education_short)
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}
