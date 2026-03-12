package com.myaux.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaux.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.6f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo fade in + scale
        alphaAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        // Subtitle slides in
        subtitleAlpha.animateTo(1f, tween(500))
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(DarkBackground, DarkSurface),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App name with gradient
            Text(
                text = "MyauX",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value),
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sistema Escolar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                color = TextGray,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Loading dots animation
            LoadingDots(Modifier.alpha(alphaAnim.value))
        }
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotAlphas = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 200)
            ),
            label = "dot_$index"
        )
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dotAlphas.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(alpha.value)
                    .background(PurplePrimary, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
