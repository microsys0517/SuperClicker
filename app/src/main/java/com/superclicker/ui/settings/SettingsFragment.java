package com.superclicker.ui.settings;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.superclicker.R;

public class SettingsFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.btn_save_settings).setOnClickListener(v -> Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btn_export).setOnClickListener(v -> Toast.makeText(requireContext(), "导出功能开发中", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btn_import).setOnClickListener(v -> Toast.makeText(requireContext(), "导入功能开发中", Toast.LENGTH_SHORT).show());
        return view;
    }
}
