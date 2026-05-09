package com.superclicker.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.superclicker.R;
import com.superclicker.db.AppDatabase;
import com.superclicker.engine.ScriptEngine;
import com.superclicker.model.Script;
import com.superclicker.model.Step;
import com.superclicker.model.StepType;
import com.superclicker.service.FloatingService;
import com.superclicker.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class StepEditorFragment extends Fragment {

    private static final String TAG = "Editor";
    private RecyclerView rv;
    private TextView tvName, tvCount;
    private StepAdapter adapter;
    private AppDatabase db;
    private Script script;
    private ScriptEngine engine;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.fragment_step_editor, c, false);
        rv = v.findViewById(R.id.rv_steps);
        tvName = v.findViewById(R.id.tv_script_name);
        tvCount = v.findViewById(R.id.tv_step_count);

        v.findViewById(R.id.btn_back).setOnClickListener(x -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        v.findViewById(R.id.btn_add_step).setOnClickListener(x -> addStep());

        v.findViewById(R.id.btn_record).setOnClickListener(x -> record());

        v.findViewById(R.id.btn_delete_step).setOnClickListener(x -> deleteSelected());

        v.findViewById(R.id.btn_play).setOnClickListener(x -> play());

        v.findViewById(R.id.btn_config).setOnClickListener(x -> config());

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        db = AppDatabase.getInstance(requireContext());
        adapter = new StepAdapter(step -> editStep(step));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
    }

    public void loadScript(Script sc) {
        this.script = sc;
        if (tvName != null) tvName.setText(sc.name);
        refresh();
    }

    private void refresh() {
        if (script == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Step> list = db.stepDao().getByScriptIdSync(script.id);
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                adapter.setData(list);
                tvCount.setText(list.size() + " 步");
            });
        });
    }

    // ===== 添加步骤 - 弹出类型选择 =====
    private void addStep() {
        if (script == null) {
            Toast.makeText(requireContext(), "请先创建脚本", Toast.LENGTH_SHORT).show();
            return;
        }

        // 按类别分组显示
        String[] categories = {"操作", "识别", "判断", "工具", "控制"};
        StepType.Category[] catValues = StepType.Category.values();

        new AlertDialog.Builder(requireContext())
            .setTitle("选择步骤类别")
            .setItems(categories, (d, which) -> {
                showStepsInCategory(catValues[which]);
            })
            .show();
    }

    private void showStepsInCategory(StepType.Category category) {
        List<StepType> types = new ArrayList<>();
        for (StepType t : StepType.values()) {
            if (t.category == category) types.add(t);
        }

        String[] labels = new String[types.size()];
        String[] descs = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            labels[i] = types.get(i).label;
            descs[i] = types.get(i).description;
        }

        new AlertDialog.Builder(requireContext())
            .setTitle(category.label + "类步骤")
            .setItems(labels, (d, which) -> {
                createStep(types.get(which));
            })
            .show();
    }

    private void createStep(StepType type) {
        Step step = new Step();
        step.scriptId = script.id;
        step.type = type;
        step.label = type.label;

        // 根据类型弹出参数输入
        switch (type) {
            case CLICK:
                showCoordInput("点击坐标", step, true, false);
                break;
            case SWIPE:
                showCoordInput("滑动坐标", step, true, true);
                break;
            case LONG_PRESS:
                showCoordInput("长按坐标", step, true, false);
                break;
            case JUMP:
                showJumpInput(step);
                break;
            case INPUT_TEXT:
                showTextInput(step);
                break;
            case COUNTER:
                showCounterInput(step);
                break;
            case NUMBER_COMPARE:
            case TEXT_COMPARE:
            case TIME_COMPARE:
                showCompareInput(step);
                break;
            case TEXT_MATCH:
                showTextMatchInput(step);
                break;
            case IMAGE_MATCH:
                showImageMatchInput(step);
                break;
            default:
                saveStep(step);
                break;
        }
    }

    // 输入坐标
    private void showCoordInput(String title, Step step, boolean hasCoord1, boolean hasCoord2) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        android.widget.EditText etX1 = new android.widget.EditText(requireContext());
        etX1.setHint("X1 (横坐标)"); etX1.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        android.widget.EditText etY1 = new android.widget.EditText(requireContext());
        etY1.setHint("Y1 (纵坐标)"); etY1.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        layout.addView(etX1);
        layout.addView(etY1);

        android.widget.EditText etX2 = null, etY2 = null;
        if (hasCoord2) {
            etX2 = new android.widget.EditText(requireContext());
            etX2.setHint("X2 (终点横坐标)"); etX2.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            etY2 = new android.widget.EditText(requireContext());
            etY2.setHint("Y2 (终点纵坐标)"); etY2.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etX2);
            layout.addView(etY2);
        }

        android.widget.EditText etDuration = new android.widget.EditText(requireContext());
        etDuration.setHint("时长(ms) 默认300"); etDuration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etDuration.setText("300");
        layout.addView(etDuration);

        android.widget.EditText etWait = new android.widget.EditText(requireContext());
        etWait.setHint("执行后等待(ms) 默认500"); etWait.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etWait.setText("500");
        layout.addView(etWait);

        final android.widget.EditText fx2 = etX2, fy2 = etY2;

        new AlertDialog.Builder(requireContext())
            .setTitle(title).setView(layout)
            .setPositiveButton("保存", (d, w) -> {
                step.x1 = parseInt(etX1);
                step.y1 = parseInt(etY1);
                if (fx2 != null) step.x2 = parseInt(fx2);
                if (fy2 != null) step.y2 = parseInt(fy2);
                step.duration = parseLong(etDuration);
                step.waitAfter = parseLong(etWait);
                step.label = step.type.label + " (" + step.x1 + "," + step.y1 + ")";
                saveStep(step);
            })
            .setNegativeButton("取消", null).show();
    }

    // 输入跳转目标
    private void showJumpInput(Step step) {
        android.widget.EditText et = new android.widget.EditText(requireContext());
        et.setHint("跳转到第几步（从1开始）");
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setPadding(50, 30, 50, 10);

        new AlertDialog.Builder(requireContext())
            .setTitle("跳转步骤").setView(et)
            .setPositiveButton("保存", (d, w) -> {
                step.jumpTarget = parseInt(et) - 1;
                step.label = "跳转→步骤" + (step.jumpTarget + 1);
                saveStep(step);
            }).setNegativeButton("取消", null).show();
    }

    // 输入文本
    private void showTextInput(Step step) {
        android.widget.EditText et = new android.widget.EditText(requireContext());
        et.setHint("要输入的文本内容");
        et.setPadding(50, 30, 50, 10);

        new AlertDialog.Builder(requireContext())
            .setTitle("输入文本").setView(et)
            .setPositiveButton("保存", (d, w) -> {
                step.inputText = et.getText().toString().trim();
                step.label = "输入: " + step.inputText;
                saveStep(step);
            }).setNegativeButton("取消", null).show();
    }

    // 计数器
    private void showCounterInput(Step step) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        android.widget.EditText etName = new android.widget.EditText(requireContext());
        etName.setHint("计数器名称"); etName.setText("counter1");
        android.widget.EditText etValue = new android.widget.EditText(requireContext());
        etValue.setHint("数值"); etValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etValue.setText("1");
        android.widget.EditText etOp = new android.widget.EditText(requireContext());
        etOp.setHint("操作: set / add / sub"); etOp.setText("add");

        layout.addView(etName); layout.addView(etValue); layout.addView(etOp);

        new AlertDialog.Builder(requireContext())
            .setTitle("计数器").setView(layout)
            .setPositiveButton("保存", (d, w) -> {
                step.counterName = etName.getText().toString().trim();
                step.counterValue = parseInt(etValue);
                step.counterOp = etOp.getText().toString().trim();
                step.label = "计数 " + step.counterName + " " + step.counterOp + " " + step.counterValue;
                saveStep(step);
            }).setNegativeButton("取消", null).show();
    }

    // 比较
    private void showCompareInput(Step step) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        android.widget.EditText etV1 = new android.widget.EditText(requireContext());
        etV1.setHint("值1");
        android.widget.EditText etOp = new android.widget.EditText(requireContext());
        etOp.setHint("运算符: > < == >= <= != contains"); etOp.setText("==");
        android.widget.EditText etV2 = new android.widget.EditText(requireContext());
        etV2.setHint("值2");

        layout.addView(etV1); layout.addView(etOp); layout.addView(etV2);

        new AlertDialog.Builder(requireContext())
            .setTitle(step.type.label).setView(layout)
            .setPositiveButton("保存", (d, w) -> {
                step.compareValue1 = etV1.getText().toString().trim();
                step.compareOperator = etOp.getText().toString().trim();
                step.compareValue2 = etV2.getText().toString().trim();
                step.label = step.compareValue1 + " " + step.compareOperator + " " + step.compareValue2;
                saveStep(step);
            }).setNegativeButton("取消", null).show();
    }

    // 识字
    private void showTextMatchInput(Step step) {
        android.widget.EditText et = new android.widget.EditText(requireContext());
        et.setHint("要查找的文字");
        et.setPadding(50, 30, 50, 10);

        new AlertDialog.Builder(requireContext())
            .setTitle("识字查找").setView(et)
            .setPositiveButton("保存", (d, w) -> {
                step.matchText = et.getText().toString().trim();
                step.label = "识字: " + step.matchText;
                saveStep(step);
            }).setNegativeButton("取消", null).show();
    }

    // 识图
    private void showImageMatchInput(Step step) {
        android.widget.EditText et = new android.widget.EditText(requireContext());
        et.setHint("图片文件路径（如 /sdcard/template.png）");
        et.setPadding(50, 30, 50, 10);

        new AlertDialog.Builder(requireContext())
            .setTitle("识图查找").setView(et)
            .setPositiveButton("保存", (d, w) -> {
                step.matchImagePath = et.getText().toString().trim();
                step.label = "识图: " + step.matchImagePath;
                saveStep(step);
            }).setNegativeButton("取消", null).show();
    }

    // 编辑已有步骤
    private void editStep(Step step) {
        switch (step.type) {
            case CLICK:
            case SWIPE:
            case LONG_PRESS:
                boolean hasS = step.type == StepType.SWIPE;
                showCoordInput("编辑: " + step.type.label, step, true, hasS);
                break;
            case JUMP:
                showJumpInput(step);
                break;
            case INPUT_TEXT:
                showTextInput(step);
                break;
            default:
                Toast.makeText(requireContext(), "编辑: " + step.type.label, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void saveStep(Step step) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (step.id == 0) {
                int max = db.stepDao().getMaxOrder(script.id);
                step.order = max + 1;
                db.stepDao().insert(step);
            } else {
                db.stepDao().update(step);
            }
            if (getActivity() != null) getActivity().runOnUiThread(this::refresh);
            Logger.i(TAG, "步骤已保存: " + step.type.label);
        });
    }

    // ===== 录制 =====
    private void record() {
        if (script == null) {
            Toast.makeText(requireContext(), "请先创建脚本", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), "录制模式：回到桌面操作，自动记录坐标", Toast.LENGTH_LONG).show();
        Logger.i(TAG, "录制功能提示：请手动添加步骤");
    }

    // ===== 执行 =====
    private void play() {
        if (script == null) {
            Toast.makeText(requireContext(), "没有脚本", Toast.LENGTH_SHORT).show();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Step> steps = db.stepDao().getByScriptIdSync(script.id);
            if (steps.isEmpty()) {
                if (getActivity() != null) getActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "脚本为空，请先添加步骤", Toast.LENGTH_SHORT).show());
                return;
            }
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                engine = new ScriptEngine(requireContext());
                engine.start(script, steps);
                Logger.i(TAG, "脚本开始执行: " + script.name);
                Toast.makeText(requireContext(), "脚本已启动，查看悬浮控制栏", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // ===== 设置 =====
    private void config() {
        if (script == null) {
            Toast.makeText(requireContext(), "没有脚本", Toast.LENGTH_SHORT).show();
            return;
        }

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        android.widget.EditText etLoop = new android.widget.EditText(requireContext());
        etLoop.setHint("循环次数 (0=无限)"); etLoop.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etLoop.setText(String.valueOf(script.config.loopCount));

        android.widget.EditText etInterval = new android.widget.EditText(requireContext());
        etInterval.setHint("循环间隔(ms)"); etInterval.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etInterval.setText(String.valueOf(script.config.loopInterval));

        android.widget.EditText etStart = new android.widget.EditText(requireContext());
        etStart.setHint("开始步骤(从0开始)"); etStart.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etStart.setText(String.valueOf(script.config.startStep));

        layout.addView(etLoop);
        layout.addView(etInterval);
        layout.addView(etStart);

        new AlertDialog.Builder(requireContext())
            .setTitle("脚本设置").setView(layout)
            .setPositiveButton("保存", (d, w) -> {
                script.config.loopCount = parseInt(etLoop);
                script.config.loopInterval = parseLong(etInterval);
                script.config.startStep = parseInt(etStart);
                Executors.newSingleThreadExecutor().execute(() -> db.scriptDao().update(script));
                Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("取消", null).show();
    }

    private void deleteSelected() {
        List<Step> sel = adapter.getSelectedSteps();
        if (sel.isEmpty()) {
            Toast.makeText(requireContext(), "长按步骤选择后删除", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
            .setTitle("删除").setMessage("删除 " + sel.size() + " 个步骤？")
            .setPositiveButton("删除", (d, w) -> Executors.newSingleThreadExecutor().execute(() -> {
                List<Long> ids = new ArrayList<>();
                for (Step s : sel) ids.add(s.id);
                db.stepDao().deleteByIds(ids);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    adapter.clearSelection();
                    refresh();
                });
            })).setNegativeButton("取消", null).show();
    }

    private int parseInt(android.widget.EditText et) {
        try { return Integer.parseInt(et.getText().toString().trim()); } catch (Exception e) { return 0; }
    }
    private long parseLong(android.widget.EditText et) {
        try { return Long.parseLong(et.getText().toString().trim()); } catch (Exception e) { return 0; }
    }
}
