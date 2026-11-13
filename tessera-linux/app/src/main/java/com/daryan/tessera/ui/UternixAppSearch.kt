package com.daryan.tessera.ui

import com.daryan.tessera.R
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.deb.UColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable fun UternixAppSearch(apps:List<PermissionManifest>,onDismiss:()->Unit,onOpen:(PermissionManifest)->Unit){
 var query by remember { mutableStateOf("") }
 Dialog(onDismissRequest=onDismiss){
  Surface(shape=MaterialTheme.shapes.large,color=UColors.Raised,modifier=Modifier.fillMaxWidth().heightIn(max=630.dp)){
   Column(Modifier.padding(16.dp)){
    OutlinedTextField(value=query,onValueChange={query=it},modifier=Modifier.fillMaxWidth(),singleLine=true,
      label={Text(stringResource(R.string.ut_search_hint))})
    Spacer(Modifier.height(12.dp))
    val filtered=remember(query,apps){searchApps(apps,query)}
    if(filtered.isEmpty()){
     Text(stringResource(R.string.ut_empty_search),Modifier.padding(18.dp),color=UColors.Muted)
    }else LazyColumn(Modifier.fillMaxWidth()){
     items(filtered,key={it.id}) { app ->
      Row(Modifier.fillMaxWidth().clickable{onOpen(app);onDismiss()}.padding(vertical=10.dp),
          verticalAlignment=Alignment.CenterVertically){
       AppGlyph(app,40.dp)
       Spacer(Modifier.width(14.dp))
       Text(app.name,style=MaterialTheme.typography.bodyLarge,color=UColors.Text)
      }
     }
    }
   }
  }
 }
}
