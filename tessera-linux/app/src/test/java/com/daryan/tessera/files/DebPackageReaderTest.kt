// SPDX-License-Identifier: MIT
package com.daryan.tessera.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.zip.GZIPOutputStream

class DebPackageReaderTest {
    @get:Rule val tmp = TemporaryFolder()

    private val control = "Package: example-app\nVersion: 1.2+3\nArchitecture: amd64\nDescription: Example app\n extra description\n".toByteArray()

    private fun tar(content: ByteArray, declaredSize: Int = content.size): ByteArray {
        val header = ByteArray(512)
        fun put(at: Int, value: String) { value.toByteArray(Charsets.US_ASCII).copyInto(header, at) }
        put(0, "./control")
        put(100, "0000644\u0000")
        put(108, "0000000\u0000")
        put(116, "0000000\u0000")
        put(124, String.format(Locale.US, "%011o", declaredSize) + "\u0000")
        put(136, "00000000000\u0000")
        for (i in 148..155) header[i] = ' '.code.toByte()
        header[156] = '0'.code.toByte()
        put(257, "ustar\u0000")
        val sum = header.sumOf { it.toInt() and 0xff }
        put(148, String.format(Locale.US, "%06o", sum) + "\u0000 ")
        val out = ByteArrayOutputStream()
        out.write(header)
        out.write(content)
        repeat((512 - content.size % 512) % 512) { out.write(0) }
        repeat(1024) { out.write(0) }
        return out.toByteArray()
    }

    private fun ar(name: String, bytes: ByteArray, includeVersion: Boolean = true): File {
        val out = ByteArrayOutputStream()
        out.write("!<arch>\n".toByteArray())
        fun member(n: String, data: ByteArray) {
            val header = String.format(Locale.US, "%-16s%-12s%-6s%-6s%-8s%-10d`\n", "$n/", "0", "0", "0", "100644", data.size)
            assertEquals(60, header.toByteArray(Charsets.US_ASCII).size)
            out.write(header.toByteArray(Charsets.US_ASCII))
            out.write(data)
            if (data.size % 2 != 0) out.write('\n'.code)
        }
        if (includeVersion) member("debian-binary", "2.0\n".toByteArray())
        member(name, bytes)
        return tmp.newFile().apply { writeBytes(out.toByteArray()) }
    }

    private fun compressed(raw: ByteArray, xz: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        val writer = if (xz) XZOutputStream(out, LZMA2Options()) else GZIPOutputStream(out)
        writer.use { it.write(raw) }
        return out.toByteArray()
    }

    @Test fun `reads xz compressed package metadata without executing it`() {
        val info = DebPackageReader.read(ar("control.tar.xz", compressed(tar(control), xz = true)))
        assertEquals(DebInfo("example-app", "1.2+3", "Example app", "amd64"), info)
    }

    @Test fun `reads gzip and uncompressed control archives`() {
        for ((name, data) in listOf("control.tar.gz" to compressed(tar(control), xz = false), "control.tar" to tar(control))) {
            assertEquals("example-app", DebPackageReader.read(ar(name, data)).pkg)
        }
    }

    @Test fun `rejects missing version and unsupported compression`() {
        for (file in listOf(ar("control.tar", tar(control), includeVersion = false), ar("control.tar.zst", byteArrayOf(1)))) {
            val result = runCatching { DebPackageReader.read(file) }
            assertTrue(result.exceptionOrNull() is IOException)
        }
    }

    @Test fun `rejects corrupt and oversized control entries`() {
        val broken = tar(control).apply { this[20] = 3 }
        val oversized = tar(byteArrayOf(), declaredSize = 1024 * 1024 + 1)
        for (data in listOf(broken, oversized)) {
            assertTrue(runCatching { DebPackageReader.read(ar("control.tar", data)) }.exceptionOrNull() is IOException)
        }
    }

    @Test fun `reads the supplied OpenCode package when available`() {
        val path = System.getenv("DEB_LAUNCHER_TEST_DEB")
        assumeTrue(!path.isNullOrBlank() && File(path).isFile)
        val info = DebPackageReader.read(File(path!!))
        assertEquals("opencode", info.pkg)
        assertEquals("2.2.6", info.version)
        assertEquals("amd64", info.architecture)
    }
}
