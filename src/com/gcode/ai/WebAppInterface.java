package com.gcode.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebAppInterface {
    private final Context context;
    private final SharedPreferences prefs;

    public WebAppInterface(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("gcode_prefs", Context.MODE_PRIVATE);
    }

    private File resolveFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return Environment.getExternalStorageDirectory();
        }
        String cleanPath = filePath.trim();
        if (cleanPath.startsWith("file://")) {
            cleanPath = cleanPath.substring(7);
        }
        File f = new File(cleanPath);
        if (f.isAbsolute()) {
            return f;
        }
        // Try /storage/emulated/0 first
        File f1 = new File(Environment.getExternalStorageDirectory(), cleanPath);
        if (f1.exists()) {
            return f1;
        }
        // Try Termux home
        File f2 = new File("/data/data/com.termux/files/home", cleanPath);
        if (f2.exists()) {
            return f2;
        }
        // Default to /storage/emulated/0
        return f1;
    }

    @JavascriptInterface
    public String getStorageDirectory() {
        try {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } catch (Exception e) {
            return "/storage/emulated/0";
        }
    }

    @JavascriptInterface
    public String getDownloadDirectory() {
        try {
            File dl = new File(Environment.getExternalStorageDirectory(), "Download");
            return dl.getAbsolutePath();
        } catch (Exception e) {
            return "/storage/emulated/0/Download";
        }
    }

    @JavascriptInterface
    public boolean fileExists(String filePath) {
        if (filePath == null) return false;
        try {
            return resolveFile(filePath).exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads an image file from storage, resizes it safely, and returns Base64 JPEG data.
     * Memory-safe with bounds decoding & inSampleSize.
     */
    @JavascriptInterface
    public String readImageBase64(String filePath, int maxWidth, int maxHeight) {
        JSONObject result = new JSONObject();
        try {
            File file = resolveFile(filePath);
            if (!file.exists() || !file.isFile()) {
                result.put("success", false);
                result.put("error", "Image file does not exist: " + filePath);
                return result.toString();
            }

            int reqWidth = (maxWidth > 0) ? maxWidth : 1280;
            int reqHeight = (maxHeight > 0) ? maxHeight : 1280;

            // Step 1: Decode bounds only
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), boundsOptions);

            int origWidth = boundsOptions.outWidth;
            int origHeight = boundsOptions.outHeight;

            if (origWidth <= 0 || origHeight <= 0) {
                result.put("success", false);
                result.put("error", "Failed to decode image dimensions for: " + file.getAbsolutePath());
                return result.toString();
            }

            // Step 2: Compute inSampleSize
            int inSampleSize = 1;
            while ((origWidth / (inSampleSize * 2)) >= reqWidth && (origHeight / (inSampleSize * 2)) >= reqHeight) {
                inSampleSize *= 2;
            }

            // Step 3: Decode bitmap with sample size
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = inSampleSize;
            decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap sampledBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), decodeOptions);

            if (sampledBitmap == null) {
                result.put("success", false);
                result.put("error", "Failed to decode bitmap from: " + file.getAbsolutePath());
                return result.toString();
            }

            // Step 4: Scale down if needed
            Bitmap finalBitmap = sampledBitmap;
            int currentW = sampledBitmap.getWidth();
            int currentH = sampledBitmap.getHeight();

            if (currentW > reqWidth || currentH > reqHeight) {
                float ratio = Math.min((float) reqWidth / currentW, (float) reqHeight / currentH);
                int targetW = Math.max(1, Math.round(currentW * ratio));
                int targetH = Math.max(1, Math.round(currentH * ratio));
                finalBitmap = Bitmap.createScaledBitmap(sampledBitmap, targetW, targetH, true);
                if (finalBitmap != sampledBitmap) {
                    sampledBitmap.recycle();
                }
            }

            // Step 5: Compress to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            finalBitmap.recycle();

            String base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            result.put("success", true);
            result.put("mimeType", "image/jpeg");
            result.put("base64", base64Str);
            result.put("width", finalBitmap.getWidth());
            result.put("height", finalBitmap.getHeight());
            result.put("sizeBytes", imageBytes.length);
        } catch (Throwable e) {
            try {
                result.put("success", false);
                result.put("error", "Error reading image: " + e.getMessage());
            } catch (Exception ignored) {}
        }
        return result.toString();
    }

    /**
     * Reads any binary or text file as base64 (up to 10MB).
     */
    @JavascriptInterface
    public String readFileBase64(String filePath) {
        JSONObject result = new JSONObject();
        try {
            File file = resolveFile(filePath);
            if (!file.exists()) {
                result.put("success", false);
                result.put("error", "File does not exist: " + filePath);
                return result.toString();
            }
            if (file.length() > 10 * 1024 * 1024) {
                result.put("success", false);
                result.put("error", "File exceeds 10MB limit");
                return result.toString();
            }

            byte[] buffer = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            int read = 0;
            int offset = 0;
            while (offset < buffer.length && (read = fis.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += read;
            }
            fis.close();

            String b64 = Base64.encodeToString(buffer, Base64.NO_WRAP);
            result.put("success", true);
            result.put("base64", b64);
            result.put("sizeBytes", buffer.length);
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (Exception ignored) {}
        }
        return result.toString();
    }

    /**
     * Executes shell commands with full Termux Linux environment.
     * Deadlock-free stream merging, non-interactive stdin, and timeout protection.
     */
    @JavascriptInterface
    public String executeCommand(String command, String workingDir) {
        JSONObject result = new JSONObject();
        StringBuilder output = new StringBuilder();
        int exitCode = -1;

        try {
            File dir;
            if (workingDir != null && !workingDir.trim().isEmpty()) {
                dir = new File(workingDir);
                if (!dir.exists() || !dir.isDirectory()) {
                    dir = new File("/data/data/com.termux/files/home");
                    if (!dir.exists()) dir = Environment.getExternalStorageDirectory();
                }
            } else {
                dir = new File("/data/data/com.termux/files/home");
                if (!dir.exists()) dir = Environment.getExternalStorageDirectory();
            }

            File termuxBash = new File("/data/data/com.termux/files/usr/bin/bash");
            String[] cmdArray;
            if (termuxBash.exists() && termuxBash.canExecute()) {
                cmdArray = new String[]{"/data/data/com.termux/files/usr/bin/bash", "-c", command};
            } else {
                cmdArray = new String[]{"/system/bin/sh", "-c", command};
            }

            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.directory(dir);

            // Full Termux environment injection
            Map<String, String> env = pb.environment();
            env.put("PATH", "/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:/system/bin:/system/xbin");
            env.put("LD_LIBRARY_PATH", "/data/data/com.termux/files/usr/lib");
            env.put("PREFIX", "/data/data/com.termux/files/usr");
            env.put("HOME", "/data/data/com.termux/files/home");
            env.put("TMPDIR", "/data/data/com.termux/files/usr/tmp");
            env.put("TERM", "xterm-256color");
            env.put("LANG", "en_US.UTF-8");
            env.put("SHELL", "/data/data/com.termux/files/usr/bin/bash");
            env.put("SSL_CERT_FILE", "/data/data/com.termux/files/usr/etc/tls/cert.pem");
            env.put("DEBIAN_FRONTEND", "noninteractive");
            env.put("PYTHONPATH", "/data/data/com.termux/files/usr/lib/python3.14/site-packages");
            env.put("JAVA_HOME", "/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk");

            // Merge stderr into stdout to guarantee zero buffer deadlock
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Close stdin so command never hangs on interactive input
            try {
                process.getOutputStream().close();
            } catch (Exception ignored) {}

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() < 95000) {
                    output.append(line).append("\n");
                }
            }
            reader.close();

            // Safe timeout: 120 seconds max per command
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                output.append("\n[Process Timed Out after 120s]");
                exitCode = -1;
            } else {
                exitCode = process.exitValue();
            }

            result.put("success", true);
            result.put("stdout", output.toString());
            result.put("stderr", "");
            result.put("exitCode", exitCode);
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("stdout", "");
                result.put("stderr", "Execution Error: " + e.getMessage());
                result.put("exitCode", -1);
            } catch (Exception ignored) {}
        }

        return result.toString();
    }

    @JavascriptInterface
    public String readFile(String filePath, int startLine, int endLine) {
        JSONObject result = new JSONObject();
        try {
            File file = resolveFile(filePath);
            if (!file.exists()) {
                result.put("success", false);
                result.put("error", "File does not exist: " + filePath);
                return result.toString();
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            );

            StringBuilder content = new StringBuilder();
            String line;
            int currentLine = 1;
            int actualStart = (startLine > 0) ? startLine : 1;
            int actualEnd = (endLine > 0) ? endLine : Integer.MAX_VALUE;

            while ((line = reader.readLine()) != null) {
                if (currentLine >= actualStart && currentLine <= actualEnd) {
                    content.append(currentLine).append(": ").append(line).append("\n");
                }
                currentLine++;
            }
            reader.close();

            result.put("success", true);
            result.put("content", content.toString());
            result.put("totalLines", currentLine - 1);
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (Exception ignored) {}
        }
        return result.toString();
    }

    @JavascriptInterface
    public String writeFile(String filePath, String content) {
        JSONObject result = new JSONObject();
        try {
            File file = resolveFile(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();

            result.put("success", true);
            result.put("bytesWritten", content.length());
            result.put("path", file.getAbsolutePath());
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (Exception ignored) {}
        }
        return result.toString();
    }

    @JavascriptInterface
    public String editFile(String filePath, String targetText, String replacementText) {
        JSONObject result = new JSONObject();
        try {
            File file = resolveFile(filePath);
            if (!file.exists()) {
                result.put("success", false);
                result.put("error", "File not found: " + filePath);
                return result.toString();
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String full = sb.toString();
            if (!full.contains(targetText)) {
                result.put("success", false);
                result.put("error", "Target text snippet not found in file.");
                return result.toString();
            }

            int index = full.indexOf(targetText);
            int nextIndex = full.indexOf(targetText, index + targetText.length());
            if (nextIndex != -1) {
                result.put("success", false);
                result.put("error", "Target text occurs multiple times. Provide a larger unique snippet.");
                return result.toString();
            }

            String updated = full.replace(targetText, replacementText);
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(updated.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();

            result.put("success", true);
            result.put("message", "Successfully replaced target text.");
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (Exception ignored) {}
        }
        return result.toString();
    }

    @JavascriptInterface
    public String listDirectory(String dirPath) {
        JSONObject result = new JSONObject();
        try {
            File dir = resolveFile(dirPath);
            if (!dir.exists() || !dir.isDirectory()) {
                result.put("success", false);
                result.put("error", "Directory does not exist: " + dirPath);
                return result.toString();
            }

            File[] files = dir.listFiles();
            JSONArray list = new JSONArray();
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        if (f1.isDirectory() && !f2.isDirectory()) return -1;
                        if (!f1.isDirectory() && f2.isDirectory()) return 1;
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                });

                for (File f : files) {
                    JSONObject item = new JSONObject();
                    item.put("name", f.getName());
                    item.put("isDirectory", f.isDirectory());
                    item.put("size", f.isDirectory() ? 0 : f.length());
                    list.put(item);
                }
            }

            result.put("success", true);
            result.put("files", list);
            result.put("path", dir.getAbsolutePath());
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (Exception ignored) {}
        }
        return result.toString();
    }

    @JavascriptInterface
    public void vibrate(int milliseconds) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(milliseconds > 0 ? milliseconds : 50);
            }
        } catch (Exception ignored) {}
    }

    @JavascriptInterface
    public void saveConfig(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    @JavascriptInterface
    public String getConfig(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
}
