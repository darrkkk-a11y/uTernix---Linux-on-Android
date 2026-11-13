package com.daryan.tessera.ui

import com.daryan.tessera.R
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.deb.UColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable private fun UPanel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){
 Surface(modifier=modifier,shape=RoundedCornerShape(20.dp),color=UColors.Surface,
  border=BorderStroke(1.dp,UColors.Border)){
  Column(Modifier.padding(18.dp),content=content)
 }
}

@Composable private fun UQuickAction(label:String,icon:Int,modifier:Modifier=Modifier,onClick:()->Unit){
 Surface(onClick=onClick,modifier=modifier.heightIn(min=86.dp),shape=RoundedCornerShape(16.dp),
  color=UColors.Raised,border=BorderStroke(1.dp,UColors.Border)){
  Column(Modifier.padding(14.dp),horizontalAlignment=Alignment.Start,verticalArrangement=Arrangement.spacedBy(12.dp)){
   Image(painterResource(icon),contentDescription=null,modifier=Modifier.size(30.dp))
   Text(label,style=MaterialTheme.typography.labelLarge,color=UColors.Text,maxLines=1,overflow=TextOverflow.Ellipsis)
  }
 }
}

/** Real frontend home, bound to VM values. No numbers are hard-coded. */
@Composable fun UternixHome(
 appCount:Int,running:Int,linuxMode:String,blocker:String?,apps:List<PermissionManifest>,
 onOpenAllApps:()->Unit,onOpen:(PermissionManifest)->Unit,onTerminal:()->Unit,
 onFiles:()->Unit,onStore:()->Unit,onSetup:()->Unit,onQueue:()->Unit,
 padding:PaddingValues
){
 BoxWithConstraints(Modifier.fillMaxSize()) {
  val wide = maxWidth >= 840.dp
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
      .padding(top=padding.calculateTopPadding(),bottom=padding.calculateBottomPadding()+24.dp)
      .padding(horizontal=if(wide) 28.dp else 16.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
    Text(stringResource(R.string.ut_home_title),style=MaterialTheme.typography.headlineMedium,color=UColors.Text)
    Text("$appCount",style=MaterialTheme.typography.titleLarge,color=UColors.Muted)
   }
   if(wide){
    Row(horizontalArrangement=Arrangement.spacedBy(18.dp),verticalAlignment=Alignment.Top){
     StatusPanel(linuxMode,blocker,running,onSetup,Modifier.weight(1f))
     QuickPanel(onTerminal,onFiles,onStore,onOpenAllApps,Modifier.weight(1f))
    }
   }else{
    StatusPanel(linuxMode,blocker,running,onSetup)
    QuickPanel(onTerminal,onFiles,onStore,onOpenAllApps)
   }
   UPanel {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
      horizontalArrangement=Arrangement.SpaceBetween){
     Text(stringResource(R.string.ut_recent_apps),color=UColors.Text,style=MaterialTheme.typography.titleLarge)
     TextButton(onClick=onOpenAllApps){Text(stringResource(R.string.ut_all_apps))}
    }
    Spacer(Modifier.height(12.dp))
    if(apps.isEmpty()){
     Text(stringResource(R.string.ut_no_apps),color=UColors.Muted,style=MaterialTheme.typography.bodyMedium)
    }else{
     val shown=apps.take(if(wide) 10 else 5)
     shown.forEach { app ->
      Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable{onOpen(app)}
        .padding(horizontal=4.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){
       AppGlyph(app,size=42.dp)
       Spacer(Modifier.width(12.dp))
       Text(app.name,Modifier.weight(1f),style=MaterialTheme.typography.bodyLarge,
          color=UColors.Text,maxLines=1,overflow=TextOverflow.Ellipsis)
       Icon(painterResource(R.drawable.ut_arrow_right_outline_text),null,Modifier.size(22.dp),tint=UColors.Muted)
      }
     }
    }
   }
   TextButton(onClick=onQueue){ Text(stringResource(R.string.ut_installs),color=UColors.Info) }
  }
 }
}

@Composable private fun StatusPanel(mode:String,blocker:String?,running:Int,onSetup:()->Unit,modifier:Modifier=Modifier){
 UPanel(modifier) {
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
   Image(painterResource(R.drawable.ut_device_filled_text),null,Modifier.size(32.dp))
   Column {
    Text(stringResource(R.string.ut_linux_status),style=MaterialTheme.typography.titleLarge,color=UColors.Text)
    Text(mode,style=MaterialTheme.typography.bodySmall,color=UColors.Muted)
   }
  }
  Spacer(Modifier.height(18.dp))
  Text(if(blocker==null) stringResource(R.string.ut_ready) else blocker,
    style=MaterialTheme.typography.bodyMedium,color=if(blocker==null) UColors.Success else UColors.Package)
  Spacer(Modifier.height(12.dp))
  Text("$running  ${stringResource(R.string.ut_running)}",style=MaterialTheme.typography.bodyMedium,color=UColors.Muted)
  Spacer(Modifier.height(18.dp))
  Button(onClick=onSetup,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
    colors=ButtonDefaults.buttonColors(containerColor=UColors.Accent,contentColor=UColors.Background),
    shape=RoundedCornerShape(14.dp)){
    Text(stringResource(if(blocker==null) R.string.ut_manage_linux else R.string.ut_setup_linux))
  }
 }
}

@Composable private fun QuickPanel(onTerminal:()->Unit,onFiles:()->Unit,onStore:()->Unit,onApps:()->Unit,modifier:Modifier=Modifier){
 Column(modifier,verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text(stringResource(R.string.ut_quick_actions),style=MaterialTheme.typography.titleMedium,color=UColors.Text)
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
   UQuickAction(stringResource(R.string.ut_terminal),R.drawable.ut_terminal_outline_text,Modifier.weight(1f),onTerminal)
   UQuickAction(stringResource(R.string.ut_nav_files),R.drawable.ut_folder_outline_text,Modifier.weight(1f),onFiles)
  }
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
   UQuickAction(stringResource(R.string.ut_nav_store),R.drawable.ut_package_outline_text,Modifier.weight(1f),onStore)
   UQuickAction(stringResource(R.string.ut_all_apps),R.drawable.ut_apps_outline_text,Modifier.weight(1f),onApps)
  }
 }
}
