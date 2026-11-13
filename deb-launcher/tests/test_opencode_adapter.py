import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('adapter', ROOT / 'guest/adapt-opencode-asar.py')
adapter = importlib.util.module_from_spec(spec)
spec.loader.exec_module(adapter)


class AdapterTest(unittest.TestCase):
    def test_rejects_unsafe_asar_paths(self):
        for value in ('..', '.', '', 'a/b', 'a\\b'):
            with self.subTest(value=value), self.assertRaises(ValueError):
                adapter.checked_parts(('safe', value))

    def test_rejects_asar_links(self):
        with self.assertRaises(ValueError):
            list(adapter.members({'files': {'bad': {'link': '/outside'}}}))

    def test_special_installer_refuses_an_unknown_deb(self):
        with tempfile.TemporaryDirectory() as temp:
            deb = Path(temp) / 'unknown.deb'
            deb.write_bytes(b'not the supported package')
            result = subprocess.run(['/bin/sh', str(ROOT / 'guest/install-opencode-arm64'), str(deb)],
                                    text=True, capture_output=True)
            self.assertEqual(result.returncode, 1)
            self.assertIn('checksum', result.stderr)


if __name__ == '__main__':
    unittest.main()
