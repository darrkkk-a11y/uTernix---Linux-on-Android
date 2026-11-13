# DEB Launcher for Android (development build)

The current Android project is `../tessera-linux` (launcher version 0.7.1). It is a modified Tessera
0.7.0, using its PRoot Debian guest and built-in Termux:X11 display. The APK
has a separate application ID, `lab.deblauncher.full`, so it does not overwrite
an existing Tessera installation. Android 7+ and an ARM64 device are required
for this build.

## Capabilities and limits

- Debian metadata for selected Android-storage files is read directly, without
  needing the Linux shell. The package is copied to a unique guest staging file;
  the selected original is never modified. Debian in PRoot is still required
  to install and run packages.
- Selecting an ARM64 `.deb` follows Tessera's normal APT installation path.
- Selecting an amd64 `.deb` on ARM64 enables Debian multiarch and attempts to
  start direct x86-64 ELF executables with Box64 0.4.4. Compatibility is not
  universal; scripts that later execute an x86-64 binary, some binaries with
  fixed load addresses, and some Electron/Chromium versions can fail.
- The specific supplied OpenCode Desktop 2.2.6 package, SHA-256
  `87f66555f3a3fec3901ef7e1ac86f5e2deb55b90004cf42396e7547c6b195459`,
  takes a separate path: it is extracted without running its Debian maintainer
  scripts and adapted to ARM64 Electron 44.9.1, ARM64 OpenCode CLI 2.2.6 and
  ARM64 node-pty. This downloads about 250 MB in addition to Debian setup and
  installs dependencies through APT. It creates a `.desktop` entry rather than
  registering the original `.deb` with dpkg.
- Other OpenCode versions fall back to Box64. They are not covered by the
  special adapter and may not work.

The Android APK has **not been tested on a connected device**. The native
OpenCode adaptation was tested in an isolated ARM64 Linux/Xvfb session: a
window appeared, the CLI service reported ready, and the ARM64 terminal module
passed a smoke test. That does not prove Android compatibility.

Only install trusted packages: APT normally runs package maintainer scripts.
PRoot is not a security boundary; a Linux application runs with the Android
application's permissions. Electron's sandbox is disabled in PRoot mode.

## Build inputs and licenses

- Tessera: GPL-3.0-or-later, source in `../tessera-linux`, upstream
  `https://github.com/daryan044/tessera-linux`, commit
  `d5810dc0e6a3b98282f4d0fdf5a4ca21c6a2e355`, with its Termux:X11 submodules.
- Box64: MIT, source in `../box64-source`, tag `v0.4.4`; its license is staged
  into the APK. `../box64-build/box64` was compiled from that source.
- Matching Tessera release binaries are staged by
  `../build-tools-local/stage-tessera.py`; the script pins their APK hash.
- Electron, OpenCode CLI, and node-pty are obtained at installation time from
  their official release/package hosts and verified against pinned hashes.
- Guest scripts and Android integration added here are MIT-licensed; the
  combined Android project remains subject to Tessera's GPL license.

`stage-runtime.py --refresh` prepares generated APK assets from the local
Box64 build and guest scripts. The local SDK/QEMU wrappers are documented in
`../build-tools-local/README.md`. For the exact Gradle invocation see
`../build-tools-local/build-launcher.sh`.

## Tests

Run `python3 -m unittest discover -s tests -v` for guest-script checks. The
Gradle `:app:testDebugUnitTest` task covers the Android project. The optional
`tests/run_install_smoke.py` needs the named local archives and supplied `.deb`;
it mocks APT and downloads and writes only under `/tmp/opencode/install-e2e`.
