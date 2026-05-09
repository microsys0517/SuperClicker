package com.superclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.superclicker.ui.script.ScriptListFragment;
import com.superclicker.ui.editor.StepEditorFragment;
import com.superclicker.ui.log.LogFragment;
import com.superclicker.ui.settings.SettingsFragment;
import com.superclicker.service.FloatingControlService;
import com.superclicker.service.ClickAccessibilityService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNav;
    private TextView tvStatus;
    private final ScriptListFragment scriptListFragment = new ScriptListFragment();
    private final StepEditorFragment stepEditorFragment = new StepEditorFragment();
    private final LogFragment logFragment = new LogFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            tvStatus = findViewById(R.id.tv_main_status);
            bottomNav = findViewById(R.id.bottom_nav);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scriptListFragment)
                    .commit();
            }

            bottomNav.setOnItemSelectedListener(item -> {
                Fragment f;
                int id = item.getItemId();
                if (id == R.id.nav_scripts) f = scriptListFragment;
                else if (id == R.id.nav_editor) f = stepEditorFragment;
                else if (id == R.id.nav_log) f = logFragment;
                else if (id == R.id.nav_settings) f = settingsFragment;
                else return false;
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f)
                    .commit();
                return true;
            });

            // 延迟启动悬浮窗服务，避免启动崩溃
            new Handler(Looper.getMainLooper()).postDelayed(this::startFloatingService, 1000);

        } catch (Exception e) {
            Log.e(TAG, "onCreate 异常: " + e.getMessage(), e);
        }
    }

    private void startFloatingService() {
        try {
            if (Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(this, FloatingControlService.class);
                startForegroundService(intent);
                Log.i(TAG, "悬浮窗服务已启动");
            } else {
                Log.i(TAG, "悬浮窗权限未开启，跳过启动服务");
            }
        } catch (Exception e) {
            Log.e(TAG, "启动悬浮窗服务失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            boolean a11y = ClickAccessibilityService.isRunning();
            boolean overlay = Settings.canDrawOverlays(this);
            if (a11y && overlay) {
                tvStatus.setText("就绪");
                tvStatus.setTextColor(0xFF4CAF50);
            } else {
                StringBuilder sb = new StringBuilder();
                if (!a11y) sb.append("需无障碍");
                if (!overlay) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append("需悬浮窗");
                }
                tvStatus.setText(sb.toString());
                tvStatus.setTextColor(0xFFFF9800);
            }
        } catch (Exception e) {
            Log.e(TAG, "onResume 异常: " + e.getMessage(), e);
        }
    }

    public StepEditorFragment getEditorFragment() {
        return stepEditorFragment;
    }

    public void switchToEditor() {
        bottomNav.setSelectedItemId(R.id.nav_editor);
    }
}
