#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
testdir=$(mktemp -d)
trap 'rm -rf "$testdir"' EXIT HUP INT TERM
java com.sun.tools.javac.Main --release 17 -d "$testdir" \
  au.blindmot.mtomman/src/main/java/au/blindmot/utils/MtmBarcode.java \
  scripts/tests/MtmBarcodeTest.java
java -cp "$testdir" MtmBarcodeTest
