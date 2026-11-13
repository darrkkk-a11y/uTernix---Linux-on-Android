import os
from pathlib import Path
import subprocess
import tempfile
import unittest

START = Path(__file__).resolve().parents[1] / 'guest/opencode-start'


class StartTest(unittest.TestCase):
    def test_first_start_isolates_config_and_second_start_preserves_it(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            home = root / 'home'
            home.mkdir()
            app = root / 'app'
            (app / 'resources').mkdir(parents=True)
            cli = app / 'resources/opencode-cli'
            cli.write_text('''#!/bin/sh
mkdir -p "$XDG_CONFIG_HOME/opencode"
printf '{"test":true}\\n' > "$XDG_CONFIG_HOME/opencode/service.json"
printf 'set:%s\\n' "$*" >> "$HOME/calls"
''')
            cli.chmod(0o755)
            exe = app / 'ai.opencode'
            exe.write_text('#!/bin/sh\nprintf "%s\\n" "$XDG_CONFIG_HOME"\nprintf "%s\\n" "$@"\n')
            exe.chmod(0o755)
            env = os.environ.copy()
            env.update(HOME=str(home), XDG_CONFIG_HOME=str(root / 'other-config'),
                       DEB_LAUNCHER_OPENCODE_DIR=str(app))
            for _ in range(2):
                result = subprocess.run(['/bin/sh', str(START), 'a b'], env=env,
                                        text=True, capture_output=True)
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertEqual(result.stdout.splitlines(), [str(home / '.config/deb-launcher-opencode'),
                     '--start-maximized', '--enable-features=AutoDarkMode',
                     '--force-dark-mode', 'a b'])
            self.assertEqual(len((home / 'calls').read_text().splitlines()), 1)
            self.assertFalse((root / 'other-config').exists())


if __name__ == '__main__':
    unittest.main()
