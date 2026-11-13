// SPDX-License-Identifier: MIT
package com.daryan.tessera.files

import java.io.File
import java.io.IOException

/** Copies an explicitly selected Android file into the app's PRoot guest. */
internal object DebStaging {
    private const val GUEST_DIR = "/var/tmp/tessera-debs"

    /** Run on an IO thread. A unique destination prevents overwriting another package. */
    fun copy(source: File, rootfs: File, packageName: String): String {
        if (!source.isFile || !source.canRead()) throw IOException("Cannot read the selected Android file")
        val root = rootfs.canonicalFile
        if (!root.isDirectory) throw IOException("The Linux system is not installed")
        val directory = File(root, GUEST_DIR.removePrefix("/"))
        fun checkDirectory() {
            if (!directory.canonicalPath.startsWith(root.path + File.separator)) {
                throw IOException("The staging directory is outside the Linux system")
            }
        }
        checkDirectory()
        val created = !directory.isDirectory && directory.mkdirs()
        if (!directory.isDirectory) throw IOException("Cannot create the package staging directory")
        checkDirectory()
        if (created) {
            // Match the existing guest staging path's broad accessibility for APT's _apt user.
            directory.setReadable(true, false)
            directory.setWritable(true, false)
            directory.setExecutable(true, false)
        }
        val prefix = packageName.replace(Regex("[^A-Za-z0-9._+-]"), "_").take(48).ifEmpty { "package" }
        val staged = File.createTempFile("tessera-${prefix}-", ".deb", directory)
        try {
            source.inputStream().use { input -> staged.outputStream().use { output -> input.copyTo(output) } }
            if (!staged.setReadable(true, false) && !staged.canRead()) throw IOException("Cannot read the staged package")
        } catch (e: Exception) {
            // This unique file was created by this call, not supplied by the user.
            staged.delete()
            throw e
        }
        return "$GUEST_DIR/${staged.name}"
    }
}
