// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SystemAppsIconTest {
    @Test
    fun scalableSvgIconResolvesFromLinuxRootfs() {
        val root = Files.createTempDirectory("uternix-svg-icon-").toFile()
        try {
            val path = File(root, "usr/share/icons/hicolor/scalable/apps/firefox.svg")
            path.parentFile?.mkdirs()
            path.writeText("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\"></svg>")
            assertEquals(path.canonicalPath, File(SystemApps.iconFile(root, "firefox")!!).canonicalPath)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun themedPngIconRetainsItsOriginalFile() {
        val root = Files.createTempDirectory("uternix-png-icon-").toFile()
        try {
            val path = File(root, "usr/share/icons/Adwaita/256x256/apps/gimp.png")
            path.parentFile?.mkdirs()
            path.writeBytes(byteArrayOf(1, 2, 3, 4, 5))
            assertEquals(path.canonicalPath, File(SystemApps.iconFile(root, "gimp")!!).canonicalPath)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun traversalIconValueCannotEscapeRootfs() {
        val root = Files.createTempDirectory("uternix-icon-safety-").toFile()
        try {
            assertNull(SystemApps.iconFile(root, "../../etc/shadow"))
            assertNull(SystemApps.iconFile(root, "adwaita/../../../etc/passwd"))
        } finally {
            root.deleteRecursively()
        }
    }
}
