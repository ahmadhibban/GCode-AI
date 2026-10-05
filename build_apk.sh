#!/data/data/com.termux/files/usr/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "==> 1. Checking android.jar..."
ANDROID_JAR="/data/data/com.termux/files/home/android-sdk-jar/android.jar"
if [ ! -f "$ANDROID_JAR" ]; then
    ANDROID_JAR="/data/data/com.termux/files/usr/share/java/android.jar"
fi
echo "Using android.jar: $ANDROID_JAR"

rm -rf bin gen
mkdir -p bin gen

echo "==> 2. Generating R.java..."
aapt package -f -m -J gen/ -M AndroidManifest.xml -S res/ -I "$ANDROID_JAR"

echo "==> 3. Compiling Java classes..."
javac -d bin/ -cp "$ANDROID_JAR" gen/com/gcode/ai/R.java src/com/gcode/ai/*.java

echo "==> 4. Converting to Dalvik Executable (d8)..."
d8 --lib "$ANDROID_JAR" --output bin/ $(find bin/ -name "*.class")

echo "==> 5. Packaging APK with resources and assets..."
aapt package -f -M AndroidManifest.xml -S res/ -A assets/ -I "$ANDROID_JAR" -F bin/app.unsigned.apk
cd bin
aapt add app.unsigned.apk classes.dex
cd ..

echo "==> 6. Signing APK with keystore..."
OUT_APK="/storage/emulated/0/Download/GCode_AI.apk"
apksigner sign --ks debug.keystore --ks-pass pass:android --out "$OUT_APK" bin/app.unsigned.apk

echo "==> 7. Verifying APK..."
apksigner verify -v "$OUT_APK"

echo "=========================================================="
echo " SUCCESS! GCode AI APK created at: $OUT_APK"
echo "=========================================================="
