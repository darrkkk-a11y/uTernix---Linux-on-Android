#!/usr/bin/env python3
"""Exercise the OpenCode adapter in an isolated local directory.

Uses the real, checksum-pinned input archives and .deb, but replaces apt and
network downloads with local test doubles. Does not install host packages.
The output stays under /tmp/opencode/install-e2e for the GUI smoke test.
"""
import os
from pathlib import Path
import subprocess

work = Path('/root/termai-re-lab/work')
root = Path('/tmp/opencode/install-e2e')
if root.exists():
    raise SystemExit(f'Refusing to overwrite existing test directory: {root}')
bin_dir = root / 'bin'
bin_dir.mkdir(parents=True)

def tool(name, body):
    path = bin_dir / name
    path.write_text(body)
    path.chmod(0o755)

tool('apt-get', '''#!/bin/sh
printf '%s\\n' "$*" >> "$TEST_APT_LOG"
''')
tool('curl', '''#!/bin/sh
set -eu
url= out=
while [ "$#" -gt 0 ]; do
  case "$1" in
    --retry|--connect-timeout) shift 2;;
    -o) out=$2; shift 2;;
    -*) shift;;
    *) url=$1; shift;;
  esac
done
case "$url" in
  *electron-v44.9.1-linux-arm64.zip) src="$FIXTURES/electron-v44.9.1-linux-arm64.zip";;
  *cli-linux-arm64-2.2.6.tgz) src="$FIXTURES/opencode-cli-linux-arm64-2.2.6.tgz";;
  *node-pty-linux-arm64-1.2.0-beta.12.tgz) src="$FIXTURES/node-pty-linux-arm64-1.2.0-beta.12.tgz";;
  *) echo 'Unexpected test download' >&2; exit 2;;
esac
cp -- "$src" "$out"
''')
env = os.environ.copy()
env.update({
    'PATH': f'{bin_dir}:/usr/bin:/bin',
    'TEST_APT_LOG': str(root / 'apt.log'),
    'FIXTURES': str(work / 'build-tools-local/downloads'),
    'DEB_LAUNCHER_APPS_DIR': str(root / 'apps'),
    'DEB_LAUNCHER_DESKTOP_DIR': str(root / 'desktop'),
    'DEB_LAUNCHER_RUNTIME_DIR': str(work / 'deb-launcher/guest'),
})
command = ['/bin/sh', str(work / 'deb-launcher/guest/install-opencode-arm64'),
           '/tmp/opencode/deb-analysis/opencode-desktop-linux-amd64.deb']
log = root / 'install.log'
with log.open('w') as f:
    result = subprocess.run(command, env=env, stdout=f, stderr=subprocess.STDOUT, timeout=600)
if result.returncode:
    raise SystemExit(f'Installer failed ({result.returncode}); see {log}')
app = root / 'apps/opencode-2.2.6'
assert (app / '.deb-launcher-ready').is_file()
assert (app / 'resources/app/node_modules/@lydell/node-pty-linux-arm64/prebuilds/linux-arm64/pty.node').is_file()
assert (root / 'desktop/deb-launcher-opencode.desktop').is_file()
before = (root / 'apt.log').read_text()
with log.open('a') as f:
    again = subprocess.run(command, env=env, stdout=f, stderr=subprocess.STDOUT, timeout=60)
assert again.returncode == 0
assert (root / 'apt.log').read_text() == before, 'Retry unexpectedly ran apt again'
print('Isolated install passed, including idempotent retry:', app)
