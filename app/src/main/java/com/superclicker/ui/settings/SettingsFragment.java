package com.superclicker.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.superclicker.R;
import com.superclicker.service.ClickService;

public class SettingsFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.fragment_settings, c, false);

        v.findViewById(R.id.btn_save_settings).setOnClickListener(x ->
            Toast.makeText(requireContext(), "脚本设置请在编辑页面点击「设置」按钮", Toast.LENGTH_LONG).show());

        v.findViewById(R.id.btn_export).setOnClickListener(x ->
            Toast.makeText(requireContext(), "导出功能开发中", Toast.LENGTH_SHORT).show());

        v.findViewById(R.id.btn_import).setOnClickListener(x ->
            Toast.makeText(requireContext(), "导入功能开发中", Toast.LENGTH_SHORT).show());

        return v;
    }
}
