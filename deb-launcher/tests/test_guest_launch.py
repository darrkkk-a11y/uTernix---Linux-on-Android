import os
from pathlib import Path
import subprocess
import tempfile
import unittest

LAUNCH = Path(__file__).resolve().parents[1] / 'guest/launch'


class LaunchTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.env = os.environ.copy()
        self.env['PATH'] = f'{self.root}:/usr/bin:/bin'
        self.env['DEB_LAUNCHER_BOX64'] = str(self.root / 'box64')
        self.write_executable('uname', '#!/bin/sh\nprintf "aarch64\\n"\n')

    def tearDown(self):
        self.temp.cleanup()

    def write_executable(self, name, content):
        path = self.root / name
        path.write_text(content)
        path.chmod(0o755)
        return path

    def run_launch(self, *args):
        return subprocess.run(['/bin/sh', str(LAUNCH), *map(str, args)],
                              capture_output=True, text=True, env=self.env)

    def x86_elf(self, name='program'):
        data = bytearray(64)
        data[:6] = b'\x7fELF\x02\x01'
        data[18:20] = b'\x3e\x00'
        path = self.root / name
        path.write_bytes(data)
        path.chmod(0o755)
        return path

    def test_x86_elf_uses_box64_and_preserves_args(self):
        self.write_executable('box64', '#!/bin/sh\nprintf "<%s>\\n" "$@"\n')
        program = self.x86_elf('space name')
        result = self.run_launch(program, 'a b', ';literal')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.splitlines(), [f'<{program}>', '<a b>', '<;literal>'])

    def test_x86_without_box64_is_an_error(self):
        result = self.run_launch(self.x86_elf())
        self.assertEqual(result.returncode, 126)
        self.assertIn('needs Box64', result.stderr)

    def test_native_script_runs_directly(self):
        target = self.write_executable('native', '#!/bin/sh\nprintf "native:%s\\n" "$1"\n')
        result = self.run_launch(target, 'a b')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, 'native:a b\n')

    def test_path_lookup_preserves_original_name(self):
        self.write_executable('box64', '#!/bin/sh\nprintf "<%s>\\n" "$@"\n')
        self.x86_elf('onpath')
        result = self.run_launch('onpath', 'arg')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.splitlines(), ['<onpath>', '<arg>'])

    def test_unrelated_od_in_path_does_not_break_detection(self):
        self.write_executable('od', '#!/bin/sh\nexit 99\n')
        self.write_executable('box64', '#!/bin/sh\nprintf "translated\\n"\n')
        result = self.run_launch(self.x86_elf())
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, 'translated\n')

    def test_no_program(self):
        self.assertEqual(self.run_launch().returncode, 2)


if __name__ == '__main__':
    unittest.main()
