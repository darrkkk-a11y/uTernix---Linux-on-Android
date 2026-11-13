#!/usr/bin/env python3
"""Stage ARM64 binaries/assets from the matching Tessera v0.7.0 release APK.

The APK is a build input, not proof of authenticity; its expected hash is pinned
to the release downloaded for this local build. The matching GPL source is in
../tessera-linux, at commit d5810dc0e6a3b98282f4d0fdf5a4ca21c6a2e355.
"""
import hashlib
from pathlib import Path, PurePosixPath
from zipfile import ZipFile

root = Path(__file__).resolve().parent.parent
apk = root / 'tessera-v0.7.0.apk'
project = root / 'tessera-linux'
expected = '90b99fd4c2b12a9e4440c7f23b333a00a344a05c602870f3142d382d737dc4d3'
if hashlib.sha256(apk.read_bytes()).hexdigest() != expected:
    raise SystemExit('Unexpected Tessera APK hash; refusing to stage binaries')

native = {'liblauncher.so', 'libproot.so', 'libproot-loader.so', 'libproot-loader32.so'}
asset_dirs = {'xkb': 'x11', 'gpu': 'gpu', 'theme': 'theme', 'usb': 'usb'}
count = 0
with ZipFile(apk) as z:
    for info in z.infolist():
        p = PurePosixPath(info.filename)
        if p.is_absolute() or '..' in p.parts or info.is_dir():
            continue
        parts = p.parts
        dest = None
        if len(parts) == 3 and parts[:2] == ('lib', 'arm64-v8a'):
            if parts[2] in native:
                dest = project / 'app/src/main/jniLibs/arm64-v8a' / parts[2]
            elif parts[2] == 'libXlorie.so':
                dest = project / 'build/x11/jniLibs/arm64-v8a' / parts[2]
        elif len(parts) >= 3 and parts[0] == 'assets' and parts[1] in asset_dirs:
            dest = project / 'build' / asset_dirs[parts[1]] / 'assets' / Path(*parts[1:])
        if dest is not None:
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(z.read(info))
            count += 1
print(f'Staged {count} files from Tessera v0.7.0')
