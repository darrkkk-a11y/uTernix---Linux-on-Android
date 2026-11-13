// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

package com.daryan.tessera.ui

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.deb.Deb
import com.daryan.tessera.ui.deb.debIconForApp
import com.daryan.tessera.ui.deb.utSurface
import com.daryan.tessera.ui.deb.utOutline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val bitmaps = LruCache<String, ImageBitmap>(256)

/** Decodes a PNG off the main thread, once per path. */
@Composable
fun rememberFileBitmap(path: String?): ImageBitmap? =
    produceState(path?.let { bitmaps.get(it) }, path) {
        if (path == null || value != null) return@produceState
        value = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
            ?.also { bitmaps.put(path, it) }
    }.value

/**
 * An app's tile, filling the box it is in. An app with an icon of its own (store apps) sits on a
 * glass plate; the rest get the pack's tile for what they are ([debIconForApp]). A color the user
 * picked for the app becomes a plate of that color with the tile on it.
 */
@Composable
fun AppTileFill(app: PermissionManifest, modifier: Modifier = Modifier) {
    val custom = customIconColors.value[app.id]
    val bitmap = rememberFileBitmap(app.iconFile)
    val shape = RoundedCornerShape(percent = 22)
    val dark = Deb.extras.dark
    val tileRes = debIconForApp(app).let { if (dark) it.png else it.vector }
    when {
        custom != null -> Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize().clip(shape).background(Color(custom)).utOutline(shape, 1.dp, 0.5f),
        ) {
            if (bitmap != null) Image(bitmap, contentDescription = null, modifier = Modifier.fillMaxSize(0.72f))
            else Image(painterResource(tileRes), contentDescription = null, modifier = Modifier.fillMaxSize(0.78f))
        }
        bitmap != null -> Image(bitmap, contentDescription = null,
            modifier = modifier.fillMaxSize().clip(shape))
        else -> Image(painterResource(tileRes), contentDescription = null, modifier = modifier.fillMaxSize())
    }
}

/** [AppTileFill] at a given size, for places with no box of their own (dialogs). */
@Composable
fun AppGlyph(app: PermissionManifest, size: Dp, tint: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    Box(modifier.size(size)) { AppTileFill(app) }
}

/** An app as a small tile, its icon over its name: in search results and the desktop's Start menu. */
@Composable
fun CompactAppTile(app: PermissionManifest, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Box(Modifier.size(56.dp)) { AppTileFill(app) }
        Spacer(Modifier.height(6.dp))
        Text(
            app.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
