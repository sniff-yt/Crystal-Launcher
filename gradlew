#!/usr/bin/env sh
# Lightweight Gradle bootstrap for Crystal Launcher.
# Downloads the pinned Gradle distribution once, then reuses the cached copy.
set -eu
GRADLE_VERSION="8.13"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/crystal-launcher"
DIST="$BASE/gradle-$GRADLE_VERSION"
if [ ! -x "$DIST/bin/gradle" ]; then
  mkdir -p "$BASE"
  ZIP="$BASE/gradle-$GRADLE_VERSION-bin.zip"
  echo "Downloading Gradle $GRADLE_VERSION (first run only)..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 --connect-timeout 20 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "Install curl or wget, then try again." >&2
    exit 1
  fi
  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$ZIP" -d "$BASE"
  else
    python - "$ZIP" "$BASE" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1]) as z:
    z.extractall(sys.argv[2])
PY
  fi
  rm -f "$ZIP"
fi
exec "$DIST/bin/gradle" "$@"
