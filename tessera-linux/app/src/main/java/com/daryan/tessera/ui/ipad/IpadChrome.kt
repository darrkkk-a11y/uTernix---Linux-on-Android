// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

package com.daryan.tessera.ui.ipad

import com.daryan.tessera.ui.deb.UColors
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daryan.tessera.R
import com.daryan.tessera.X11Scale
import com.daryan.tessera.ui.DesktopActions
import com.daryan.tessera.ui.deb.Deb
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.DebIconImage
import com.daryan.tessera.ui.deb.utSurface
import com.daryan.tessera.ui.deb.utBorderBrush
import kotlin.math.roundToInt

/*
 * The iPad-style shell of the Linux desktop: a floating glass dock, page dots for the desktops,
 * and a Control Center. Everything that has no tile in the DEB Launcher icon pack uses a tile
 * drawn for it (tools/design/ipad_tiles) in the pack's geometry and palette.
 */

/** Tiles drawn for the shell, after the pack's own: same slab, same body, same rim. */
enum class IpadTile(@DrawableRes val png: Int, @DrawableRes val vector: Int, val label: String) {
    Airplane(R.drawable.ut_airplane_outline_text, R.drawable.ut_airplane_outline_text, "Airplane"),
    Cellular(R.drawable.ut_network_outline_text, R.drawable.ut_network_outline_text, "Cellular"),
    OrientationLock(R.drawable.ut_lock_outline_text, R.drawable.ut_lock_outline_text, "Rotation"),
    Mirror(R.drawable.ut_desktop_outline_text, R.drawable.ut_desktop_outline_text, "Mirroring"),
    Sun(R.drawable.ut_brightness_outline_text, R.drawable.ut_brightness_outline_text, "Brightness"),
    Volume(R.drawable.ut_volume_outline_text, R.drawable.ut_volume_outline_text, "Volume"),
    Flashlight(R.drawable.ut_brightness_outline_text, R.drawable.ut_brightness_outline_text, "Flashlight"),
    Calculator(R.drawable.ut_cpu_outline_text, R.drawable.ut_cpu_outline_text, "Calculator"),
    Camera(R.drawable.ut_desktop_outline_text, R.drawable.ut_desktop_outline_text, "Camera"),
    Wallpaper(R.drawable.ut_paint_outline_text, R.drawable.ut_paint_outline_text, "Wallpaper"),
    Keyboard(R.drawable.ut_terminal_outline_text, R.drawable.ut_terminal_outline_text, "Keyboard"),
    Desktop(R.drawable.ut_desktop_outline_text, R.drawable.ut_desktop_outline_text, "Desktop"),
    Multitask(R.drawable.ut_sessions_outline_text, R.drawable.ut_sessions_outline_text, "Windows"),
    LowPower(R.drawable.ut_activity_outline_text, R.drawable.ut_activity_outline_text, "Low power"),
    Power(R.drawable.ut_stop_outline_text, R.drawable.ut_stop_outline_text, "End"),
}

@Composable
fun IpadTileImage(tile: IpadTile, size: Dp, modifier: Modifier = Modifier, alpha: Float = 1f) {
    Image(
        painter = painterResource(if (Deb.extras.dark) tile.png else tile.vector),
        contentDescription = tile.label,
        modifier = modifier.size(size),
        alpha = alpha,
    )
}

/** The glass of the iPad shell: more opaque than the app's, since windows move behind it. */
fun Modifier.ipadSurface(radius: Dp): Modifier = this.utSurface(RoundedCornerShape(radius), raised = true)

/**
 * The desktops as an iPad's home screen pages: dots in a small glass capsule, the one in view
 * white. Tapping a dot goes to that desktop.
 */
@Composable
fun PageDots(count: Int, current: Int, vertical: Boolean, onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val dot = @Composable { n: Int ->
        val on = n == current
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp).clip(CircleShape).clickable(onClickLabel = "Desktop ${n + 1}") { onPick(n) },
        ) {
            Box(
                Modifier.size(if (on) 9.dp else 7.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (on) 0.95f else 0.38f)),
            )
        }
    }
    if (vertical) Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { repeat(count) { dot(it) } }
    else Row(modifier, verticalAlignment = Alignment.CenterVertically) { repeat(count) { dot(it) } }
}

private fun Context.open(intent: Intent): Boolean =
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess

private fun torchId(context: Context): String? = runCatching {
    val cm = context.getSystemService(CameraManager::class.java)
    cm.cameraIdList.firstOrNull { id ->
        val c = cm.getCameraCharacteristics(id)
        c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true && c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
    }
}.getOrNull()

/** A tile as a button: lit with a soft glow when [on]. */
@Composable
private fun CcButton(label: String, on: Boolean = false, size: Dp = 64.dp, onClick: () -> Unit, tile: @Composable () -> Unit) {
    val glow = MaterialTheme.colorScheme.primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(size + 8.dp).clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size).background(
                if (on) UColors.Pressed else UColors.Raised, RoundedCornerShape(14.dp)
            ),
        ) { tile() }
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
            color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** A tall glass slider, filled from below as on an iPad; [tile] sits at its foot. */
@Composable
private fun VerticalSlider(value: Float, onChange: (Float) -> Unit, onFinish: () -> Unit, modifier: Modifier, tile: @Composable () -> Unit) {
    var height by remember { mutableStateOf(1) }
    val change by rememberUpdatedState(onChange)
    val finish by rememberUpdatedState(onFinish)
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier.clip(shape).background(UColors.Surface).onSizeChanged { height = it.height.coerceAtLeast(1) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    change((1f - down.position.y / height).coerceIn(0f, 1f))
                    do {
                        val event = awaitPointerEvent()
                        event.changes.firstOrNull()?.let {
                            change((1f - it.position.y / height).coerceIn(0f, 1f))
                            it.consume()
                        }
                    } while (event.changes.any { it.pressed })
                    finish()
                }
            },
    ) {
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(value.coerceIn(0f, 1f))
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.92f), Color(0xFFB9D8FF).copy(alpha = 0.85f)))),
        )
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) { tile() }
    }
}

/**
 * The Control Center: connectivity, brightness and volume, the usual shortcuts, and what the
 * desktop adds (keyboard, show desktop, every window, wallpaper, size, settings, end).
 */
@Composable
fun ControlCenter(
    actions: DesktopActions,
    showingDesktop: Boolean,
    onShowDesktop: () -> Unit,
    onOverview: () -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val colors = MaterialTheme.colorScheme
    val audio = remember { context.getSystemService(AudioManager::class.java) }
    val maxVolume = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volume by remember { mutableStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume) }
    val window = activity?.window
    var brightness by remember {
        val own = window?.attributes?.screenBrightness ?: -1f
        mutableStateOf(
            if (own >= 0f) own
            else runCatching { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f }.getOrDefault(0.5f),
        )
    }
    var scale by remember { mutableStateOf(actions.scale().toFloat()) }
    var locked by remember { mutableStateOf(activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LOCKED) }
    val torch = remember { torchId(context) }
    var torchOn by remember { mutableStateOf(false) }
    var armed by remember { mutableStateOf(false) }

    fun go(intent: Intent) {
        onDone()
        context.open(intent)
    }
    fun settings(action: String) = go(Intent(action))

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.width(344.dp).clip(RoundedCornerShape(34.dp))
            .background(UColors.Raised.copy(alpha = 0.95f), RoundedCornerShape(34.dp))
            .padding(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().height(172.dp)) {
            // Connectivity: four tiles in one glass block.
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.weight(1.5f).fillMaxHeight().ipadSurface(26.dp).padding(vertical = 6.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CcButton("Airplane", size = 52.dp, onClick = { settings(Settings.ACTION_AIRPLANE_MODE_SETTINGS) }) { IpadTileImage(IpadTile.Airplane, 52.dp) }
                    CcButton("Cellular", size = 52.dp, onClick = { settings(Settings.ACTION_WIRELESS_SETTINGS) }) { IpadTileImage(IpadTile.Cellular, 52.dp) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CcButton("Wi-Fi", size = 52.dp, onClick = { settings(Settings.ACTION_WIFI_SETTINGS) }) { DebIconImage(DebIcon.UtilWifi, size = 52.dp) }
                    CcButton("Bluetooth", size = 52.dp, onClick = { settings(Settings.ACTION_BLUETOOTH_SETTINGS) }) { DebIconImage(DebIcon.UtilBluetooth, size = 52.dp) }
                }
            }
            VerticalSlider(
                value = brightness,
                onChange = {
                    brightness = it
                    // This window's own brightness, as a video app sets it; the phone's setting stays.
                    window?.let { w -> w.attributes = w.attributes.apply { screenBrightness = it.coerceAtLeast(0.02f) } }
                },
                onFinish = {},
                modifier = Modifier.weight(0.62f).fillMaxHeight(),
            ) { IpadTileImage(IpadTile.Sun, 44.dp) }
            VerticalSlider(
                value = volume,
                onChange = {
                    volume = it
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, (it * maxVolume).roundToInt(), 0)
                },
                onFinish = {},
                modifier = Modifier.weight(0.62f).fillMaxHeight(),
            ) { IpadTileImage(IpadTile.Volume, 44.dp) }
        }

        Row(Modifier.fillMaxWidth().ipadSurface(26.dp).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            CcButton("Rotation", on = locked, onClick = {
                locked = !locked
                activity?.requestedOrientation = if (locked) ActivityInfo.SCREEN_ORIENTATION_LOCKED else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }) { IpadTileImage(IpadTile.OrientationLock, 64.dp) }
            CcButton("Focus", onClick = { settings(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) }) { DebIconImage(DebIcon.UtilAltMoon, size = 64.dp) }
            CcButton("Mirroring", onClick = { settings(Settings.ACTION_CAST_SETTINGS) }) { IpadTileImage(IpadTile.Mirror, 64.dp) }
            CcButton("Low power", onClick = { settings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }) { IpadTileImage(IpadTile.LowPower, 64.dp) }
        }

        Row(Modifier.fillMaxWidth().ipadSurface(26.dp).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            CcButton("Flashlight", on = torchOn, onClick = {
                val id = torch
                if (id != null) {
                    val next = !torchOn
                    if (runCatching { context.getSystemService(CameraManager::class.java).setTorchMode(id, next) }.isSuccess) torchOn = next
                }
            }) { IpadTileImage(IpadTile.Flashlight, 64.dp) }
            CcButton("Clock", onClick = { go(Intent(AlarmClock.ACTION_SHOW_ALARMS)) }) { DebIconImage(DebIcon.UtilAltClock, size = 64.dp) }
            CcButton("Calculator", onClick = { go(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALCULATOR)) }) { IpadTileImage(IpadTile.Calculator, 64.dp) }
            CcButton("Camera", onClick = { go(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) }) { IpadTileImage(IpadTile.Camera, 64.dp) }
        }

        // What belongs to the Linux desktop.
        Row(Modifier.fillMaxWidth().ipadSurface(26.dp).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            CcButton("Keyboard", onClick = { onDone(); actions.toggleKeyboard() }) { IpadTileImage(IpadTile.Keyboard, 64.dp) }
            CcButton("Desktop", on = showingDesktop, onClick = { onDone(); onShowDesktop() }) { IpadTileImage(IpadTile.Desktop, 64.dp) }
            CcButton("Windows", onClick = { onDone(); onOverview() }) { IpadTileImage(IpadTile.Multitask, 64.dp) }
            CcButton("Wallpaper", onClick = { onDone(); actions.pickWallpaper() }) { IpadTileImage(IpadTile.Wallpaper, 64.dp) }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().ipadSurface(26.dp).padding(start = 10.dp, end = 16.dp),
        ) {
            IpadTileImage(IpadTile.Desktop, 40.dp)
            Slider(
                value = scale,
                onValueChange = { scale = it },
                // The screen changes size once the slider rests, not on every step.
                onValueChangeFinished = { actions.setScale(scale.roundToInt()) },
                valueRange = X11Scale.MIN.toFloat()..X11Scale.MAX.toFloat(),
                steps = (X11Scale.MAX - X11Scale.MIN) / 25 - 1,
                modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
            )
            Text("${scale.roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().ipadSurface(26.dp).padding(vertical = 8.dp)) {
            CcButton("Settings", size = 52.dp, onClick = { onDone(); actions.openSettings() }) { DebIconImage(DebIcon.NavSettings, size = 52.dp) }
            // Two taps, as the Start menu's End: the first asks.
            CcButton(if (armed) "End desktop?" else "End", on = armed, size = 52.dp, onClick = { if (armed) actions.end() else armed = true }) {
                IpadTileImage(IpadTile.Power, 52.dp)
            }
        }
    }
}
