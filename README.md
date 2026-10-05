# Hibban's GCode AI 🚀

An autonomous AI developer and coding assistant running natively on Android with **full Termux Linux environment access** and **Multimodal Vision**.

Developed by **Ahmad Hibban**.

---

## ✨ Features

- **Native Linux Execution:** Connects directly to Termux's `/data/data/com.termux/files/usr/bin/bash` with full environment variables (`PATH`, `PREFIX`, `HOME`, `PYTHONPATH`, `SSL_CERT_FILE`, `JAVA_HOME`).
- **Autonomous Multimodal Vision:** Inspects and analyzes any photo, camera capture, or screenshot on the phone using Gemini's multimodal vision model via the `view_image` tool.
- **Unrestricted Filesystem Access:** Full read/write access across all phone storage (`/storage/emulated/0`, DCIM, Download) and Termux home (`~`).
- **Zero Buffer Deadlocks & Stdin Protection:** Merged stdout/stderr streams, non-interactive execution, and 120s timeout protection.
- **Multi-Key Quota Rotation:** Supports multiple Google Gemini API keys with intelligent automatic rotation upon rate limits (429).
- **Persistent Chat:** Conversation history is securely preserved across app restarts and device reboots.
- **Sleek Custom Modal:** Elegant dark-glass confirmation dialog for clearing history with zero clunky OS prompts.
- **Bilingual & Beautiful Typography:** Custom high-legibility Bengali font (*Kalpurush*) and Arabic font (*Amiri*) with a sleek dark slate theme.

---

## 🛠️ Built-in Agent Tools

1. `run_command` — Execute bash/terminal commands, run Python 3.14 scripts, git, ffmpeg, curl, etc.
2. `read_file` — Read any text file with 1-indexed line numbers and smart path resolution.
3. `write_file` — Create or overwrite files anywhere on the device.
4. `edit_file` — Precise text substring replacement with collision prevention.
5. `list_directory` — Explore directories and subdirectories.
6. `view_image` — Visually inspect and analyze images on device storage.

---

## 📱 Prebuilt APK

The signed, ready-to-install Android APK is available in the repository at:
[`apk/GCode_AI.apk`](apk/GCode_AI.apk)

---

## 📁 Repository Structure

```
├── AndroidManifest.xml   # Full permissions & hardware access
├── apk/
│   ├── GCode_AI.apk      # Signed Release APK
│   └── GCode_AI.apk.idsig # APK signature v4 scheme ID
├── assets/
│   ├── index.html        # Modern Dark Chat UI & Agent engine
│   ├── icon.png          # App 3D branding icon
│   └── fonts/            # Kalpurush & Amiri fonts
├── src/com/gcode/ai/
│   ├── MainActivity.java # WebView host with runtime permission management
│   └── WebAppInterface.java # Native Java bridge to Termux shell & vision
├── res/                  # App drawables, launcher icons, strings
├── build_apk.sh          # Native Termux build script (aapt + javac + d8 + apksigner)
├── debug.keystore        # Keystore for release APK signing
├── gcode_cli.py          # Standalone Termux CLI version
└── README.md             # Project documentation
```

---

## 🛠️ Build from Source (Termux / Linux)

Prerequisites:
- Android SDK build tools (`aapt`, `d8`, `apksigner`)
- `android.jar` (API 28+)
- Java compiler (`javac`)

To build and sign the APK:
```bash
bash build_apk.sh
```
The output APK will be placed in `apk/GCode_AI.apk`.

---

## 👤 Author

**Ahmad Hibban**
- GitHub: [@ahmadhibban](https://github.com/ahmadhibban)

---

## 📄 License

Open-source under the MIT License. Copyright © Ahmad Hibban.
