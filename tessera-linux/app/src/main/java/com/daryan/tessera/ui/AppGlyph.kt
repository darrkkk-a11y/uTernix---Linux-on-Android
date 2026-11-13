// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.deb.Deb
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.debIconForApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File

private val iconBitmaps = LruCache<String, ImageBitmap>(192)
private val tileTones = listOf(
    Color(0xFF6F8FF2), Color(0xFF55C8D7), Color(0xFF9C81E5),
    Color(0xFF5DD1A6), Color(0xFFF5A567), Color(0xFFE68AAE),
)

/** Read local desktop icon assets off the UI thread, reloading when a package updates them. */
@Composable
fun rememberFileBitmap(path: String?): ImageBitmap? {
    val source = path?.let { File(it) }
    val cacheKey = source?.takeIf { it.isFile }?.let {
        it.absolutePath + ":" + it.length() + ":" + it.lastModified()
    }
    return produceState<ImageBitmap?>(initialValue = cacheKey?.let(iconBitmaps::get), key1 = cacheKey) {
        val p = source ?: return@produceState
        if (value != null || cacheKey == null) return@produceState
        value = withContext(Dispatchers.IO) {
            runCatching { decodeLinuxIcon(p)?.asImageBitmap() }.getOrNull()
        }?.also { iconBitmaps.put(cacheKey, it) }
    }.value
}

private fun decodeLinuxIcon(file: File): Bitmap? {
    if (!file.isFile || file.length() !in 1..1_500_000) return null
    val prefix = file.inputStream().use { input ->
        ByteArray(8192).let { buffer -> buffer.copyOf(input.read(buffer).coerceAtLeast(0)) }
    }.toString(Charsets.UTF_8).trimStart('\uFEFF', ' ', '\n', '\r', '\t')
    val ext = file.extension.lowercase()
    return when {
        ext == "svg" || prefix.startsWith("<svg") || prefix.contains("<svg", ignoreCase = true) -> {
            val svg = file.inputStream().use { SVG.getFromInputStream(it) }
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).drawPicture(svg.renderToPicture(512, 512))
            bitmap
        }
        ext == "xpm" || prefix.contains("XPM") -> decodeXpm(file.readText())
        else -> BitmapFactory.decodeFile(file.absolutePath)
    }
}

/** Handle common freedesktop XPM icons without losing their original colors. */
private fun decodeXpm(text: String): Bitmap? {
    val rows = Regex("\"([^\"]*)\"").findAll(text).map { it.groupValues[1] }.toList()
    val header = rows.firstOrNull()?.trim()?.split(Regex("\\s+")) ?: return null
    if (header.size < 4) return null
    val w = header[0].toIntOrNull() ?: return null
    val h = header[1].toIntOrNull() ?: return null
    val count = header[2].toIntOrNull() ?: return null
    val cpp = header[3].toIntOrNull() ?: return null
    if (w !in 1..512 || h !in 1..512 || count !in 1..512 || cpp !in 1..4 || rows.size < 1 + count + h) return null
    val palette = mutableMapOf<String, Int>()
    rows.drop(1).take(count).forEach { line ->
        if (line.length < cpp) return@forEach
        val code = line.take(cpp)
        val color = Regex("(?:^|\\s)c\\s+(\\S+)", RegexOption.IGNORE_CASE)
            .find(line.drop(cpp))?.groupValues?.get(1) ?: return@forEach
        palette[code] = if (color.equals("None", ignoreCase = true)) android.graphics.Color.TRANSPARENT
            else runCatching { android.graphics.Color.parseColor(color) }.getOrDefault(android.graphics.Color.TRANSPARENT)
    }
    val pixels = IntArray(w * h)
    for (y in 0 until h) {
        val line = rows[1 + count + y]
        if (line.length < w * cpp) return null
        for (x in 0 until w) pixels[y * w + x] =
            palette[line.substring(x * cpp, (x + 1) * cpp)] ?: android.graphics.Color.TRANSPARENT
    }
    return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
}

/** Every Linux app keeps its real desktop icon, proportional to the available tile. */
@Composable
fun AppTileFill(app: PermissionManifest, modifier: Modifier = Modifier) {
    val custom = customIconColors.value[app.id]?.let { Color(it) }
    val tone = custom ?: tileTones[(app.id.hashCode() and Int.MAX_VALUE) % tileTones.size]
    val bitmap = rememberFileBitmap(app.iconFile)
    val shape = RoundedCornerShape(20.dp)
    val dark = Deb.extras.dark
    val fallback = debIconForApp(app).let { if (dark) it.png else it.vector }
    Box(
        modifier = modifier.fillMaxSize().shadow(7.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        tone.copy(alpha = 0.33f),
                        Color(0xFF1E2A42),
                        tone.copy(alpha = 0.17f),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(0.82f),
            )
        } else if (debIconForApp(app) == DebIcon.NavApplications) {
            Text(
                text = app.name.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        } else {
            Image(
                painter = painterResource(fallback),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(0.70f),
            )
        }
    }
}

@Composable
fun AppGlyph(app: PermissionManifest, size: Dp, tint: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    Box(modifier.size(size)) { AppTileFill(app) }
}

@Composable
fun CompactAppTile(app: PermissionManifest, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Box(Modifier.size(66.dp)) { AppTileFill(app) }
        Spacer(Modifier.height(8.dp))
        Text(
            app.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
