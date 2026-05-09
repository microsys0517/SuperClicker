package com.superclicker.ui.script;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.superclicker.MainActivity;
import com.superclicker.R;
import com.superclicker.db.AppDatabase;
import com.superclicker.model.Script;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ScriptListFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvEmpty;
    private Adapter adapter;
    private AppDatabase db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.fragment_scripts, c, false);
        rv = v.findViewById(R.id.rv_scripts);
        tvEmpty = v.findViewById(R.id.tv_empty);
        v.findViewById(R.id.btn_add_script).setOnClickListener(x -> showCreate());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        db = AppDatabase.getInstance(requireContext());
        adapter = new Adapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        db.scriptDao().getAll().observe(getViewLifecycleOwner(), list -> {
            adapter.setData(list);
            tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
            rv.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void showCreate() {
        EditText et = new EditText(requireContext());
        et.setHint("脚本名称");
        et.setPadding(60, 40, 60, 20);
        new AlertDialog.Builder(requireContext())
            .setTitle("新建脚本").setView(et)
            .setPositiveButton("创建", (d, w) -> {
                String name = et.getText().toString().trim();
                if (name.isEmpty()) name = "未命名脚本";
                Script sc = new Script();
                sc.name = name;
                Executors.newSingleThreadExecutor().execute(() -> {
                    long id = db.scriptDao().insert(sc);
                    sc.id = id;
                    requireActivity().runOnUiThread(() -> openEditor(sc));
                });
            }).setNegativeButton("取消", null).show();
    }

    private void openEditor(Script sc) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getEditorFragment().loadScript(sc);
            ((MainActivity) getActivity()).switchToEditor();
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private List<Script> data = new ArrayList<>();
        void setData(List<Script> l) { data = l != null ? l : new ArrayList<>(); notifyDataSetChanged(); }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_script, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Script sc = data.get(pos);
            h.tvName.setText(sc.name);
            h.tvDesc.setText(sc.description != null ? sc.description : "");
            h.tvInfo.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(sc.updateTime)));
            h.itemView.setOnClickListener(v -> openEditor(sc));
            h.btnDel.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("删除").setMessage("删除「" + sc.name + "」？")
                .setPositiveButton("删除", (d, w) -> Executors.newSingleThreadExecutor().execute(() -> db.scriptDao().delete(sc)))
                .setNegativeButton("取消", null).show());
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc, tvInfo; ImageButton btnDel;
            VH(View v) { super(v); tvName = v.findViewById(R.id.tv_script_name); tvDesc = v.findViewById(R.id.tv_script_desc); tvInfo = v.findViewById(R.id.tv_script_info); btnDel = v.findViewById(R.id.btn_delete_script); }
        }
    }
}
