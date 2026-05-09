package com.superclicker.ui.settings;
import android.os.Bundle;import android.view.*;import android.widget.Toast;
import androidx.annotation.*;import androidx.fragment.app.Fragment;import com.superclicker.R;
public class SettingsFragment extends Fragment{@Nullable @Override
public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle s){
View v=i.inflate(R.layout.fragment_settings,c,false);
v.findViewById(R.id.btn_save_settings).setOnClickListener(x->Toast.makeText(requireContext(),"设置已保存",Toast.LENGTH_SHORT).show());
v.findViewById(R.id.btn_export).setOnClickListener(x->Toast.makeText(requireContext(),"导出功能后续版本支持",Toast.LENGTH_SHORT).show());
v.findViewById(R.id.btn_import).setOnClickListener(x->Toast.makeText(requireContext(),"导入功能后续版本支持",Toast.LENGTH_SHORT).show());
return v;}}
