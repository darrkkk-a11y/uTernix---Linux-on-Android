// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

package com.daryan.tessera.widgets

import com.daryan.tessera.ui.deb.debIconForApp
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.daryan.tessera.container.PermissionManifest
import com.daryan.tessera.ui.customIconColors

/**
 * App icons for the phone's widgets, which can't draw Compose: the app's own picture on a tile of
 * its color, or its first letter when it has none. Small, as all of a widget's bitmaps together
 * travel through a binder call with a size limit.
 */
object WidgetIcons {
    private const val SIDE = 112

    /** Tones for apps with no color of their own; an app keeps the same one everywhere. */
    private val TONES = intArrayOf(0xFF6750A4.toInt(), 0xFF00696B.toInt(), 0xFF8C4A60.toInt(), 0xFF4A6500.toInt(), 0xFF00639B.toInt(), 0xFF8B5000.toInt())

    fun badge(context: Context, app: PermissionManifest): Bitmap {
        val bitmap = Bitmap.createBitmap(SIDE, SIDE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val custom = customIconColors.value[app.id]
        val own = app.iconFile?.let { path -> runCatching { BitmapFactory.decodeFile(path) }.getOrNull() }
        val tile = tileBitmap(context, debIconForApp(app))
        if (custom == null && own == null && tile != null) {
            // The pack's own tile, edge to edge.
            canvas.drawBitmap(tile, null, Rect(0, 0, SIDE, SIDE), paint)
            return bitmap
        }
        paint.color = custom ?: 0xFF071B34.toInt()
        canvas.drawRoundRect(RectF(0f, 0f, SIDE.toFloat(), SIDE.toFloat()), SIDE * 0.22f, SIDE * 0.22f, paint)
        if (own != null) {
            val inset = SIDE / 8
            canvas.drawBitmap(own, null, Rect(inset, inset, SIDE - inset, SIDE - inset), paint)
        } else if (tile != null) {
            val inset = SIDE / 7
            canvas.drawBitmap(tile, null, Rect(inset, inset, SIDE - inset, SIDE - inset), paint)
        } else {
            paint.color = Color.WHITE
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = SIDE * 0.5f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(initial(app.name), SIDE / 2f, SIDE / 2f - (paint.descent() + paint.ascent()) / 2, paint)
        }
        return bitmap
    }

    /**
     * The pack's tile as a bitmap: the raster on a dark widget; on a light one the traced vector,
     * because some raster tiles carry a dark backing that only hides on a dark ground.
     */
    private fun tileBitmap(context: Context, icon: com.daryan.tessera.ui.deb.DebIcon): Bitmap? {
        val night = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (night) return runCatching { BitmapFactory.decodeResource(context.resources, icon.png) }.getOrNull()
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, icon.vector) ?: return null
        val bitmap = Bitmap.createBitmap(SIDE, SIDE, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, SIDE, SIDE)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    /** The letter that stands for an app with no picture. */
    internal fun initial(name: String): String =
        name.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"
}
