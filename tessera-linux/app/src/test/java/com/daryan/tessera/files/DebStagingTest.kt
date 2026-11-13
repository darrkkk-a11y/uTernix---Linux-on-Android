// SPDX-License-Identifier: MIT
package com.daryan.tessera.files

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeNoException
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.file.Files

class DebStagingTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun `copies selected file without overwriting it or earlier staged files`() {
        val root = tmp.newFolder("rootfs")
        val source = tmp.newFile("original.deb").apply { writeBytes(byteArrayOf(0, 1, 2, 3, -1)) }
        val first = DebStaging.copy(source, root, "example")
        val second = DebStaging.copy(source, root, "example")
        assertNotEquals(first, second)
        assertTrue(first.startsWith("/var/tmp/tessera-debs/"))
        assertTrue(first.endsWith(".deb"))
        assertArrayEquals(source.readBytes(), File(root, first.removePrefix("/")).readBytes())
        assertArrayEquals(source.readBytes(), File(root, second.removePrefix("/")).readBytes())
        assertTrue(source.isFile)
    }

    @Test fun `refuses a staging directory redirected outside the guest`() {
        val root = tmp.newFolder("rootfs")
        val outside = tmp.newFolder("outside")
        val source = tmp.newFile("original.deb").apply { writeText("data") }
        try {
            Files.createSymbolicLink(File(root, "var").toPath(), outside.toPath())
        } catch (e: Exception) {
            assumeNoException(e)
        }
        assertTrue(runCatching { DebStaging.copy(source, root, "example") }.exceptionOrNull() is IOException)
        assertFalse(File(outside, "tmp/tessera-debs").exists())
        assertTrue(source.isFile)
    }

    @Test fun `refuses to stage without an installed guest`() {
        val source = tmp.newFile("original.deb").apply { writeText("data") }
        assertTrue(runCatching { DebStaging.copy(source, File(tmp.root, "missing"), "example") }.exceptionOrNull() is IOException)
    }
}
