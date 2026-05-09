package com.superclicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.superclicker.service.ClickService;
import com.superclicker.service.FloatingService;
import com.superclicker.ui.editor.StepEditorFragment;
import com.superclicker.ui.log.LogFragment;
import com.superclicker.ui.script.ScriptListFragment;
import com.superclicker.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQ_NOTIFICATION = 1001;

    private BottomNavigationView bottomNav;
    private TextView tvStatus;
    private final ScriptListFragment fragScripts = new ScriptListFragment();
    private final StepEditorFragment fragEditor = new StepEditorFragment();
    private final LogFragment fragLog = new LogFragment();
    private final SettingsFragment fragSettings = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_main_status);
        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragScripts).commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_scripts) f = fragScripts;
            else if (id == R.id.nav_editor) f = fragEditor;
            else if (id == R.id.nav_log) f = fragLog;
            else if (id == R.id.nav_settings) f = fragSettings;
            else return false;
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
            return true;
        });

        new Handler(Looper.getMainLooper()).postDelayed(this::startPermissionFlow, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        if (isAllReady()) startFloating();
    }

    // ===== 权限引导流程 =====
    private void startPermissionFlow() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                askPermission("通知权限", "用于脚本执行状态提醒",
                    () -> ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATION));
                return;
            }
        }
        checkOverlay();
    }

    private void checkOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            askPermission("悬浮窗权限", "用于显示脚本控制面板",
                () -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()))));
            return;
        }
        checkAccessibility();
    }

    private void checkAccessibility() {
        if (!ClickService.isRunning()) {
            askPermission("无障碍服务", "用于执行自动点击、滑动等操作",
                () -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
            return;
        }
        startFloating();
    }

    private void askPermission(String title, String msg, Runnable action) {
        new AlertDialog.Builder(this)
            .setTitle(title).setMessage(msg)
            .setPositiveButton("去开启", (d, w) -> {
                try { action.run(); } catch (Exception e) {
                    Toast.makeText(this, "请手动到设置中开启", Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("稍后", null).setCancelable(false).show();
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == REQ_NOTIFICATION) {
            new Handler(Looper.getMainLooper()).postDelayed(this::checkOverlay, 300);
        }
    }

    private boolean isAllReady() {
        return Settings.canDrawOverlays(this) && ClickService.isRunning();
    }

    private void startFloating() {
        try {
            if (Settings.canDrawOverlays(this)) {
                startForegroundService(new Intent(this, FloatingService.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "启动悬浮窗失败: " + e.getMessage());
        }
    }

    private void updateStatus() {
        boolean o = Settings.canDrawOverlays(this);
        boolean a = ClickService.isRunning();
        if (o && a) {
            tvStatus.setText("就绪");
            tvStatus.setTextColor(0xFF4CAF50);
        } else {
            StringBuilder sb = new StringBuilder();
            if (!o) sb.append("[悬浮窗]");
            if (!a) sb.append("[无障碍]");
            sb.append("未开启");
            tvStatus.setText(sb.toString());
            tvStatus.setTextColor(0xFFFF9800);
        }
    }

    public StepEditorFragment getEditorFragment() { return fragEditor; }
    public void switchToEditor() { bottomNav.setSelectedItemId(R.id.nav_editor); }
}
