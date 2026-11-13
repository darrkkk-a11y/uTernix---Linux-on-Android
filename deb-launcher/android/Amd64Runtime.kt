// SPDX-License-Identifier: MIT
package com.daryan.tessera.container

import android.content.Context
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/** Installs the bundled translator into Tessera's PRoot guest, not Android's filesystem. */
object Amd64Runtime {
    private const val RELATIVE_DIR = "opt/deb-launcher/box64-0.4.4-v2"
    const val GUEST_DIR = "/$RELATIVE_DIR"
    const val GUEST_LAUNCH = "$GUEST_DIR/launch"
    const val GUEST_INSTALL = "$GUEST_DIR/install-amd64"

    /** Call on an IO thread. Existing, different files are never overwritten. */
    @Synchronized
    fun prepare(context: Context, rootfs: File) {
        require(rootfs.isDirectory) { "The Linux guest is not ready" }
        val root = rootfs.canonicalFile
        val dir = File(root, RELATIVE_DIR)
        require(dir.canonicalPath.startsWith(root.path + File.separator)) {
            "The runtime directory escapes the Linux guest"
        }
        check(dir.isDirectory || dir.mkdirs()) { "Cannot create the guest runtime directory" }
        require(dir.canonicalPath.startsWith(root.path + File.separator)) {
            "The runtime directory escapes the Linux guest"
        }
        for (name in listOf("box64", "launch", "install-amd64", "install-opencode-arm64", "adapt-opencode-asar.py", "opencode-start")) {
            val asset = "deb-launcher/$name"
            val dest = File(dir, name)
            require(dest.canonicalFile.parentFile == dir.canonicalFile) {
                "Runtime file $name escapes its directory"
            }
            val expected = context.assets.open(asset).use(::digest)
            if (dest.exists()) {
                check(dest.isFile && dest.inputStream().use(::digest).contentEquals(expected)) {
                    "Existing runtime file $name differs; it was not overwritten"
                }
            } else {
                check(dest.createNewFile()) { "Cannot reserve runtime file $name" }
                try {
                    context.assets.open(asset).use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                    check(dest.inputStream().use(::digest).contentEquals(expected)) {
                        "Runtime file $name failed verification"
                    }
                } catch (e: Exception) {
                    // This file was created in this call; never remove a pre-existing one.
                    dest.delete()
                    throw e
                }
            }
            check(dest.setExecutable(true, false) || dest.canExecute()) {
                "Cannot make runtime file $name executable"
            }
        }
    }

    private fun digest(input: InputStream): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return md.digest()
            md.update(buffer, 0, read)
        }
    }
}
