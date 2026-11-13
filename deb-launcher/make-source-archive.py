#!/usr/bin/env python3
"""Collect corresponding sources without SDKs, caches, keys or user .debs."""
from pathlib import Path, PurePosixPath
import tarfile

work = Path(__file__).resolve().parent.parent
out = work / 'deb-launcher-0.7.1-sources.tar.gz'
if out.exists():
    raise SystemExit(f'Refusing to overwrite {out}')
entries = [
    'tessera-linux', 'box64-source', 'deb-launcher',
    'build-tools-local/proot-source', 'build-tools-local/README.md',
    'build-tools-local/stage-tessera.py', 'build-tools-local/build-launcher.sh',
    'build-tools-local/run-x86-64.sh', 'build-tools-local/build-high-proot-loader.sh',
    'build-tools-local/sdk-wrapper', 'build-tools-local/ndk-qemu',
]
excluded_parts = {'.git', '.gradle', 'build', '__pycache__', '.idea'}
excluded_names = {'keystore.properties', 'local.properties'}

def keep(info):
    parts = PurePosixPath(info.name).parts
    if any(p in excluded_parts for p in parts):
        return None
    if parts[-1] in excluded_names or parts[-1].endswith(('.keystore', '.jks')):
        return None
    return info

with tarfile.open(out, 'x:gz', compresslevel=5) as archive:
    for entry in entries:
        path = work / entry
        if not path.exists():
            raise SystemExit(f'Missing source: {path}')
        archive.add(path, arcname=entry, filter=keep)
print('Source archive:', out, 'bytes:', out.stat().st_size)
