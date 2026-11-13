#!/bin/sh
# Build the modified Tessera-based launcher with the project's local ARM64
# host tooling. Does not install the APK or run it on a device.
set -eu
TOOLS=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WORK=$(CDPATH= cd -- "$TOOLS/.." && pwd)
python3 "$WORK/deb-launcher/stage-runtime.py" --refresh
export ANDROID_HOME="$TOOLS/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
cd "$WORK/tessera-linux"
exec ./gradlew :app:assembleDebug :app:testDebugUnitTest \
  --no-daemon --no-configuration-cache --max-workers=1 \
  '-Dorg.gradle.jvmargs=-Xmx1200m -Dfile.encoding=UTF-8' \
  -Pkotlin.compiler.execution.strategy=in-process \
  -Pandroid.builder.sdkDownload=false \
  "-Pandroid.aapt2FromMavenOverride=$TOOLS/sdk-wrapper/aapt2"
