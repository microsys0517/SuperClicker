package com.superclicker.ui.script;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.superclicker.*;
import com.superclicker.db.AppDatabase;
import com.superclicker.model.Script;
import com.superclicker.ui.editor.StepEditorFragment;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class ScriptListFragment extends Fragment {
    private RecyclerView rvScripts;
    private TextView tvEmpty;
    private ScriptAdapter adapter;
    private AppDatabase db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scripts, container, false);
        rvScripts = view.findViewById(R.id.rv_scripts);
        tvEmpty = view.findViewById(R.id.tv_empty);
        view.findViewById(R.id.btn_add_script).setOnClickListener(v -> showCreateDialog());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        adapter = new ScriptAdapter();
        rvScripts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvScripts.setAdapter(adapter);
        db.scriptDao().getAll().observe(getViewLifecycleOwner(), scripts -> {
            adapter.setData(scripts);
            tvEmpty.setVisibility(scripts == null || scripts.isEmpty() ? View.VISIBLE : View.GONE);
            rvScripts.setVisibility(scripts == null || scripts.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void showCreateDialog() {
        EditText et = new EditText(requireContext());
        et.setHint("脚本名称");
        et.setPadding(60, 40, 60, 20);
        new AlertDialog.Builder(requireContext()).setTitle("新建脚本").setView(et)
            .setPositiveButton("创建", (d, w) -> {
                String name = et.getText().toString().trim();
                if (name.isEmpty()) name = "未命名脚本";
                Script s = new Script();
                s.name = name;
                Executors.newSingleThreadExecutor().execute(() -> {
                    long id = db.scriptDao().insert(s);
                    s.id = id;
                    requireActivity().runOnUiThread(() -> openEditor(s));
                });
            }).setNegativeButton("取消", null).show();
    }

    private void openEditor(Script script) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getEditorFragment().loadScript(script);
            ((MainActivity) getActivity()).switchToEditor();
        }
    }

    private class ScriptAdapter extends RecyclerView.Adapter<VH> {
        private List<Script> data = new ArrayList<>();
        void setData(List<Script> l) { data = l != null ? l : new ArrayList<>(); notifyDataSetChanged(); }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) { return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_script, p, false)); }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Script s = data.get(pos);
            h.tvName.setText(s.name);
            h.tvDesc.setText(s.description != null ? s.description : "");
            h.tvInfo.setText("更新: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(s.updateTime)));
            h.itemView.setOnClickListener(v -> openEditor(s));
            h.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(requireContext()).setTitle("删除").setMessage("删除「" + s.name + "」？")
                .setPositiveButton("删除", (d, w) -> Executors.newSingleThreadExecutor().execute(() -> db.scriptDao().delete(s)))
                .setNegativeButton("取消", null).show());
        }
        @Override public int getItemCount() { return data.size(); }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvInfo;
        View btnDelete;
        VH(View v) { super(v); tvName = v.findViewById(R.id.tv_script_name); tvDesc = v.findViewById(R.id.tv_script_desc); tvInfo = v.findViewById(R.id.tv_script_info); btnDelete = v.findViewById(R.id.btn_delete_script); }
    }
}
