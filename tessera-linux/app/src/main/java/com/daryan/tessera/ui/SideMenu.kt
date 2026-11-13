// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

package com.daryan.tessera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daryan.tessera.BackendMode
import com.daryan.tessera.R
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.DebIconImage
import com.daryan.tessera.ui.deb.UColors

/** Preserve the existing menu model and callback API for launcher compatibility. */
data class MenuStatus(
    val mode: BackendMode,
    val blocker: String?,
    val running: Int,
    val activeInstalls: Int,
    val rootAvailable: Boolean,
    val destination: Destination,
    val container: String? = null,
)

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
 * Compact uTernix drawer. System mode is configured in System, and touch controls are
 * configured in the X11 window, not duplicated in global navigation.
 */
@Composable
fun SideMenu(status: MenuStatus, actions: MenuActions) {
    var advanced by remember { mutableStateOf(false) }
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 344.dp),
        drawerContainerColor = UColors.Surface,
        drawerContentColor = UColors.Text,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
            ) {
                DebIconImage(DebIcon.BrandSize128, size = 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("uTernix", style = MaterialTheme.typography.titleLarge, color = UColors.Text)
                    Text(
                        status.blocker ?: stringResource(R.string.ut_drawer_linux_ready),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.blocker == null) UColors.Success else UColors.Package,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${status.mode.short} · " + stringResource(R.string.ut_drawer_sessions, status.running),
                        style = MaterialTheme.typography.labelSmall,
                        color = UColors.Muted,
                    )
                }
            }
            HorizontalDivider(color = UColors.Border, modifier = Modifier.padding(vertical = 12.dp))
            MenuHeading(stringResource(R.string.ut_drawer_quick))
            MenuLine(DebIcon.NavTerminal, stringResource(R.string.ut_drawer_terminal),
                stringResource(R.string.ut_drawer_terminal_hint), status.blocker == null, actions.terminal)
            MenuLine(DebIcon.PkgInstallPackage, stringResource(R.string.ut_drawer_installs),
                if (status.activeInstalls > 0) stringResource(R.string.ut_drawer_active, status.activeInstalls)
                else stringResource(R.string.ut_drawer_installs_hint), onClick = actions.installs)

            MenuHeading(stringResource(R.string.ut_drawer_tools))
            MenuLine(DebIcon.UtilBluetooth, stringResource(R.string.ut_drawer_trackpad),
                stringResource(R.string.ut_drawer_trackpad_hint), onClick = actions.trackpad)
            MenuLine(DebIcon.SysResources, stringResource(R.string.ut_drawer_display),
                stringResource(R.string.ut_drawer_display_hint), onClick = actions.displaySettings)
            MenuLine(DebIcon.NavSettings, stringResource(R.string.ut_drawer_settings),
                stringResource(R.string.ut_drawer_settings_hint), onClick = actions.settings)

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = UColors.Border, modifier = Modifier.padding(horizontal = 8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { advanced = !advanced }.heightIn(min = 52.dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(stringResource(R.string.ut_drawer_advanced),
                    style = MaterialTheme.typography.titleSmall, color = UColors.Muted,
                    modifier = Modifier.weight(1f))
                Text(if (advanced) "−" else "+", color = UColors.Muted,
                    style = MaterialTheme.typography.titleLarge)
            }
            if (advanced) {
                MenuLine(DebIcon.SysSecurity, stringResource(R.string.ut_drawer_root_shell),
                    stringResource(R.string.ut_drawer_root_hint), status.blocker == null, actions.rootShell)
                MenuLine(DebIcon.ActCreate, stringResource(R.string.ut_drawer_shortcut),
                    stringResource(R.string.ut_drawer_shortcut_hint), onClick = actions.newShortcut)
                MenuLine(DebIcon.FileConf, stringResource(R.string.ut_drawer_system_files),
                    stringResource(R.string.ut_drawer_system_files_hint), onClick = actions.systemFiles)
                MenuLine(DebIcon.UtilAltTerminalLogs, stringResource(R.string.ut_drawer_diagnostics),
                    stringResource(R.string.ut_drawer_diagnostics_hint), onClick = actions.copyDiagnostics)
                MenuLine(DebIcon.UtilLaunch, stringResource(R.string.ut_drawer_intro), onClick = actions.intro)
                MenuLine(DebIcon.UtilAltAbout, stringResource(R.string.ut_drawer_about), onClick = actions.about)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MenuHeading(label: String) {
    Text(label, style = MaterialTheme.typography.labelMedium, color = UColors.Subtle,
        modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 8.dp))
}

@Composable
private fun MenuLine(
    icon: DebIcon,
    label: String,
    detail: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        DebIconImage(icon, size = 28.dp, alpha = if (enabled) 1f else 0.45f)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall,
                color = if (enabled) UColors.Text else UColors.Subtle,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall,
                color = UColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
