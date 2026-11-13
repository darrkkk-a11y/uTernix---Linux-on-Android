// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daryan.tessera.R
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.deb.UColors

private val accentByAction = listOf(UColors.Success, UColors.Info, UColors.Package, UColors.System)

@Composable
fun UternixHome(
    appCount: Int,
    running: Int,
    linuxMode: String,
    blocker: String?,
    apps: List<PermissionManifest>,
    onOpenAllApps: () -> Unit,
    onOpen: (PermissionManifest) -> Unit,
    onTerminal: () -> Unit,
    onFiles: () -> Unit,
    onStore: () -> Unit,
    onSetup: () -> Unit,
    onQueue: () -> Unit,
    padding: PaddingValues,
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }
    UternixAmbient(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = padding.calculateTopPadding() + 14.dp,
                        bottom = padding.calculateBottomPadding() + 26.dp,
                    )
                    .padding(horizontal = if (wide) 28.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("uTernix", color = UColors.Text, style = MaterialTheme.typography.headlineLarge)
                        Text(
                            stringResource(R.string.ut3_home_subtitle),
                            color = UColors.Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = onQueue) {
                        Text(stringResource(R.string.ut_installs), color = UColors.Info)
                    }
                }
                AnimatedVisibility(
                    visible = show,
                    enter = fadeIn(tween(340)) + slideInVertically(tween(340)) { it / 5 },
                ) {
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LinuxStatusCard(linuxMode, blocker, running, onSetup, Modifier.weight(1.1f))
                            HomeQuickActions(onTerminal, onFiles, onStore, onOpenAllApps, Modifier.weight(1f))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            LinuxStatusCard(linuxMode, blocker, running, onSetup)
                            HomeQuickActions(onTerminal, onFiles, onStore, onOpenAllApps)
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            stringResource(R.string.ut3_apps_section),
                            style = MaterialTheme.typography.titleLarge, color = UColors.Text,
                        )
                        Text(
                            stringResource(R.string.ut2_library_count, appCount),
                            color = UColors.Muted, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = onOpenAllApps) {
                        Text(stringResource(R.string.ut3_open_library), color = UColors.Accent)
                    }
                }
                if (apps.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = UColors.Surface.copy(alpha = 0.82f),
                        border = BorderStroke(1.dp, UColors.Border),
                    ) {
                        Text(
                            stringResource(R.string.ut_no_apps),
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            color = UColors.Muted,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        apps.take(if (wide) 12 else 8).forEach { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(86.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onOpen(app) }
                                    .padding(vertical = 7.dp),
                            ) {
                                AppGlyph(app, size = 68.dp)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    app.name, color = UColors.Text,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinuxStatusCard(
    mode: String,
    blocker: String?,
    running: Int,
    onSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ready = blocker == null
    val tone = if (ready) UColors.Success else UColors.Package
    val shape = RoundedCornerShape(22.dp)
    Surface(
        modifier = modifier.fillMaxWidth().shadow(14.dp, shape),
        color = Color.Transparent,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(tone.copy(alpha = 0.60f), UColors.Border, UColors.System.copy(alpha = 0.25f)))
        ),
        shape = shape,
    ) {
        Column(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(UColors.Raised, tone.copy(alpha = 0.12f).compositeOver(UColors.Surface), UColors.Surface)
                )
            ).padding(18.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ut_linux_filled_text),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.ut_linux_status),
                        color = UColors.Text, style = MaterialTheme.typography.titleLarge,
                    )
                    Text(mode, color = UColors.Muted, style = MaterialTheme.typography.bodySmall)
                }
                UtLiveDot(tone)
            }
            Text(
                if (ready) stringResource(R.string.ut_ready) else blocker.orEmpty(),
                color = if (ready) UColors.Success else UColors.Package,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "$running " + stringResource(R.string.ut_running),
                    color = UColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = onSetup,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UColors.Accent),
                    border = BorderStroke(1.dp, UColors.Accent.copy(alpha = 0.55f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                ) {
                    Text(
                        stringResource(if (ready) R.string.ut_manage_linux else R.string.ut_setup_linux),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeQuickActions(
    onTerminal: () -> Unit,
    onFiles: () -> Unit,
    onStore: () -> Unit,
    onApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        stringResource(R.string.ut_terminal), stringResource(R.string.ut_nav_files),
        stringResource(R.string.ut_nav_store), stringResource(R.string.ut_all_apps),
    )
    val icons = listOf(
        R.drawable.ut_terminal_filled_text, R.drawable.ut_folder_filled_text,
        R.drawable.ut_package_filled_text, R.drawable.ut_apps_filled_text,
    )
    val handlers = listOf(onTerminal, onFiles, onStore, onApps)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(R.string.ut_quick_actions),
            color = UColors.Text,
            style = MaterialTheme.typography.titleMedium,
        )
        repeat(2) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) { col ->
                    val index = row * 2 + col
                    UtKineticTile(
                        modifier = Modifier.weight(1f).height(74.dp),
                        accent = accentByAction[index],
                        onClick = handlers[index],
                    ) {
                        Row(
                            Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            Image(
                                painterResource(icons[index]),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                            )
                            Text(
                                labels[index],
                                color = UColors.Text,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 2, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
