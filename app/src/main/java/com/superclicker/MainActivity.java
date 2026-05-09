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
import com.superclicker.ui.script.ScriptListFragment;
import com.superclicker.ui.editor.StepEditorFragment;
import com.superclicker.ui.log.LogFragment;
import com.superclicker.ui.settings.SettingsFragment;
import com.superclicker.service.FloatingControlService;
import com.superclicker.service.ClickAccessibilityService;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainAct";
    private static final int REQ_NOTIF = 1001;
    private BottomNavigationView bottomNav;
    private TextView tvStatus;
    private final ScriptListFragment fScripts = new ScriptListFragment();
    private final StepEditorFragment fEditor = new StepEditorFragment();
    private final LogFragment fLog = new LogFragment();
    private final SettingsFragment fSettings = new SettingsFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            tvStatus = findViewById(R.id.tv_main_status);
            bottomNav = findViewById(R.id.bottom_nav);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fScripts).commit();
            }
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment f;
                int id = item.getItemId();
                if (id == R.id.nav_scripts) f = fScripts;
                else if (id == R.id.nav_editor) f = fEditor;
                else if (id == R.id.nav_log) f = fLog;
                else if (id == R.id.nav_settings) f = fSettings;
                else return false;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
                return true;
            });
            new Handler(Looper.getMainLooper()).postDelayed(this::checkPerms, 500);
        } catch (Exception e) { Log.e(TAG, "onCreate: " + e.getMessage()); }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        if (Settings.canDrawOverlays(this) && ClickAccessibilityService.isRunning()) startSvc();
    }
    private void checkPerms() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showDlg("通知权限", "需要通知权限提醒脚本状态", () -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF));
                return;
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            showDlg("悬浮窗权限", "需要悬浮窗权限显示控制面板", () -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));
            return;
        }
        if (!ClickAccessibilityService.isRunning()) {
            showDlg("无障碍服务", "需要无障碍服务执行自动化操作", () -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
            return;
        }
        startSvc();
    }
    private void showDlg(String t, String m, Runnable r) {
        new AlertDialog.Builder(this).setTitle(t).setMessage(m)
            .setPositiveButton("去开启", (d, w) -> { try { r.run(); } catch (Exception e) { Toast.makeText(this, "请手动到设置中开启", Toast.LENGTH_LONG).show(); } })
            .setNegativeButton("稍后", null).setCancelable(false).show();
    }
    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] p, @NonNull int[] r) {
        super.onRequestPermissionsResult(req, p, r);
        if (req == REQ_NOTIF) new Handler(Looper.getMainLooper()).postDelayed(this::checkPerms, 300);
    }
    private void startSvc() {
        try { if (Settings.canDrawOverlays(this)) startForegroundService(new Intent(this, FloatingControlService.class)); }
        catch (Exception e) { Log.e(TAG, "startSvc: " + e.getMessage()); }
    }
    private void updateStatus() {
        try {
            boolean a = ClickAccessibilityService.isRunning(), o = Settings.canDrawOverlays(this);
            if (a && o) { tvStatus.setText("就绪"); tvStatus.setTextColor(0xFF4CAF50); }
            else { tvStatus.setText((!o ? "[悬浮窗]" : "") + (!a ? "[无障碍]" : "") + " 未开启"); tvStatus.setTextColor(0xFFFF9800); }
        } catch (Exception e) { Log.e(TAG, "status: " + e.getMessage()); }
    }
    public StepEditorFragment getEditorFragment() { return fEditor; }
    public void switchToEditor() { bottomNav.setSelectedItemId(R.id.nav_editor); }
}
