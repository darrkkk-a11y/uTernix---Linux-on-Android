#!/bin/sh
set -eu
cd "$(dirname "$0")"
SDK=${ANDROID_HOME:-/usr/lib/android-sdk}
BT="$SDK/build-tools/35.0.1"
ANDROID="$SDK/platforms/android-34/android.jar"
AAPT2=${AAPT2:-$(command -v aapt2)}
mkdir -p build/classes build/dex
"$AAPT2" link --manifest AndroidManifest.xml -I "$ANDROID" -o build/unsigned.apk
javac -source 8 -target 8 -bootclasspath "$ANDROID" -d build/classes src/lab/deblauncher/MainActivity.java
"$BT/d8" --min-api 23 --lib "$ANDROID" --output build/dex build/classes/lab/deblauncher/*.class
python3 -c 'import zipfile; z=zipfile.ZipFile("build/unsigned.apk", "a", zipfile.ZIP_DEFLATED); z.write("build/dex/classes.dex", "classes.dex"); z.close()'
if [ ! -f build/debug.keystore ]; then
  keytool -genkeypair -keystore build/debug.keystore -storepass android -keypass android -alias deb-launcher -dname 'CN=DEB Launcher Debug' -keyalg RSA -keysize 2048 -validity 10000
fi
"$BT/apksigner" sign --ks build/debug.keystore --ks-pass pass:android --key-pass pass:android --out build/deb-launcher-debug.apk build/unsigned.apk
"$BT/apksigner" verify build/deb-launcher-debug.apk
printf 'APK: %s\n' "$PWD/build/deb-launcher-debug.apk"
