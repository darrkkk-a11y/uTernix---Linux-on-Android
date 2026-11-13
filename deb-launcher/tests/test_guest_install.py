import os
from pathlib import Path
import subprocess
import tempfile
import unittest

INSTALL = Path(__file__).resolve().parents[1] / 'guest/install-amd64'


class InstallTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.bin = self.root / 'bin'
        self.bin.mkdir()
        self.stage = self.root / 'staging'
        self.stage.mkdir()
        self.deb = self.stage / 'example.deb'
        self.deb.write_bytes(b'not a real package')
        self.log = self.root / 'commands'
        self.env = os.environ.copy()
        self.env.update(PATH=f'{self.bin}:/usr/bin:/bin',
                        DEB_LAUNCHER_STAGING=str(self.stage), TEST_LOG=str(self.log))
        self.tool('dpkg', '''#!/bin/sh
case "$1" in
--print-architecture) echo arm64;;
--print-foreign-architectures) :;;
--add-architecture) printf 'add %s\\n' "$2" >> "$TEST_LOG";;
*) exit 3;;
esac
''')
        self.tool('dpkg-deb', '#!/bin/sh\necho amd64\n')
        self.tool('apt-get', '#!/bin/sh\nprintf "apt <%s>\\n" "$*" >> "$TEST_LOG"\n')

    def tearDown(self):
        self.temp.cleanup()

    def tool(self, name, text):
        path = self.bin / name
        path.write_text(text)
        path.chmod(0o755)

    def run_install(self, *args):
        return subprocess.run(['/bin/sh', str(INSTALL), *map(str, args)], env=self.env,
                              capture_output=True, text=True)

    def test_install_uses_safe_apt_arguments(self):
        result = self.run_install(self.deb)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.log.read_text().splitlines(), [
            'add amd64', 'apt <update>',
            f'apt <install -y --no-install-recommends --no-remove {self.deb}>',
        ])

    def test_refuses_outside_staging(self):
        result = self.run_install(self.root / 'external.deb')
        self.assertEqual(result.returncode, 2)
        self.assertFalse(self.log.exists())

    def test_refuses_nested_path(self):
        result = self.run_install(self.stage / 'inner' / 'example.deb')
        self.assertEqual(result.returncode, 2)

    def test_refuses_other_architecture(self):
        self.tool('dpkg-deb', '#!/bin/sh\necho arm64\n')
        result = self.run_install(self.deb)
        self.assertEqual(result.returncode, 1)
        self.assertFalse(self.log.exists())

    def test_exact_opencode_dispatches_to_native_adapter(self):
        runtime = self.root / 'runtime'
        runtime.mkdir()
        self.env['DEB_LAUNCHER_RUNTIME_DIR'] = str(runtime)
        self.tool('dpkg-deb', '''#!/bin/sh
case "$3" in Architecture) echo amd64;; Package) echo opencode;; Version) echo 2.2.6;; *) exit 2;; esac
''')
        self.tool('sha256sum', '''#!/bin/sh
printf '%s  %s\\n' 87f66555f3a3fec3901ef7e1ac86f5e2deb55b90004cf42396e7547c6b195459 "$1"
''')
        (runtime / 'install-opencode-arm64').write_text('#!/bin/sh\nprintf "adapted:%s\\n" "$1"\n')
        result = self.run_install(self.deb)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, f'adapted:{self.deb}\n')
        self.assertFalse(self.log.exists())


if __name__ == '__main__':
    unittest.main()
