import importlib.util
from pathlib import Path
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location("inventory", Path(__file__).parents[1] / "check-mtm-build.py")
inventory = importlib.util.module_from_spec(spec)
spec.loader.exec_module(inventory)


class BuildInventoryTest(unittest.TestCase):
    def test_folded_manifest(self):
        headers = inventory.manifest_headers("Require-Bundle: first,\r\n second\r\nBundle-Version: 12.0.4\r\n")
        self.assertEqual(headers["Require-Bundle"], "first,second")
        self.assertEqual(headers["Bundle-Version"], "12.0.4")

    def test_missing_old_duplicate_and_complete_targets(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / "META-INF").mkdir()
            (root / "META-INF/MANIFEST.MF").write_text(
                "Bundle-SymbolicName: test;singleton:=true\nBundle-Version: 12.0.4.qualifier\n"
                "Bundle-RequiredExecutionEnvironment: JavaSE-17\nRequire-Bundle: org.adempiere.base\n")
            (root / ".classpath").write_text('<classpath><classpathentry kind="lib" path="local.jar"/></classpath>')
            plugins = root / "plugins"
            plugins.mkdir()
            result = inventory.inspect(root)
            self.assertFalse(result["inventory_ready"])
            self.assertEqual(result["missing_local_libraries"], ["local.jar"])
            (root / "local.jar").touch()

            def jar(name, version):
                with zipfile.ZipFile(plugins / name, "w") as archive:
                    archive.writestr("META-INF/MANIFEST.MF", "Bundle-SymbolicName: org.adempiere.base;singleton:=true\nBundle-Version: " + version + "\n")

            jar("old.jar", "7.1.0")
            self.assertEqual(inventory.inspect(root, plugins)["wrong_platform_major"], ["org.adempiere.base"])
            jar("current.jar", "12.0.0.202501260707")
            self.assertEqual(inventory.inspect(root, plugins)["ambiguous_required_bundles"], ["org.adempiere.base"])
            (plugins / "old.jar").unlink()
            result = inventory.inspect(root, plugins)
            self.assertTrue(result["inventory_ready"])
            self.assertFalse(result["full_compile_verified"])

    def test_malformed_archive_blocks_readiness(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / "META-INF").mkdir()
            (root / "META-INF/MANIFEST.MF").write_text(
                "Bundle-SymbolicName: test\nBundle-Version: 12.0.4\n"
                "Bundle-RequiredExecutionEnvironment: JavaSE-17\nRequire-Bundle: org.adempiere.base\n")
            (root / ".classpath").write_text('<classpath/>')
            plugins = root / "plugins"
            plugins.mkdir()
            (plugins / "broken.jar").write_text("not a jar")
            result = inventory.inspect(root, plugins)
            self.assertEqual(result["unreadable_jars"], ["broken.jar"])
            self.assertFalse(result["inventory_ready"])
