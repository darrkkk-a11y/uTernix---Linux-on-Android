// uTernix design primitives: solid, matte, accessible surfaces.
package com.daryan.tessera.ui.deb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun utBorderBrush(stops: List<Color>, alpha: Float = 1f): Brush =
    Brush.linearGradient(
        stops.ifEmpty { listOf(UColors.Accent, UColors.Border, UColors.System) }
            .map { it.copy(alpha = alpha) }
    )

fun Modifier.utOutline(shape: Shape, width: Dp = 1.dp, alpha: Float = 1f): Modifier =
    border(width = width, brush = utBorderBrush(
        listOf(UColors.Info.copy(alpha = 0.55f), UColors.Border, UColors.System.copy(alpha = 0.5f)), alpha
    ), shape = shape)

fun Modifier.utSurface(shape: Shape, raised: Boolean = false): Modifier = composed {
    val base = if (raised) UColors.Raised else UColors.Surface
    shadow(if (raised) 10.dp else 5.dp, shape)
        .background(
            Brush.linearGradient(
                listOf(UColors.Accent.copy(alpha = 0.1f).compositeOver(base), base, UColors.Raised)
            ), shape
        )
        .utOutline(shape)
}

@Composable
fun UtCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    raised: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = if (raised) UColors.Raised else UColors.Surface
    val inner: @Composable () -> Unit = {
        Column(
            Modifier.background(
                Brush.linearGradient(
                    listOf(UColors.Info.copy(alpha = 0.07f).compositeOver(base), base, UColors.Raised)
                )
            ),
            content = content,
        )
    }
    if (onClick != null) Surface(
        onClick = onClick, modifier = modifier.shadow(8.dp, shape), shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, utBorderBrush(listOf(UColors.Accent.copy(alpha = 0.40f), UColors.Border, UColors.System.copy(alpha = 0.24f)))),
        content = inner,
    ) else Surface(
        modifier = modifier.shadow(8.dp, shape), shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, utBorderBrush(listOf(UColors.Accent.copy(alpha = 0.40f), UColors.Border, UColors.System.copy(alpha = 0.24f)))),
        content = inner,
    )
}

@Composable
fun UtBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier.background(Brush.verticalGradient(listOf(UColors.Background, Color(0xFF101A30), UColors.Background)))
    ) {
        CompositionLocalProvider(LocalContentColor provides UColors.Text) { content() }
    }
}
@Composable fun DebIconImage(icon:DebIcon,modifier:Modifier=Modifier,size:Dp=24.dp,
 contentDescription:String?=null,vector:Boolean=true,alpha:Float=1f,tint:Color?=null){
 Image(painter=painterResource(icon.vector),contentDescription=contentDescription,
   modifier=modifier.size(size),alpha=alpha,colorFilter=tint?.let { ColorFilter.tint(it) })
}
@Composable fun DebHeroIcon(icon:DebIcon,size:Dp=80.dp,modifier:Modifier=Modifier,contentDescription:String?=null){
 Box(modifier.size(size),contentAlignment=Alignment.Center) {
  DebIconImage(icon,size=size*0.68f,contentDescription=contentDescription)
 }
}
enum class DebStatus(val icon:DebIcon) {
 Success(DebIcon.StatusSuccess),Info(DebIcon.StatusInformation),Warning(DebIcon.StatusWarning),
 Error(DebIcon.StatusError),Loading(DebIcon.StatusLoading),Progress(DebIcon.StatusProgress)
}
@Composable fun StatusLine(status:DebStatus,text:String,modifier:Modifier=Modifier){
 Row(modifier,verticalAlignment=Alignment.CenterVertically){
  DebIconImage(status.icon,size=24.dp)
  Spacer(Modifier.width(10.dp))
  Text(text,style=MaterialTheme.typography.bodyMedium,maxLines=3,overflow=TextOverflow.Ellipsis)
 }
}
@Composable fun GroupTitle(text:String,modifier:Modifier=Modifier,icon:DebIcon?=null){
 Row(modifier,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
  if(icon!=null) DebIconImage(icon,size=24.dp)
  Text(text,style=MaterialTheme.typography.titleMedium,color=UColors.Text)
 }
}
@Composable fun UtPill(modifier:Modifier=Modifier,content:@Composable ()->Unit){
 Box(modifier.utSurface(RoundedCornerShape(11.dp)).padding(horizontal=12.dp,vertical=8.dp)){content()}
}
