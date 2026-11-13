// uTernix design primitives: solid, matte, accessible surfaces.
package com.daryan.tessera.ui.deb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun utBorderBrush(stops: List<Color>, alpha:Float=1f):Brush=androidx.compose.ui.graphics.SolidColor(UColors.Border.copy(alpha=alpha))
fun Modifier.utOutline(shape:Shape,width:Dp=1.dp,alpha:Float=1f):Modifier=border(width,UColors.Border.copy(alpha=alpha),shape)
fun Modifier.utSurface(shape:Shape,raised:Boolean=false):Modifier=composed {
 background(if(raised) UColors.Raised else UColors.Surface,shape).border(1.dp,UColors.Border,shape)
}
@Composable fun UtCard(modifier:Modifier=Modifier,shape:Shape=MaterialTheme.shapes.large,
  raised:Boolean=false,onClick:(()->Unit)?=null,content:@Composable ColumnScope.()->Unit){
 val col=if(raised) UColors.Raised else UColors.Surface
 if(onClick!=null) Surface(onClick=onClick,modifier=modifier,shape=shape,color=col,
     border=BorderStroke(1.dp,UColors.Border)){ Column(content=content) }
 else Surface(modifier=modifier,shape=shape,color=col,
     border=BorderStroke(1.dp,UColors.Border)){ Column(content=content) }
}
@Composable fun UtBackdrop(modifier:Modifier=Modifier,content:@Composable BoxScope.()->Unit){
 Box(modifier.background(UColors.Background)) { CompositionLocalProvider(
   LocalContentColor provides UColors.Text){content()} }
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
