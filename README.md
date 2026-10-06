# Hibban's GCode AI 🤖⚡
### Autonomous AI Coding Assistant & Developer with 100% Standalone Embedded Linux Distribution

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-API%2028%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Python](https://img.shields.io/badge/Python-3.14.6-3776AB?style=flat-square&logo=python&logoColor=white)](https://python.org)
[![Virtualization](https://img.shields.io/badge/Engine-PRoot%20User--Space-orange?style=flat-square)](https://proot-me.github.io)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64%20(aarch64)-critical?style=flat-square)](https://github.com/ahmadhibban/GCode-AI)
[![Author](https://img.shields.io/badge/Author-Ahmad%20Hibban-4f46e5?style=flat-square)](https://github.com/ahmadhibban)

**Hibban's GCode AI** is an autonomous AI software developer and pair-programming assistant running natively on Android devices. It embeds a complete, real Linux distribution directly inside the application, enabling true autonomous terminal execution, script compilation, filesystem manipulation, and multimodal vision without requiring Termux, root permissions, or any external cross-app dependencies.

Developed by **[Ahmad Hibban (আহমাদ হিব্বান)](https://github.com/ahmadhibban)**.

---

## 🏷️ Tags & Topics

`android` • `ai-assistant` • `autonomous-agent` • `python3` • `gemini-api` • `proot` • `standalone-linux` • `multimodal-vision` • `bengali-typography` • `arabic-rtl` • `termux-alternative` • `coding-agent`

---

## 🌟 Key Highlights

- **100% Standalone Embedded Linux System**:
  Bundles an authentic, isolated ARM64 Linux rootfs inside the application assets. On first launch, the environment automatically extracts to the app's private sandbox (`/data/data/com.gcode.ai/files/usr/`) with a native progress setup screen.
- **Zero Termux Dependencies**:
  Runs out-of-the-box on ANY user's Android phone without requiring Termux or any cross-app permissions.
- **PRoot User-Space Virtualization Engine**:
  Executes shell commands and binaries via PRoot in user-space using `ptrace`/`seccomp`. Bypasses Android SELinux denials without requiring root access.
- **Minimal Core Bootstrap + On-Demand Package Installation**:
  Bundles only the essential bootstrap environment required for initial operation (GNU Bash, Python 3.14, Pip, GNU Coreutils, Curl, Git, and Android SDK tools). Any additional packages (such as `python-docx`, `requests`, `numpy`, etc.) are installed autonomously by the AI on demand whenever a specific task requires them.
- **Autonomous Multimodal Vision**:
  Inspects and analyzes any photo, camera capture, or screenshot on the phone using Gemini's multimodal vision model via the `view_image` tool.
- **Unrestricted Agent Loop**:
  Modeled after Antigravity CLI. Runs continuously in an unrestricted loop until tasks are 100% finished, with intelligent Gemini API key quota rotation.
- **Bilingual Typography & Native Arabic RTL**:
  - **Bengali**: High-legibility *Kalpurush* font applied seamlessly to both chat rendering and input textarea.
  - **Arabic**: Classical *Amiri* calligraphy font with automatic Right-to-Left (RTL) alignment for Arabic answers and dynamic input direction switching.
- **Persistent Chat History**:
  Conversation state is securely saved and restored across app restarts and reboots.

---

## 📦 What's Inside the Bundled Environment?

To prevent unnecessary APK bloat while ensuring complete autonomy, the bundled environment strictly includes the base bootstrap packages:

| Component | Bundled Version / Tools | Purpose |
| :--- | :--- | :--- |
| **Shell** | GNU Bash 5.3 & Dash | Scripting and command orchestration |
| **Runtime Core** | Python 3.14.6 + Pip 26.2.1 | AI script execution, data manipulation, automation |
| **Virtualization** | PRoot (ARM64 user-space) | Filesystem translation and sandboxed execution |
| **Core Utilities** | GNU Coreutils (`ls`, `cat`, `grep`, `sed`, `awk`, `find`, `tar`, `gzip`, `cut`, `sort`, `uniq`, etc.) | Essential file and system operations |
| **Networking** | Curl & Git with CA TLS Certificates | Secure HTTPS requests and repository cloning |
| **Android Tools** | `aapt`, `apksigner`, `d8` | On-device Android APK compilation and signing |

> 💡 **On-Demand Philosophy**: Heavy third-party packages (such as compilers, multimedia engines, or large Python packages) are intentionally NOT bundled to keep the initial download compact. The AI downloads and installs whatever it needs on the fly using `pip install <package>` or `apt install <package>`.

---

## 🛠️ Built-in Agent Tools

1. `run_command` — Execute bash/terminal commands in the built-in Linux environment, run Python scripts, install packages, and manage files.
2. `read_file` — Read any text file on device storage with 1-indexed line numbers and smart path resolution.
3. `write_file` — Create or overwrite files anywhere on device storage (`/storage/emulated/0`, Download, DCIM).
4. `edit_file` — Precise text substring replacement with collision prevention.
5. `list_directory` — Explore directories and subdirectories.
6. `view_image` — Multimodal vision inspection of photos and screenshots on device storage.

---

## 📱 Prebuilt APK

The signed, ready-to-install Android APK (v3.0.0 Standalone Edition) is located on device at:
`/storage/emulated/0/Download/GCode_AI.apk`

---

## 📂 Repository Structure

```
GCode-AI/
├── AndroidManifest.xml   # Standalone Android manifest (API 28, full storage access)
├── assets/
│   ├── index.html        # Modern Dark Chat UI, Agent engine, RTL & custom fonts
│   ├── icon.png          # App 3D branding icon
│   ├── fonts/            # Kalpurush (Bengali) & Amiri (Arabic) fonts
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

Prerequisites:
- Android SDK build tools (`aapt`, `d8`, `apksigner`)
- Java compiler (`javac`)
- `android.jar` (API 28+)

To compile, package, and sign the standalone APK:
```bash
bash build_apk.sh
```
The output APK will be placed in `/storage/emulated/0/Download/GCode_AI.apk` and `apk/GCode_AI.apk`.

---

## 👤 Author

**Ahmad Hibban (আহমাদ হিব্বান)**
- GitHub: [@ahmadhibban](https://github.com/ahmadhibban)
- Portfolio: [ahmadhibban](https://github.com/ahmadhibban/ahmadhibban)

---

## 📄 License

Open-source under the MIT License. Copyright © Ahmad Hibban.
