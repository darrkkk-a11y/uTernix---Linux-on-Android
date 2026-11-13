// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.ui

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.daryan.tessera.ui.deb.UColors
import kotlin.math.max

/** Respect the platform's reduced-motion setting on Android 8+. */
fun utAnimationsEnabled(): Boolean =
    Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()

/** Ambient color is limited to background washes; the text and app artwork stay sharp. */
@Composable
fun UternixAmbient(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(UColors.Background, Color(0xFF0D1730), UColors.Background)
                )
            )
            .drawBehind {
                val radius = max(size.width, size.height) * 0.8f
                drawRect(
                    Brush.radialGradient(
                        listOf(UColors.System.copy(alpha = 0.17f), Color.Transparent),
                        center = Offset(size.width * 0.94f, size.height * 0.08f),
                        radius = radius,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(UColors.Info.copy(alpha = 0.11f), Color.Transparent),
                        center = Offset(size.width * 0.02f, size.height * 0.65f),
                        radius = radius,
                    )
                )
            },
        content = content,
    )
}

/** One canonical tile style: animated press feedback, rich accent and controlled depth. */
@Composable
fun UtKineticTile(
    modifier: Modifier = Modifier,
    accent: Color = UColors.Accent,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = utAnimationsEnabled()
    val scale by animateFloatAsState(
        targetValue = if (pressed && motion) 0.965f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "Tile press",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 9.dp,
        label = "Tile elevation",
    )
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.shadow(elevation, shape),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(accent.copy(alpha = 0.50f), UColors.Border, accent.copy(alpha = 0.13f))
            )
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.25f).compositeOver(UColors.Raised),
                        UColors.Surface,
                        UColors.Raised,
                    )
                )
            ),
            content = content,
        )
    }
}
