# Hibban's GCode AI 🚀

An autonomous AI developer and coding assistant running natively on Android with a **100% Standalone Embedded Linux Distribution** and **Multimodal Vision**.

Developed by **Ahmad Hibban**.

---

## ✨ Features

- **100% Standalone Embedded Linux:** Bundles a complete, self-contained Linux environment (GNU Bash, Python 3.14, Pip, Git, Curl, GNU Coreutils, and PRoot user-space virtualization engine) directly inside the app.
- **Zero Termux Dependencies:** Completely independent. Works out-of-the-box on ANY Android device without requiring Termux or any cross-app permissions.
- **Direct User-Space Execution:** Executes scripts and shell commands through PRoot without root permissions and without SELinux denials.
- **Autonomous Multimodal Vision:** Inspects and analyzes any photo, camera capture, or screenshot on the phone using Gemini's multimodal vision model via the `view_image` tool.
- **Unrestricted Filesystem Access:** Full read/write access across phone storage (`/storage/emulated/0`, DCIM, Download).
- **Unrestricted Agent Loop:** Modeled after Antigravity CLI with autonomous on-demand package installation and continuous execution until tasks are 100% completed.
- **Multi-Key Quota Rotation:** Supports multiple Google Gemini API keys with intelligent automatic rotation upon rate limits (429).
- **Persistent Chat:** Conversation history is securely preserved across app restarts and device reboots.
- **Bilingual & Beautiful Typography:** Custom high-legibility Bengali font (*Kalpurush*) and Arabic font (*Amiri*) with a sleek dark slate theme.

---

## 🛠️ Built-in Agent Tools

1. `run_command` — Execute bash/terminal commands in the built-in Linux environment, run Python 3.14 scripts, pip install, git, curl, etc.
2. `read_file` — Read any text file with 1-indexed line numbers and smart path resolution.
3. `write_file` — Create or overwrite files anywhere on the device.
4. `edit_file` — Precise text substring replacement with collision prevention.
5. `list_directory` — Explore directories and subdirectories.
6. `view_image` — Visually inspect and analyze images on device storage.

---

## 📱 Prebuilt APK

The signed, ready-to-install Android APK (v3.0.0 Standalone Edition) is located on device at:
`/storage/emulated/0/Download/GCode_AI.apk`

---

## 📁 Repository Structure

```
├── AndroidManifest.xml   # Standalone configuration (API 28, full storage access)
├── assets/
│   ├── index.html        # Modern Dark Chat UI & Agent engine
│   ├── icon.png          # App 3D branding icon
│   ├── fonts/            # Kalpurush & Amiri fonts
│   ├── system_bootstrap.tar.gz.part_aa # Embedded Linux rootfs (Part 1)
│   ├── system_bootstrap.tar.gz.part_ab # Embedded Linux rootfs (Part 2)
│   └── system_bootstrap.tar.gz.part_ac # Embedded Linux rootfs (Part 3)
├── src/com/gcode/ai/
│   ├── MainActivity.java # Setup progress extractor & WebView host
│   └── WebAppInterface.java # Standalone PRoot process engine & vision bridge
├── res/                  # App drawables, launcher icons, strings
├── build_apk.sh          # Native Android build script (aapt + javac + d8 + apksigner)
├── debug.keystore        # Keystore for release APK signing
└── README.md             # Project documentation
```

---

## 🛠️ Build from Source (Android / Linux)

To compile, package, and sign the standalone APK:
```bash
bash build_apk.sh
```
The output APK will be placed in `/storage/emulated/0/Download/GCode_AI.apk` and `apk/GCode_AI.apk`.

---

## 👤 Author

**Ahmad Hibban**
- GitHub: [@ahmadhibban](https://github.com/ahmadhibban)

---

## 📄 License

Open-source under the MIT License. Copyright © Ahmad Hibban.
