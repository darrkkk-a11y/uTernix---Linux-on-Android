@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
// uTernix — flat dark design system. The original Midnight Glass theme has been retired.
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
    val Background=Color(0xFF0D1017)
    val Surface=Color(0xFF171C26)
    val Raised=Color(0xFF202633)
    val Pressed=Color(0xFF28303F)
    val Border=Color(0xFF313946)
    val Text=Color(0xFFF3F3F1)
    val Muted=Color(0xFFA8B0BE)
    val Subtle=Color(0xFF778391)
    val Accent=Color(0xFFDAF475)
    val Success=Color(0xFF80DAB0)
    val Info=Color(0xFF8FC8F7)
    val Package=Color(0xFFF9B982)
    val System=Color(0xFFC3AFF9)
    val Danger=Color(0xFFFF9399)
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
 primary=UColors.Accent,onPrimary=Color(0xFF171D10),primaryContainer=Color(0xFF303B29),onPrimaryContainer=UColors.Accent,
 secondary=UColors.Info,onSecondary=UColors.Background,secondaryContainer=Color(0xFF263748),onSecondaryContainer=UColors.Info,
 tertiary=UColors.System,onTertiary=UColors.Background,tertiaryContainer=Color(0xFF39304B),onTertiaryContainer=UColors.System,
 error=UColors.Danger,onError=UColors.Background,errorContainer=Color(0xFF483036),onErrorContainer=UColors.Danger,
 background=UColors.Background,onBackground=UColors.Text,surface=UColors.Surface,onSurface=UColors.Text,
 surfaceVariant=UColors.Raised,onSurfaceVariant=UColors.Muted,surfaceTint=UColors.Accent,
 outline=UColors.Border,outlineVariant=UColors.Border,scrim=Color.Black,
 surfaceDim=UColors.Background,surfaceBright=UColors.Raised,surfaceContainerLowest=UColors.Background,
 surfaceContainerLow=UColors.Surface,surfaceContainer=UColors.Surface,
 surfaceContainerHigh=UColors.Raised,surfaceContainerHighest=UColors.Pressed,
 inverseSurface=UColors.Text,inverseOnSurface=UColors.Background,inversePrimary=Color(0xFF607F16)
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
