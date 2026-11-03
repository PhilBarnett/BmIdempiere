#!/usr/bin/env python3
"""Read-only MTM build inventory. Does not compile, deploy or read credentials."""
import argparse
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile


def manifest_headers(text):
    unfolded = re.sub(r"\r?\n ", "", text)
    return dict(line.split(": ", 1) for line in unfolded.splitlines() if ": " in line)


def inspect(project, plugins=None):
    headers = manifest_headers((project / "META-INF/MANIFEST.MF").read_text())
    required = [entry.strip().split(";", 1)[0]
                for entry in headers["Require-Bundle"].split(",")]
    entries = ET.parse(project / ".classpath").findall("classpathentry")
    missing = [entry.attrib["path"] for entry in entries
               if entry.attrib["kind"] == "lib"
               and not (project / entry.attrib["path"]).exists()]
    available = {}
    unreadable = []
    if plugins:
        for jar in sorted(plugins.glob("*.jar")):
            try:
                with zipfile.ZipFile(jar) as archive:
                    h = manifest_headers(archive.read("META-INF/MANIFEST.MF").decode("utf-8"))
                name = h.get("Bundle-SymbolicName", "").split(";", 1)[0]
                if name in required:
                    available.setdefault(name, []).append({
                        "file": jar.name, "version": h.get("Bundle-Version", "unknown")})
            except (OSError, zipfile.BadZipFile, KeyError, UnicodeError):
                unreadable.append(jar.name)
    missing_bundles = [name for name in required if name not in available]
    ambiguous = [name for name, jars in available.items() if len(jars) != 1]
    wrong_platform = [name for name, jars in available.items()
                      if name.startswith("org.adempiere.")
                      and any(not jar["version"].startswith("12.") for jar in jars)]
    return {
        "bundle": headers["Bundle-SymbolicName"].split(";", 1)[0],
        "source_version": headers["Bundle-Version"],
        "java_requirement": headers["Bundle-RequiredExecutionEnvironment"],
        "production_source_count": len(list((project / "src/main/java").rglob("*.java"))),
        "missing_local_libraries": missing,
        "platform_supplied": plugins is not None,
        "required_bundles": required,
        "available_required_bundles": available,
        "missing_required_bundles": missing_bundles,
        "ambiguous_required_bundles": ambiguous,
        "wrong_platform_major": wrong_platform,
        "unreadable_jars": unreadable,
        "inventory_ready": bool(plugins) and not any(
            [missing, missing_bundles, ambiguous, wrong_platform, unreadable]),
        "full_compile_verified": False,
        "note": "Inventory only. Transitive OSGi resolution, compilation and ERP integration remain separate checks.",
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plugins", type=Path, help="Directory containing iDempiere 12 target plugin JARs")
    args = parser.parse_args()
    if args.plugins and not args.plugins.is_dir():
        parser.error("--plugins must be an existing directory")
    project = Path(__file__).resolve().parents[1] / "au.blindmot.mtomman"
    result = inspect(project, args.plugins)
    print(json.dumps(result, indent=2))
    raise SystemExit(0 if result["inventory_ready"] else 2)
