package com.superclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
    private BottomNavigationView bottomNav;
    private TextView tvStatus;
    private final ScriptListFragment scriptListFragment = new ScriptListFragment();
    private final StepEditorFragment stepEditorFragment = new StepEditorFragment();
    private final LogFragment logFragment = new LogFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = findViewById(R.id.tv_main_status);
        bottomNav = findViewById(R.id.bottom_nav);
        if (savedInstanceState == null) loadFragment(scriptListFragment);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_scripts) f = scriptListFragment;
            else if (id == R.id.nav_editor) f = stepEditorFragment;
            else if (id == R.id.nav_log) f = logFragment;
            else if (id == R.id.nav_settings) f = settingsFragment;
            else return false;
            loadFragment(f);
            return true;
        });
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
    }

    private void checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        }
        if (Settings.canDrawOverlays(this)) {
            startForegroundService(new Intent(this, FloatingControlService.class));
        }
    }

    private void updateStatus() {
        boolean a11y = ClickAccessibilityService.isRunning();
        boolean overlay = Settings.canDrawOverlays(this);
        if (a11y && overlay) {
            tvStatus.setText("就绪");
            tvStatus.setTextColor(0xFF4CAF50);
        } else {
            StringBuilder sb = new StringBuilder();
            if (!a11y) sb.append("需无障碍");
            if (!overlay) { if (sb.length() > 0) sb.append(" | "); sb.append("需悬浮窗"); }
            tvStatus.setText(sb.toString());
            tvStatus.setTextColor(0xFFFF9800);
        }
    }

    public StepEditorFragment getEditorFragment() { return stepEditorFragment; }
    public void switchToEditor() { bottomNav.setSelectedItemId(R.id.nav_editor); }
}
