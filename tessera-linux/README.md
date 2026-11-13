# uTernix Android application

This directory contains the Android Gradle project powering **uTernix**. Run Gradle from here after staging the native runtime and Android SDK dependencies described in the [root README](../README.md).

- Main app: `app/` (Jetpack Compose / Kotlin)
- Native display: `third_party/termux-x11/`
- Native staging/build scripts: `scripts/`

## Upstream attribution

uTernix extends [Tessera](https://github.com/daryan044/tessera-linux), licensed under GPL-3.0-or-later. The original upstream project documents are available from the linked repository. This fork retains its source, copyright notices and applicable licenses.

The project is distributed as source and development builds; the current uTernix sources have not yet been verified in an Android SDK build in this preparation environment.
