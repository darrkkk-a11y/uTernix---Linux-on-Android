// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.daryan.tessera.ui

import com.daryan.tessera.ui.deb.UColors
import com.daryan.tessera.ui.deb.Deb
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.DebIconImage
import com.daryan.tessera.ui.deb.UtBackdrop
import com.daryan.tessera.ui.deb.utSurface
import com.daryan.tessera.ui.deb.utBorderBrush
import androidx.compose.foundation.BorderStroke
import com.daryan.tessera.ui.deb.DebGroup
import com.daryan.tessera.ui.deb.DebStatus
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daryan.tessera.BackendMode
import com.daryan.tessera.X11Input

/** What the side menu's header and badges show. */
data class MenuStatus(
    val mode: BackendMode,
    /** Why Linux can't run apps yet, or null when it can. */
    val blocker: String?,
    val running: Int,
    val activeInstalls: Int,
    /** Root was found, so chroot and Droidspaces modes are on offer. */
    val rootAvailable: Boolean,
    val destination: Destination,
    /** In Droidspaces mode, the picked container and how it is ("web, running"); null otherwise. */
    val container: String? = null,
)

/** Everything the side menu can do; each closes the menu first. */
class MenuActions(
    val go: (Destination) -> Unit,
    val terminal: () -> Unit,
    val rootShell: () -> Unit,
    val newShortcut: () -> Unit,
    val trackpad: () -> Unit,
    val installs: () -> Unit,
    val systemFiles: () -> Unit,
    val displaySettings: () -> Unit,
    val copyDiagnostics: () -> Unit,
    val setMode: (BackendMode) -> Unit,
    val settings: () -> Unit,
    val about: () -> Unit,
    val intro: () -> Unit,
)

/**
 * The hamburger menu: how Linux is doing, quick launches, every tab, the tools that live
 * elsewhere, the engine, how Linux windows take touch, and the rest.
 */
@Composable
fun SideMenu(status: MenuStatus, actions: MenuActions) {
    val context = LocalContext.current
    ModalDrawerSheet {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            Header(status)

            Heading("Quick launch")
            Item("Terminal", "A Debian shell", DebIcon.NavTerminal, enabled = status.blocker == null, onClick = actions.terminal)
            Item("Root shell", "For apt and system changes", DebIcon.SysSecurity, enabled = status.blocker == null, onClick = actions.rootShell)
            Item("New shortcut", "A command as an icon", DebIcon.ActCreate, onClick = actions.newShortcut)

            Divider()
            Heading("Tools")
            Item("PC trackpad", "Drive a computer over USB or Bluetooth", DebIcon.UtilBluetooth, onClick = actions.trackpad)
            Item(
                "Installs",
                if (status.activeInstalls > 0) "${status.activeInstalls} going now" else "What apt installed and removed",
                DebIcon.PkgInstallPackage,
                badge = status.activeInstalls.takeIf { it > 0 },
                onClick = actions.installs,
            )
            Item("System files", "bashrc, apt sources, DNS, hosts", DebIcon.FileConf, onClick = actions.systemFiles)
            Item("Display", "Resolution and scaling of Linux windows", DebIcon.SysResources, onClick = actions.displaySettings)
            Item("Copy diagnostics", "Everything a helper needs, to paste", DebIcon.UtilAltTerminalLogs, onClick = actions.copyDiagnostics)

            Divider()
            Heading("Engine")
            var showModes by remember { mutableStateOf(false) }
            androidx.compose.foundation.layout.Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                FilledTonalButton(onClick = { showModes = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(status.mode.short, modifier = Modifier.weight(1f))
                    Text("⌄")
                }
                DropdownMenu(expanded = showModes, onDismissRequest = { showModes = false }) {
                    BackendMode.entries.forEach { m ->
                        DropdownMenuItem(text = { Text(m.short) },
                            enabled = m == BackendMode.PROOT || status.rootAvailable,
                            onClick = { showModes = false; actions.setMode(m) })
                    }
                }
            }
            Text(
                when {
                    status.container != null -> "Container: ${status.container}. Change it under Setup, " +
                        "which also says why this mode is less secure than chroot."
                    status.rootAvailable -> "Each engine has its own Linux system."
                    else -> "Chroot and Droidspaces need root (Magisk, KernelSU or APatch)."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
            )

            Divider()
            Heading("Touch in Linux windows")
            Column(Modifier.padding(horizontal = 16.dp)) {
                InputChoices(
                    mode = X11Input.mode(context),
                    onMode = { X11Input.setMode(context, it) },
                    longPress = X11Input.longPressRightClick(context),
                    onLongPress = { X11Input.setLongPressRightClick(context, it) },
                )
            }

            Divider()
            Item("Settings", "App collections, store, graphics and more", DebIcon.NavSettings, onClick = actions.settings)
            Item("Introduction", "The tour from the first run", DebIcon.UtilLaunch, onClick = actions.intro)
            Item("About", "Version, creator and licenses", DebIcon.UtilAltAbout, onClick = actions.about)
        }
    }
}

/** The app's name over how Linux is doing right now. */
@Composable
private fun Header(status: MenuStatus) {
    val ready = status.blocker == null
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = UColors.Raised,
        border = BorderStroke(1.dp, utBorderBrush(Deb.extras.rim, 0.9f)),
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DebIconImage(DebIcon.BrandSize128, size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Text("uTernix", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DebIconImage((if (ready) DebStatus.Success else DebStatus.Warning).icon, size = 28.dp)
                Spacer(Modifier.width(10.dp))
                Text(status.blocker ?: "Linux is ready", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Fact(status.mode.label)
                Spacer(Modifier.width(16.dp))
                Fact(if (status.running == 0) "Nothing running" else "${status.running} running")
            }
        }
    }
}

@Composable
private fun Fact(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge)
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun Divider() = HorizontalDivider(Modifier.padding(horizontal = 28.dp, vertical = 8.dp))

@Composable
private fun Item(
    title: String,
    detail: String,
    icon: DebIcon,
    enabled: Boolean = true,
    badge: Int? = null,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Column {
                Text(title, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        icon = { DebIconImage(icon, size = 34.dp) },
        badge = badge?.let { { Badge { Text("$it") } } },
        selected = false,
        onClick = { if (enabled) onClick() },
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}
