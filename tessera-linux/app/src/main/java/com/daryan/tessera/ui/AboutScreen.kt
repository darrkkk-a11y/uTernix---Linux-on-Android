// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.daryan.tessera.ui

import com.daryan.tessera.ui.deb.UColors
import com.daryan.tessera.ui.deb.Deb
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.DebIconImage
import com.daryan.tessera.ui.deb.UtBackdrop
import com.daryan.tessera.ui.deb.utSurface
import com.daryan.tessera.ui.deb.utBorderBrush
import com.daryan.tessera.ui.deb.DebHeroIcon
import androidx.compose.foundation.BorderStroke
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daryan.tessera.R

private const val REPO = "https://github.com/daryan044/tessera-linux"

/** What the app is built on, and under which license. Mirrors the README's table. */
private val CREDITS = listOf(
    Triple("PRoot (Termux fork)", "GPL-2.0-or-later", "Runs Linux without root"),
    Triple("talloc", "LGPL-3.0-or-later", "Memory library used by PRoot"),
    Triple("Termux:X11", "GPL-3.0", "X server and app windows"),
    Triple("Termux terminal-view", "Apache-2.0", "The terminal"),
    Triple("Debian", "Various free licenses", "The Linux system, its packages and app catalog"),
    Triple("adw-gtk3", "LGPL-2.1", "The modern look of GTK 3 apps"),
        Triple("AndroidX, Jetpack Compose, Material 3", "Apache-2.0", "User interface"),
    Triple("kotlinx.serialization", "Apache-2.0", "Reading app manifests"),
    Triple("XZ for Java", "0BSD", "Reading Linux theme archives"),
)

@Composable
fun AboutScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
    }
    fun open(url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    val back = rememberBackSwipe(onBack = onClose)

    UtBackdrop(Modifier.backSwipePage(back)) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent, scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent),
                title = { Text("uTernix") },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
            )
        },
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 40.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            DebHeroIcon(DebIcon.BrandLauncher, size = 128.dp)
            Spacer(Modifier.height(16.dp))
            Text("uTernix", style = MaterialTheme.typography.headlineMedium)
            if (version.isNotEmpty()) {
                Text("Version $version", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Linux apps on Android, each in its own container and window, with the permissions you give it.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
            Surface(shape = MaterialTheme.shapes.large, color = UColors.Surface,
                border = BorderStroke(1.dp, UColors.Border), modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
                    DebIconImage(DebIcon.NavProfile, size = 56.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Created by", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Daryan Kamaran Karim", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = { open(REPO) }, modifier = Modifier.weight(1f)) { Text("Source code") }
                OutlinedButton(onClick = { open("$REPO/issues") }, modifier = Modifier.weight(1f)) { Text("Report a problem") }
            }

            Spacer(Modifier.height(28.dp))
            SectionTitle("License")
            Text(
                "Copyright © 2026 Daryan Kamaran Karim. Free software under the GNU General Public License, " +
                    "version 3 or later: you can use, study, share and change it. It comes with no warranty.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { open("$REPO/blob/main/LICENSE") }) { Text("Read the license") }

            Spacer(Modifier.height(28.dp))
            SectionTitle("Built on")
            listOf(
                Triple("uTernix interface icons", "Flat dark, Onest", "${DebIcon.all.size} vector mappings"),
                Triple("Onest and Unbounded", "SIL Open Font License 1.1", "The interface's typefaces"),
            ).forEach { (name, license, what) ->
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(name, style = MaterialTheme.typography.titleSmall)
                    Text("$what · $license", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            CREDITS.forEach { (name, license, what) ->
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(name, style = MaterialTheme.typography.titleSmall)
                    Text("$what · $license", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    )
}
