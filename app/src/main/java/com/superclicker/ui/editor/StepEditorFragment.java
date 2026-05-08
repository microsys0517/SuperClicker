package com.superclicker.ui.editor;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.superclicker.R;
import com.superclicker.db.AppDatabase;
import com.superclicker.engine.ScriptEngine;
import com.superclicker.model.*;
import com.superclicker.util.Logger;
import java.util.*;
import java.util.concurrent.Executors;

public class StepEditorFragment extends Fragment {
    private RecyclerView rvSteps;
    private TextView tvScriptName, tvStepCount;
    private StepAdapter adapter;
    private AppDatabase db;
    private Script currentScript;
    private ScriptEngine engine;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_editor, container, false);
        rvSteps = view.findViewById(R.id.rv_steps);
        tvScriptName = view.findViewById(R.id.tv_script_name);
        tvStepCount = view.findViewById(R.id.tv_step_count);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> { if (getActivity() != null) getActivity().onBackPressed(); });
        view.findViewById(R.id.btn_add_step).setOnClickListener(v -> showAddStepDialog());
        view.findViewById(R.id.btn_record).setOnClickListener(v -> Toast.makeText(requireContext(), "录制功能开发中", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btn_delete_step).setOnClickListener(v -> deleteSelected());
        view.findViewById(R.id.btn_play).setOnClickListener(v -> executeScript());
        view.findViewById(R.id.btn_config).setOnClickListener(v -> Toast.makeText(requireContext(), "设置功能开发中", Toast.LENGTH_SHORT).show());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        adapter = new StepAdapter(this::showStepConfig);
        rvSteps.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSteps.setAdapter(adapter);
    }

    public void loadScript(Script script) {
        currentScript = script;
        if (tvScriptName != null) tvScriptName.setText(script.name);
        refreshSteps();
    }

    private void refreshSteps() {
        if (currentScript == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Step> steps = db.stepDao().getByScriptIdSync(currentScript.id);
            requireActivity().runOnUiThread(() -> { adapter.setData(steps); tvStepCount.setText(steps.size() + " 步"); });
        });
    }

    private void showAddStepDialog() {
        if (currentScript == null) { Toast.makeText(requireContext(), "请先选择脚本", Toast.LENGTH_SHORT).show(); return; }
        new AddStepDialog(requireContext(), type -> {
            Step step = new Step();
            step.scriptId = currentScript.id;
            step.type = type;
            step.label = type.label;
            Executors.newSingleThreadExecutor().execute(() -> {
                step.order = db.stepDao().getMaxOrder(currentScript.id) + 1;
                db.stepDao().insert(step);
                requireActivity().runOnUiThread(this::refreshSteps);
            });
        }).show();
    }

    private void showStepConfig(Step step) {
        new StepConfigDialog(requireContext(), step, updated -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                db.stepDao().update(updated);
                requireActivity().runOnUiThread(this::refreshSteps);
            });
        }).show();
    }

    private void deleteSelected() {
        List<Step> sel = adapter.getSelectedSteps();
        if (sel.isEmpty()) { Toast.makeText(requireContext(), "长按选择步骤后删除", Toast.LENGTH_SHORT).show(); return; }
        new AlertDialog.Builder(requireContext()).setTitle("删除").setMessage("删除 " + sel.size() + " 个步骤？")
            .setPositiveButton("删除", (d, w) -> Executors.newSingleThreadExecutor().execute(() -> {
                List<Long> ids = new ArrayList<>();
                for (Step s : sel) ids.add(s.id);
                db.stepDao().deleteByIds(ids);
                requireActivity().runOnUiThread(() -> { adapter.clearSelection(); refreshSteps(); });
            })).setNegativeButton("取消", null).show();
    }

    private void executeScript() {
        if (currentScript == null) { Toast.makeText(requireContext(), "没有脚本", Toast.LENGTH_SHORT).show(); return; }
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Step> steps = db.stepDao().getByScriptIdSync(currentScript.id);
            if (steps.isEmpty()) { requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "脚本为空", Toast.LENGTH_SHORT).show()); return; }
            requireActivity().runOnUiThread(() -> {
                engine = new ScriptEngine(requireContext());
                engine.start(currentScript, steps);
            });
        });
    }
}
