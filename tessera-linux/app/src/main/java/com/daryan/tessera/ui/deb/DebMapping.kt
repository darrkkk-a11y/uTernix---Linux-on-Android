// Copyright (C) 2026 Daryan Kamaran Karim
// SPDX-License-Identifier: GPL-3.0-or-later

package com.daryan.tessera.ui.deb

import com.daryan.tessera.container.PermissionManifest

/** Words in an app's id, name, packages or command, and the tile that stands for them. First match wins. */
private val APP_WORDS: List<Pair<List<String>, DebIcon>> = listOf(
    listOf("opencode") to DebIcon.AiOpencode,
    listOf("claude") to DebIcon.AiAssistant,
    listOf("firefox") to DebIcon.AppFirefox,
    listOf("python") to DebIcon.AppPython3,
    listOf("vscode", "code-oss", "codium", "visual studio") to DebIcon.AppVscode,
    listOf("nodejs", "node.js", "npm") to DebIcon.AppNodejs,
    listOf("vlc") to DebIcon.AppVlc,
    listOf("gimp") to DebIcon.AppGimp,
    listOf("electron") to DebIcon.AppElectron,
    listOf("libreoffice", "soffice") to DebIcon.AppLibreoffice,
    listOf("nano") to DebIcon.AppNano,
    listOf("htop") to DebIcon.AppHtop,
    listOf("docker", "podman") to DebIcon.AppDocker,
)

/** The tile of an app that has no icon of its own: by what it is, then by the launcher's icon key, then its category. */
fun debIconForApp(app: PermissionManifest): DebIcon {
    val haystack = buildString {
        append(app.id).append(' ').append(app.name).append(' ')
        append(app.packages.joinToString(" ")).append(' ')
        append(app.exec.joinToString(" "))
    }.lowercase()
    for ((words, icon) in APP_WORDS) if (words.any { it in haystack }) return icon
    if (Regex("(^|[\\s/_-])git(k|-|\\s|$)").containsMatchIn(haystack)) return DebIcon.AppGit
    when (app.id) {
        "bash" -> return DebIcon.NavTerminal
        "root-shell" -> return DebIcon.SysSecurity
        "xterm" -> return DebIcon.SysShell
        "desktop" -> return DebIcon.NavLinux
    }
    if (app.desktop) return DebIcon.NavLinux
    return when (app.icon) {
        "terminal" -> if (app.gui) DebIcon.SysShell else DebIcon.NavTerminal
        "desktop" -> DebIcon.NavLinux
        "monitor" -> DebIcon.SysMonitoring
        "code" -> DebIcon.UtilAltCodeBrackets
        "globe" -> DebIcon.UtilAltGlobe
        "palette" -> DebIcon.UtilTheme
        "folder" -> DebIcon.FileFolder
        "spark" -> DebIcon.AiAssistant
        else -> when (app.category.lowercase()) {
            "development" -> DebIcon.SysDevelopment
            "internet", "network" -> DebIcon.UtilAltGlobe
            "graphics" -> DebIcon.UtilTheme
            "system" -> DebIcon.SysServices
            "office" -> DebIcon.FileFile
            "audio", "video", "media", "multimedia" -> DebIcon.FileMp4
            "games" -> DebIcon.UtilAltLightning
            else -> DebIcon.NavApplications
        }
    }
}

/** The tile of a file by its extension; [directory] wins. */
fun debIconForFile(name: String, directory: Boolean): DebIcon {
    if (directory) return DebIcon.FileFolder
    val n = name.lowercase()
    val ext = n.substringAfterLast('.', "")
    return when {
        n.endsWith(".tar.gz") || n.endsWith(".tar.xz") || n.endsWith(".tar.bz2") || n.endsWith(".tar.zst") -> DebIcon.FileTar
        ext == "tar" -> DebIcon.FileTar
        ext in setOf("zip", "7z", "rar", "gz", "xz", "bz2", "zst", "jar") -> DebIcon.FileZip
        ext == "deb" -> DebIcon.FileDeb
        ext == "apk" -> DebIcon.FileAltApkPackage
        ext in setOf("iso", "img") -> DebIcon.FileIso
        ext in setOf("sh", "bash", "zsh", "fish") -> DebIcon.FileSh
        ext in setOf("conf", "cfg", "ini", "toml", "yaml", "yml", "service", "desktop", "list", "sources") -> DebIcon.FileConf
        ext == "py" -> DebIcon.FilePy
        ext in setOf("js", "mjs", "cjs", "ts") -> DebIcon.FileJs
        ext == "json" -> DebIcon.FileJson
        ext in setOf("html", "htm", "xhtml") -> DebIcon.FileHtml
        ext == "css" -> DebIcon.FileCss
        ext in setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "heic", "avif") -> DebIcon.FilePng
        ext in setOf("mp4", "mkv", "webm", "avi", "mov", "m4v", "mp3", "flac", "ogg", "opus", "wav", "m4a") -> DebIcon.FileMp4
        ext in setOf("txt", "md", "log", "rst", "csv") -> DebIcon.FileAltTxtFile
        else -> DebIcon.FileFile
    }
}
