// uTernix: installed application library. The old tab pager and icon-tile home were retired.
// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.daryan.tessera.R
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.PagerState
import com.daryan.tessera.HomeLayout
import com.daryan.tessera.HomeWidget
import com.daryan.tessera.LauncherTab
import com.daryan.tessera.SHORTCUTS_TAB
import com.daryan.tessera.Shortcut
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.DebHeroIcon
import com.daryan.tessera.ui.deb.UColors

/** Reuses all existing VM actions; unlike the retired pager it works in portrait, landscape and DeX. */
@Composable
fun AppsPager(
    tabs: List<LauncherTab>,
    pagerState: PagerState,
    padding: PaddingValues,
    onOpen: (PermissionManifest) -> Unit,
    pinned: List<String>, favorites: List<String>,
    onTogglePin: (PermissionManifest) -> Unit,
    onToggleFavorite: (PermissionManifest) -> Unit,
    onUninstall: (PermissionManifest) -> Unit,
    onEditShortcut: (PermissionManifest) -> Unit,
    onNewShortcut: () -> Unit,
    onTrackpad: () -> Unit,
    onEditExec: (PermissionManifest) -> Unit,
    graphics: Boolean,
    onGraphics: (PermissionManifest) -> Unit,
    layout: HomeLayout,
    editing: Boolean,
    onEditing: (Boolean) -> Unit,
    onReorder: (String, List<String>) -> Unit,
    onHomeSettings: () -> Unit,
    onIconColor: (PermissionManifest) -> Unit,
    onHide: (PermissionManifest) -> Unit,
    onWidgets: () -> Unit,
    widget: @Composable (HomeWidget, Modifier) -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf("") }
    var dropdown by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val tab = tabs.firstOrNull { it.title == selected } ?: tabs.firstOrNull()
    val apps = remember(tab?.apps, search) { searchApps(tab?.apps.orEmpty(), search) }
    val visibleWidgets = remember(tab?.title, layout.widgets) { layout.widgets.filter { it.tab == tab?.title } }

    Column(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(UColors.Background, Color(0xFF121E37), UColors.Background)))
        .padding(top = padding.calculateTopPadding())) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.ut2_library_title), color = UColors.Text, style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.ut2_library_count, apps.size), color = UColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(painterResource(R.drawable.ut_more_outline_text),
                        contentDescription = stringResource(R.string.ut3_app_actions),
                        modifier = Modifier.size(24.dp), tint = UColors.Muted)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(if(editing) stringResource(R.string.ut2_library_done) else stringResource(R.string.ut2_library_arrange)) },
                        onClick = { menu = false; onEditing(!editing) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.ut2_library_layout)) }, onClick = { menu = false; onHomeSettings() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.ut2_library_widget)) }, onClick = { menu = false; onWidgets() })
                }
            }
        }
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            placeholder = { Text(stringResource(R.string.ut2_library_search)) },
            singleLine = true,
            leadingIcon = { Icon(painterResource(R.drawable.ut_search_outline_text),
                contentDescription = null, modifier = Modifier.size(22.dp), tint = UColors.Info) },
            trailingIcon = {
                if (search.isNotEmpty()) IconButton(onClick = { search = "" }) {
                    Icon(painterResource(R.drawable.ut_close_outline_text),
                        contentDescription = stringResource(R.string.ut3_clear_search),
                        modifier = Modifier.size(20.dp), tint = UColors.Muted)
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                FilledTonalButton(onClick = { dropdown = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)) {
                    Text(tab?.title ?: stringResource(R.string.ut2_library_all), modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("⌄")
                }
                DropdownMenu(expanded = dropdown, onDismissRequest = { dropdown = false }) {
                    tabs.forEach { t ->
                        DropdownMenuItem(text = { Text(t.title) }, onClick = {
                            selected = t.title; dropdown = false
                        })
                    }
                }
            }
            if (tab?.title == SHORTCUTS_TAB) {
                TextButton(onClick = onNewShortcut) { Text("+ " + stringResource(R.string.ut2_library_shortcut)) }
                TextButton(onClick = onTrackpad) { Text(stringResource(R.string.ut2_library_trackpad)) }
            }
        }
        if (tab == null || (apps.isEmpty() && visibleWidgets.isEmpty())) {
            EmptyState(if(search.isBlank()) stringResource(R.string.ut2_library_empty) else stringResource(R.string.ut2_library_no_matches),
                if(search.isBlank()) stringResource(R.string.ut2_library_install_hint) else stringResource(R.string.ut2_library_retry_search),
                PaddingValues(bottom = padding.calculateBottomPadding()))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 144.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (search.isBlank()) {
                    items(visibleWidgets.size, span = { GridItemSpan(maxLineSpan) }) { index ->
                        val w = visibleWidgets[index]
                        widget(w, Modifier.fillMaxWidth())
                    }
                }
                items(apps, key = { it.id }) { app ->
                    UternixInstalledApp(
                        app = app,
                        favorite = app.id in favorites,
                        pinned = app.id in pinned,
                        editing = editing,
                        onOpen = { onOpen(app) },
                        onFavorite = { onToggleFavorite(app) },
                        onPin = { onTogglePin(app) },
                        onRemove = { onUninstall(app) },
                        onEditExec = { if (Shortcut.isShortcut(app)) onEditShortcut(app) else onEditExec(app) },
                        onGraphics = if (graphics && app.gui) ({ onGraphics(app) }) else null,
                        onHide = { onHide(app) },
                        onIconColor = { onIconColor(app) },
                        onMove = { step ->
                            val original = tab.apps.map { it.id }.toMutableList()
                            val from = original.indexOf(app.id)
                            val to = (from + step).coerceIn(0, original.lastIndex)
                            if (from >= 0 && to != from) {
                                val value = original.removeAt(from)
                                original.add(to, value)
                                onReorder(tab.title, original)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UternixInstalledApp(
    app: PermissionManifest, favorite: Boolean, pinned: Boolean, editing: Boolean,
    onOpen: () -> Unit, onFavorite: () -> Unit, onPin: () -> Unit,
    onRemove: () -> Unit, onEditExec: () -> Unit, onGraphics: (() -> Unit)?,
    onHide: () -> Unit, onIconColor: () -> Unit, onMove: (Int) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val tone = when (app.category.lowercase()) {
        "internet", "network" -> UColors.Info
        "graphics", "audio", "video", "multimedia" -> UColors.Rose
        "development" -> UColors.System
        "system" -> UColors.Success
        "office" -> UColors.Package
        else -> UColors.Accent
    }
    UtKineticTile(
        modifier = Modifier.fillMaxWidth().heightIn(min = 178.dp),
        accent = tone,
        onClick = onOpen,
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppGlyph(app, size = 74.dp)
                Box {
                    IconButton(
                        onClick = { menu = true },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ut_more_outline_text),
                            contentDescription = stringResource(R.string.ut3_app_actions),
                            tint = UColors.Muted,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        UternixMenuCommand(stringResource(if (favorite) R.string.ut2_library_unfavorite else R.string.ut2_library_favorite)) {
                            menu = false; onFavorite()
                        }
                        UternixMenuCommand(stringResource(if (pinned) R.string.ut2_library_unpin else R.string.ut2_library_pin)) {
                            menu = false; onPin()
                        }
                        UternixMenuCommand(stringResource(R.string.ut2_library_edit)) {
                            menu = false; onEditExec()
                        }
                        if (onGraphics != null) UternixMenuCommand(stringResource(R.string.ut2_library_graphics)) {
                            menu = false; onGraphics()
                        }
                        UternixMenuCommand(stringResource(R.string.ut2_library_icon)) {
                            menu = false; onIconColor()
                        }
                        UternixMenuCommand(stringResource(R.string.ut2_library_hide)) {
                            menu = false; onHide()
                        }
                        UternixMenuCommand(stringResource(R.string.ut2_library_uninstall)) {
                            menu = false; onRemove()
                        }
                    }
                }
            }
            Text(
                app.name,
                style = MaterialTheme.typography.titleMedium,
                color = UColors.Text,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (app.category.isBlank()) stringResource(R.string.ut2_library_category_fallback)
                else app.category,
                style = MaterialTheme.typography.bodySmall,
                color = tone,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (editing) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { onMove(-1) }, modifier = Modifier.weight(1f)) {
                        Text("←")
                    }
                    OutlinedButton(onClick = { onMove(1) }, modifier = Modifier.weight(1f)) {
                        Text("→")
                    }
                }
            }
        }
    }
}

/** Search order is retained for the desktop launcher and public regression tests. */
fun searchApps(apps: List<PermissionManifest>, query: String): List<PermissionManifest> {
    val q = query.trim()
    if (q.isEmpty()) return apps.sortedBy { it.name.lowercase() }
    val lq = q.lowercase()
    fun rank(a: PermissionManifest): Int? {
        val name = a.name.lowercase()
        return when {
            name.startsWith(lq) -> 0
            name.split(' ', '-', '_', '.').any { it.startsWith(lq) } -> 1
            lq in name -> 2
            a.description.contains(q, ignoreCase = true) -> 3
            a.category.contains(q, ignoreCase = true) -> 4
            a.packages.any { it.contains(q, ignoreCase = true) } -> 5
            a.exec.firstOrNull()?.substringAfterLast('/')?.contains(q, ignoreCase = true) == true -> 6
            else -> null
        }
    }
    return apps.mapNotNull { a -> rank(a)?.let { it to a } }
        .sortedWith(compareBy<Pair<Int, PermissionManifest>> { it.first }.thenBy { it.second.name.lowercase() })
        .map { it.second }
}

@Composable
fun EmptyState(title: String, body: String, padding: PaddingValues, icon: DebIcon = DebIcon.NavApplications) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp)) {
        DebHeroIcon(icon, size = 88.dp)
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = UColors.Text)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = UColors.Muted,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun UternixMenuCommand(label: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label) }, onClick = onClick)
}
