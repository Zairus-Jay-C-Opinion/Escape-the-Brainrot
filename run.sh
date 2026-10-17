#!/bin/bash
# Compiles src/ into bin/, copies res/ into bin/, and runs Main (Mac/Linux).
set -e

# Work from the script's own folder so it runs from anywhere, even with spaces in the path.
cd "$(dirname "$0")"

mkdir -p bin

# Collect every .java file into a list so javac gets them all in one go.
SOURCES_FILE="$(mktemp)"
find src -name '*.java' > "$SOURCES_FILE"
javac -d bin @"$SOURCES_FILE"
rm -f "$SOURCES_FILE"

# Resources end up on the classpath root (bin/), which is where getResourceAsStream looks.
cp -R res/. bin/

java -cp bin Main
