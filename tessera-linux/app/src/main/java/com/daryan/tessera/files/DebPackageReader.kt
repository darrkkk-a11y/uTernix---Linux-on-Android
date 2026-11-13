// SPDX-License-Identifier: MIT
package com.daryan.tessera.files

import org.tukaani.xz.XZInputStream
import java.io.BufferedInputStream
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.zip.GZIPInputStream

/** Reads only Debian package metadata; never extracts or executes package files. */
internal object DebPackageReader {
    private const val MAX_CONTROL_ARCHIVE = 64L * 1024 * 1024
    private const val MAX_TAR_READ = 64L * 1024 * 1024
    private const val MAX_CONTROL_FILE = 1024 * 1024
    private const val TAR_BLOCK = 512
    private val packageName = Regex("[a-z0-9][a-z0-9+.-]{0,127}")
    private val architecture = Regex("[a-z0-9][a-z0-9-]{0,31}")

    fun read(file: File): DebInfo = RandomAccessFile(file, "r").use { ar ->
        val magic = ByteArray(8)
        try { ar.readFully(magic) } catch (_: EOFException) { throw IOException("File is too short to be a Debian package") }
        if (!magic.contentEquals("!<arch>\n".toByteArray(Charsets.US_ASCII))) throw IOException("Not a Debian ar archive")
        var versionSeen = false
        var members = 0
        while (ar.filePointer < ar.length()) {
            if (++members > 4096 || ar.length() - ar.filePointer < 60) throw IOException("Invalid Debian archive header")
            val header = ByteArray(60)
            ar.readFully(header)
            if (header[58] != '`'.code.toByte() || header[59] != '\n'.code.toByte()) throw IOException("Invalid Debian archive header")
            val name = ascii(header, 0, 16).trim().removeSuffix("/")
            val sizeText = ascii(header, 48, 10).trim()
            if (sizeText.isEmpty() || sizeText.any { it !in '0'..'9' }) throw IOException("Invalid Debian archive member size")
            val size = sizeText.toLongOrNull() ?: throw IOException("Debian archive member is too large")
            val start = ar.filePointer
            if (size > ar.length() - start) throw IOException("Truncated Debian archive member")
            val next = start + size + (size % 2)
            if (next < start || next > ar.length()) throw IOException("Truncated Debian archive padding")
            if (name == "debian-binary") {
                if (size != 4L) throw IOException("Unsupported Debian package version")
                val value = ByteArray(4)
                ar.readFully(value)
                if (!value.contentEquals("2.0\n".toByteArray(Charsets.US_ASCII))) throw IOException("Unsupported Debian package version")
                versionSeen = true
            } else if (name.startsWith("control.tar")) {
                if (!versionSeen) throw IOException("Missing Debian package version")
                if (size > MAX_CONTROL_ARCHIVE) throw IOException("Debian control archive is too large")
                val input = BufferedInputStream(ArSlice(ar, size))
                val decoded = when (name) {
                    "control.tar" -> input
                    "control.tar.gz" -> GZIPInputStream(input)
                    "control.tar.xz" -> XZInputStream(input, 64 * 1024)
                    else -> throw IOException("Unsupported control archive compression: $name")
                }
                return@use decoded.use(::readTar)
            }
            ar.seek(next)
        }
        throw IOException("Debian control archive was not found")
    }

    private fun readTar(input: InputStream): DebInfo {
        var consumed = 0L
        repeat(4096) {
            val header = ByteArray(TAR_BLOCK)
            readFully(input, header)
            consumed += TAR_BLOCK
            if (header.all { it == 0.toByte() }) throw IOException("Debian control file was not found")
            val expected = octal(header, 148, 8)
            val actual = header.indices.sumOf { i -> if (i in 148..155) 32L else (header[i].toInt() and 0xff).toLong() }
            if (expected != actual) throw IOException("Invalid control archive checksum")
            val size = octal(header, 124, 12)
            if (size > MAX_TAR_READ - consumed) throw IOException("Debian control archive is too large")
            var name = field(header, 0, 100)
            val prefix = field(header, 345, 155)
            if (prefix.isNotEmpty()) name = "$prefix/$name"
            while (name.startsWith("./")) name = name.substring(2)
            val type = header[156].toInt() and 0xff
            if (name == "control" && (type == 0 || type == '0'.code)) {
                if (size > MAX_CONTROL_FILE) throw IOException("Debian control file is too large")
                val data = ByteArray(size.toInt())
                readFully(input, data)
                return parseControl(String(data, Charsets.UTF_8))
            }
            val padded = size + ((TAR_BLOCK - size % TAR_BLOCK) % TAR_BLOCK)
            if (padded > MAX_TAR_READ - consumed) throw IOException("Debian control archive is too large")
            skipFully(input, padded)
            consumed += padded
        }
        throw IOException("Too many files in Debian control archive")
    }

    private fun parseControl(text: String): DebInfo {
        val fields = mutableMapOf<String, String>()
        for (line in text.lineSequence()) {
            if (line.isBlank()) break
            if (line.startsWith(' ') || line.startsWith('\t')) continue
            val colon = line.indexOf(':')
            if (colon <= 0) throw IOException("Invalid Debian control field")
            fields.putIfAbsent(line.substring(0, colon), line.substring(colon + 1).trim())
        }
        val pkg = fields["Package"].orEmpty()
        val version = fields["Version"].orEmpty()
        val arch = fields["Architecture"].orEmpty()
        if (!packageName.matches(pkg) || version.isEmpty() || version.length > 255 || version.any { it.isWhitespace() } || !architecture.matches(arch)) {
            throw IOException("Missing or invalid Debian package metadata")
        }
        return DebInfo(pkg, version, fields["Description"].orEmpty(), arch)
    }

    private fun field(bytes: ByteArray, start: Int, length: Int): String =
        ascii(bytes, start, length).substringBefore('\u0000').trim()

    private fun ascii(bytes: ByteArray, start: Int, length: Int): String = String(bytes, start, length, Charsets.US_ASCII)

    private fun octal(bytes: ByteArray, start: Int, length: Int): Long {
        val value = field(bytes, start, length)
        if (value.isEmpty()) return 0
        if (value.any { it !in '0'..'7' }) throw IOException("Invalid control archive number")
        return value.toLongOrNull(8) ?: throw IOException("Control archive number is too large")
    }

    private fun readFully(input: InputStream, out: ByteArray) {
        var offset = 0
        while (offset < out.size) {
            val n = input.read(out, offset, out.size - offset)
            if (n < 0) throw IOException("Truncated Debian control archive")
            if (n == 0) continue
            offset += n
        }
    }

    private fun skipFully(input: InputStream, size: Long) {
        var remaining = size
        val buf = ByteArray(8192)
        while (remaining > 0) {
            val n = input.read(buf, 0, minOf(remaining, buf.size.toLong()).toInt())
            if (n < 0) throw IOException("Truncated Debian control archive")
            if (n == 0) continue
            remaining -= n
        }
    }

    private class ArSlice(private val ar: RandomAccessFile, private var remaining: Long) : InputStream() {
        override fun read(): Int {
            if (remaining == 0L) return -1
            val value = ar.read()
            if (value >= 0) remaining--
            return value
        }
        override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            if (remaining == 0L) return -1
            val n = ar.read(bytes, offset, minOf(remaining, length.toLong()).toInt())
            if (n > 0) remaining -= n
            return n
        }
        override fun close() { /* The enclosing use owns the RandomAccessFile. */ }
    }
}
