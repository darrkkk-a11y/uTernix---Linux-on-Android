@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
// uTernix — Chromatic Motion: atmospheric depth, vivid accents, restrained motion.
package com.daryan.tessera.ui.deb

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daryan.tessera.R

object UColors {
    val Background=Color(0xFF080D1B)
    val Surface=Color(0xFF151E31)
    val Raised=Color(0xFF1E2C45)
    val Pressed=Color(0xFF2A3B56)
    val Border=Color(0xFF3C5272)
    val Text=Color(0xFFF8FAFF)
    val Muted=Color(0xFFB8C6DB)
    val Subtle=Color(0xFF879BB8)
    val Accent=Color(0xFF91B2FF)
    val Success=Color(0xFF76E8B9)
    val Info=Color(0xFF66D9F4)
    val Package=Color(0xFFFFB578)
    val System=Color(0xFFCAABFF)
    val Danger=Color(0xFFFF849F)
    val Rose=Color(0xFFFF91D2)
    val Gold=Color(0xFFFFD48D)
    val Glow=Color(0xFF8A7EFF)
}
val Onest = FontFamily(
  Font(R.font.onest_regular,FontWeight.Normal), Font(R.font.onest_medium,FontWeight.Medium),
  Font(R.font.onest_semibold,FontWeight.SemiBold), Font(R.font.onest_bold,FontWeight.Bold)
)
private fun style(size:Int,line:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=Onest,fontWeight=weight,fontSize=size.sp,lineHeight=line.sp)
val UTypography=Typography(
 displayLarge=style(36,44,FontWeight.Bold),displayMedium=style(32,40,FontWeight.Bold),displaySmall=style(28,36,FontWeight.Bold),
 headlineLarge=style(26,34,FontWeight.SemiBold),headlineMedium=style(23,30,FontWeight.SemiBold),headlineSmall=style(20,28,FontWeight.SemiBold),
 titleLarge=style(20,28,FontWeight.SemiBold),titleMedium=style(17,24,FontWeight.SemiBold),titleSmall=style(15,22,FontWeight.SemiBold),
 bodyLarge=style(16,24),bodyMedium=style(15,22),bodySmall=style(14,21),
 labelLarge=style(14,20,FontWeight.SemiBold),labelMedium=style(13,19,FontWeight.Medium),labelSmall=style(12,18,FontWeight.Medium)
)
val UShapes=Shapes(
 extraSmall=RoundedCornerShape(8.dp),small=RoundedCornerShape(11.dp),
 medium=RoundedCornerShape(15.dp),large=RoundedCornerShape(20.dp),extraLarge=RoundedCornerShape(24.dp)
)
private val UScheme=darkColorScheme(
 primary=UColors.Accent,onPrimary=Color(0xFF0B1630),primaryContainer=Color(0xFF293F6F),onPrimaryContainer=UColors.Accent,
 secondary=UColors.Info,onSecondary=UColors.Background,secondaryContainer=Color(0xFF204A60),onSecondaryContainer=UColors.Info,
 tertiary=UColors.System,onTertiary=UColors.Background,tertiaryContainer=Color(0xFF49366A),onTertiaryContainer=UColors.System,
 error=UColors.Danger,onError=UColors.Background,errorContainer=Color(0xFF5B2E46),onErrorContainer=UColors.Danger,
 background=UColors.Background,onBackground=UColors.Text,surface=UColors.Surface,onSurface=UColors.Text,
 surfaceVariant=UColors.Raised,onSurfaceVariant=UColors.Muted,surfaceTint=UColors.Accent,
 outline=UColors.Border,outlineVariant=UColors.Border,scrim=Color.Black,
 surfaceDim=UColors.Background,surfaceBright=UColors.Raised,surfaceContainerLowest=UColors.Background,
 surfaceContainerLow=UColors.Surface,surfaceContainer=UColors.Surface,
 surfaceContainerHigh=UColors.Raised,surfaceContainerHighest=UColors.Pressed,
 inverseSurface=UColors.Text,inverseOnSurface=UColors.Background,inversePrimary=Color(0xFF496FC8)
)

/** Compatibility values for feature modules. All surfaces are SOLID; no glass, glows or gradient rims. */
@Immutable class DebExtras(
 val dark:Boolean,val rim:List<Color>,val glowHigh:Color,val glowLow:Color,
 val glass:Color,val glassRaised:Color,val success:Color,val onSuccess:Color,
 val warning:Color,val onWarning:Color,val info:Color,val code:Color,val onCode:Color
)
private val UExtras=DebExtras(true,listOf(UColors.Border,UColors.Border),Color.Transparent,Color.Transparent,
 UColors.Surface,UColors.Raised,UColors.Success,UColors.Background,UColors.Package,UColors.Background,
 UColors.Info,Color(0xFF0B0F14),UColors.Text)
val LocalDebExtras=staticCompositionLocalOf { UExtras }
object Deb {val extras:DebExtras @Composable @ReadOnlyComposable get()=LocalDebExtras.current}

@Composable fun UternixTheme(content:@Composable ()->Unit){
 CompositionLocalProvider(LocalDebExtras provides UExtras){
  MaterialTheme(colorScheme=UScheme,typography=UTypography,shapes=UShapes,
    motionScheme=MotionScheme.expressive(),content=content)
 }
}
