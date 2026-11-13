package com.daryan.tessera.ui

import com.daryan.tessera.R
import com.daryan.tessera.ui.deb.UColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private fun Destination.labelRes():Int = when(this){
 Destination.Apps->R.string.ut_nav_apps
 Destination.Files->R.string.ut_nav_files
 Destination.Store->R.string.ut_nav_store
 Destination.Sessions->R.string.ut_nav_sessions
 Destination.Setup->R.string.ut_nav_setup
}

@Composable private fun NavEntry(destination:Destination,selected:Boolean,compact:Boolean,badge:@Composable ()->Unit,onClick:()->Unit){
 val label=stringResource(destination.labelRes())
 val color=if(selected) UColors.Accent else UColors.Muted
 val bg=if(selected) UColors.Raised else Color.Transparent
 if(compact){
  Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg)
    .selectable(selected=selected,onClick=onClick,role=Role.Tab)
    .padding(vertical=7.dp), horizontalAlignment=Alignment.CenterHorizontally){
    BadgedBox(badge={badge()}){Image(painterResource(destination.icon.vector),label,Modifier.size(27.dp))}
    Spacer(Modifier.height(5.dp))
    Text(label,style=MaterialTheme.typography.labelSmall,color=color,maxLines=1,overflow=TextOverflow.Ellipsis)
  }
 }else{
  Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg)
   .selectable(selected=selected,onClick=onClick,role=Role.Tab).padding(horizontal=16.dp, vertical=14.dp),
   verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   BadgedBox(badge={badge()}){Image(painterResource(destination.icon.vector),label,Modifier.size(28.dp))}
   Text(label,style=MaterialTheme.typography.labelLarge,color=color,maxLines=1)
  }
 }
}

/** Phone portrait navigation. No floating glass, no duplicated upper tabs. */
@Composable fun FloatingDock(selected:Destination,onSelect:(Destination)->Unit,badge:@Composable (Destination)->Unit){
 Row(Modifier.fillMaxWidth().background(UColors.Surface).navigationBarsPadding()
   .padding(horizontal=6.dp,vertical=8.dp).selectableGroup(),
   verticalAlignment=Alignment.CenterVertically){
  Destination.entries.forEach{ d ->
   Box(Modifier.weight(1f)){ NavEntry(d,d==selected,true,{badge(d)},{onSelect(d)}) }
  }
 }
}

/** Landscape / tablet: permanent rail. For short phone landscape, all items scroll. */
@Composable fun UternixRail(selected:Destination,onSelect:(Destination)->Unit,badge:@Composable (Destination)->Unit){
 val compact=androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 840
 Column(Modifier.fillMaxHeight().width(if(compact) 88.dp else 208.dp).background(UColors.Surface)
   .padding(horizontal=if(compact) 8.dp else 12.dp, vertical=12.dp)){
  if(!compact){
   Text("uTernix",Modifier.padding(start=14.dp,top=6.dp,bottom=20.dp),
     color=UColors.Text,style=MaterialTheme.typography.titleLarge)
  }
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).selectableGroup(),
    verticalArrangement=Arrangement.spacedBy(7.dp)){
   Destination.entries.forEach{d->NavEntry(d,d==selected,compact,{badge(d)},{onSelect(d)})}
  }
 }
}
