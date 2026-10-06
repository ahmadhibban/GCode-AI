package com.gcode.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends Activity {
    private WebView webView;
    private static final int PERMISSION_REQ_CODE = 1001;
    private static final int FILE_CHOOSER_REQ_CODE = 1002;
    private ValueCallback<Uri[]> uploadMessage;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Throwable ignored) {}

        // Dark status bar and navigation bar matching dark slate theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(0xFF090B10);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setNavigationBarColor(0xFF090B10);
            }
        }

        checkAndRequestAllPermissions();

        File usrBash = new File(getFilesDir(), "usr/bin/bash");
        File marker = new File(getFilesDir(), ".installed_v3");

        if (marker.exists() && usrBash.exists()) {
            initWebView();
        } else {
            showSetupAndExtract();
        }
    }

    private void showSetupAndExtract() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFF0B0E14);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Hibban's GCode AI");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Setting up Standalone Linux Environment...\nExtracting Python 3.14, Pip, Bash, Git & Curl");
        subtitle.setTextColor(0xFF94A3B8);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, pad / 2, 0, pad);

        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);

        TextView status = new TextView(this);
        status.setText("One-time first launch setup. Please wait a moment...");
        status.setTextColor(0xFF6366F1);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, pad, 0, 0);

        layout.addView(title);
        layout.addView(subtitle);
        layout.addView(pb);
        layout.addView(status);

        setContentView(layout);

        new Thread(new Runnable() {
            @Override
            public void run() {
                extractEnvironment();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        initWebView();
                    }
                });
            }
        }).start();
    }

    private boolean extractEnvironment() {
        try {
            File filesDir = getFilesDir();
            File usrDir = new File(filesDir, "usr");
            File homeDir = new File(filesDir, "home");
            File tmpDir = new File(usrDir, "tmp");
            usrDir.mkdirs();
            homeDir.mkdirs();
            tmpDir.mkdirs();

            Process process = new ProcessBuilder("/system/bin/toybox", "tar", "-xzf", "-", "-C", usrDir.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();

            InputStream is;
            try {
                is = getAssets().open("system_bootstrap.tar.gz");
            } catch (Throwable notMonolithic) {
                // Read from split parts seamlessly
                List<InputStream> parts = new ArrayList<>();
                for (char c1 = 'a'; c1 <= 'z'; c1++) {
                    for (char c2 = 'a'; c2 <= 'z'; c2++) {
                        String partName = "system_bootstrap.tar.gz.part_" + c1 + c2;
                        try {
                            InputStream partStream = getAssets().open(partName);
                            parts.add(partStream);
                        } catch (Throwable noMoreParts) {
                            break;
                        }
                    }
                    if (parts.isEmpty()) break;
                }
                Enumeration<InputStream> en = Collections.enumeration(parts);
                is = new SequenceInputStream(en);
            }

            OutputStream os = process.getOutputStream();
            byte[] buf = new byte[65536];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();
            os.close();
            is.close();
            process.waitFor();

            // Set executable permissions
            File binDir = new File(usrDir, "bin");
            File[] bins = binDir.listFiles();
            if (bins != null) {
                for (File f : bins) {
                    try {
                        f.setExecutable(true, false);
                        f.setReadable(true, false);
                    } catch (Throwable ignored) {}
                }
            }

            try {
                new ProcessBuilder("/system/bin/toybox", "chmod", "-R", "755", binDir.getAbsolutePath())
                        .start().waitFor();
            } catch (Throwable ignored) {}

            File bashCheck = new File(usrDir, "bin/bash");
            if (bashCheck.exists() && bashCheck.length() > 0) {
                new File(filesDir, ".installed_v3").createNewFile();
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initWebView() {
        webView = new WebView(this);
        setContentView(webView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setDatabaseEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);

        try {
            ws.setAllowFileAccessFromFileURLs(true);
            ws.setAllowUniversalAccessFromFileURLs(true);
        } catch (Throwable ignored) {}

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                return super.onConsoleMessage(cm);
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                try {
                    request.grant(request.getResources());
                } catch (Throwable ignored) {}
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), FILE_CHOOSER_REQ_CODE);
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void checkAndRequestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            boolean needReq = false;
            for (String p : perms) {
                if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                    needReq = true;
                    break;
                }
            }
            if (needReq) {
                requestPermissions(perms, PERMISSION_REQ_CODE);
            }
        }

        // Android 11+ (API 30+) Manage External Storage (All Files Access) via reflection
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                Method isManagerMethod = Environment.class.getMethod("isExternalStorageManager");
                boolean isManager = (Boolean) isManagerMethod.invoke(null);
                if (!isManager) {
                    Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            } catch (Throwable ignored) {}
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQ_CODE) {
            if (uploadMessage != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK && data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    } else if (data.getClipData() != null) {
                        int num = data.getClipData().getItemCount();
                        results = new Uri[num];
                        for (int i = 0; i < num; i++) {
                            results[i] = data.getClipData().getItemAt(i).getUri();
                        }
                    }
                }
                uploadMessage.onReceiveValue(results);
                uploadMessage = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
