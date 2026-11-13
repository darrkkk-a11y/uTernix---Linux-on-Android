// uTernix screen system. Replaces legacy glass/list-item layouts with shared matte components.
// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daryan.tessera.R
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.DebIconImage
import com.daryan.tessera.ui.deb.UColors

/** Prevents a phone list from becoming unreadably wide in tablet/DeX windows. */
@Composable
fun UternixContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 960.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
fun UternixPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(19.dp)
    Surface(modifier = modifier.fillMaxWidth().shadow(8.dp, shape), shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, Brush.linearGradient(
            listOf(UColors.Info.copy(alpha = 0.34f), UColors.Border, UColors.System.copy(alpha = 0.28f))
        ))) {
        Column(Modifier.background(Brush.linearGradient(
            listOf(UColors.Raised, UColors.Surface, UColors.Surface)
        )).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun UternixHeading(title: String, detail: String? = null, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = UColors.Text)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = UColors.Muted)
        }
        action?.invoke()
    }
}

/** Click target is at least 56dp high. The icon is not nested in another decorative tile. */
@Composable
fun UternixActionRow(icon: DebIcon, title: String, detail: String,
    modifier: Modifier = Modifier, enabled: Boolean = true, onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null) {
    val row: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 2.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DebIconImage(icon, size = 28.dp, alpha = if(enabled) 1f else 0.4f)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge,
                    color = if(enabled) UColors.Text else UColors.Subtle, maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodySmall,
                    color = UColors.Muted, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            if (trailing != null) trailing() else if(onClick != null) {
                Icon(painterResource(R.drawable.ut_arrow_right_outline_text),
                    contentDescription = null, modifier = Modifier.size(20.dp), tint = UColors.Muted)
            }
        }
    }
    if (onClick != null) Surface(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), color = Color.Transparent, content = row)
    else Box(modifier.fillMaxWidth()) { row() }
}

@Composable
fun UternixProgress(progress: Float? = null, modifier: Modifier = Modifier) {
    if (progress == null) LinearProgressIndicator(modifier = modifier.fillMaxWidth().height(4.dp),
        color = UColors.Accent, trackColor = UColors.Pressed)
    else LinearProgressIndicator(progress = {progress.coerceIn(0f, 1f)},
        modifier = modifier.fillMaxWidth().height(4.dp), color = UColors.Accent,
        trackColor = UColors.Pressed)
}

@Composable
fun UternixStateLabel(text: String, color: Color = UColors.Success) {
    Surface(color = color.copy(alpha = 0.13f), shape = RoundedCornerShape(8.dp)) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}
