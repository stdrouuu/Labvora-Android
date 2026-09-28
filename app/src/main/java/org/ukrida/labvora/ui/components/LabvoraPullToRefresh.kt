package org.ukrida.labvora.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabvoraPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    threshold: Dp = 44.dp,
    indicatorColor: Color = Color(0xFF3CB7A6),
    containerColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()

    // Menjaga agar animasi refreshing terlihat jelas berputar oleh pengguna (UX optimal: ~600-800ms),
    // tidak langsung hilang seketika jika response API terlalu cepat, tapi juga tidak terlalu lama.
    var showRefreshingAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showRefreshingAnimation = true
        } else if (showRefreshingAnimation) {
            // Beri jeda secukupnya agar putaran spinner terlihat jelas selesai berputar
            delay(380)
            showRefreshingAnimation = false
        }
    }

    val isIndicatorActive = isRefreshing || showRefreshingAnimation
    val isVisible = isIndicatorActive || state.distanceFraction > 0.01f

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "pull_indicator_alpha"
    )

    val animatedScale by animateFloatAsState(
        targetValue = when {
            !isVisible -> 0.45f
            isIndicatorActive -> 1f
            state.distanceFraction > 1f -> (1f + (state.distanceFraction - 1f) * 0.12f).coerceAtMost(1.15f)
            else -> (0.65f + 0.35f * state.distanceFraction).coerceIn(0.65f, 1f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pull_indicator_scale"
    )

    Box(
        modifier = modifier.pullToRefresh(
            state = state,
            isRefreshing = isIndicatorActive,
            threshold = threshold,
            onRefresh = onRefresh
        )
    ) {
        content()

        if (animatedAlpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        val fraction = state.distanceFraction
                        val hiddenY = (-52.dp).toPx()
                        val restingY = 24.dp.toPx()
                        val pullY = if (fraction <= 1f) {
                            hiddenY + (restingY - hiddenY) * fraction
                        } else {
                            restingY + (fraction - 1f) * 16.dp.toPx()
                        }
                        translationY = if (isIndicatorActive) restingY else pullY
                        scaleX = animatedScale
                        scaleY = animatedScale
                        alpha = animatedAlpha
                    }
                    .size(48.dp)
                    .shadow(elevation = 5.dp, shape = CircleShape)
                    .background(containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isIndicatorActive) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = indicatorColor,
                        strokeWidth = 3.2.dp,
                        trackColor = indicatorColor.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                } else {
                    val fraction = state.distanceFraction.coerceIn(0f, 1f)
                    val sweep = fraction * 280f
                    val startAngle = -90f + (fraction * 180f)
                    Canvas(modifier = Modifier.size(26.dp)) {
                        drawArc(
                            color = indicatorColor.copy(alpha = 0.15f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        if (sweep > 0f) {
                            drawArc(
                                color = indicatorColor,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }
    }
}
