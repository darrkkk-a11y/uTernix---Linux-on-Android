#!/usr/bin/env python3
"""Stage the locally built Box64 and guest scripts as Tessera build assets."""
from pathlib import Path
import hashlib
import shutil
import sys

root = Path(__file__).resolve().parent
work = root.parent
assets = work / 'tessera-linux/build/deb-launcher/assets/deb-launcher'
sources = {
    'box64': work / 'box64-build/box64',
    'launch': root / 'guest/launch',
    'install-amd64': root / 'guest/install-amd64',
    'install-opencode-arm64': root / 'guest/install-opencode-arm64',
    'adapt-opencode-asar.py': root / 'guest/adapt-opencode-asar.py',
    'opencode-start': root / 'guest/opencode-start',
    'BOX64-LICENSE': work / 'box64-source/LICENSE',
    'TESSERA-LICENSE': work / 'tessera-linux/LICENSE',
    'SOURCE-NOTICE.txt': root / 'SOURCE-NOTICE.txt',
}
assets.mkdir(parents=True, exist_ok=True)
for name, source in sources.items():
    if not source.is_file():
        raise SystemExit(f'Missing build input: {source}')
    dest = assets / name
    if dest.exists():
        old = hashlib.sha256(dest.read_bytes()).digest()
        new = hashlib.sha256(source.read_bytes()).digest()
        if old != new:
            if '--refresh' not in sys.argv:
                raise SystemExit(f'Existing staged asset differs, not overwritten: {dest}; use --refresh for generated assets')
            shutil.copyfile(source, dest)
    else:
        shutil.copyfile(source, dest)
    print(name, dest.stat().st_size, 'bytes')
