package com.superclicker.ui.editor;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import com.superclicker.R;
import com.superclicker.model.Step;
import com.superclicker.model.StepType;

public class StepConfigDialog extends Dialog {
    public interface OnSaveListener { void onSave(Step step); }
    private final Step step;
    private final OnSaveListener listener;

    public StepConfigDialog(@NonNull Context ctx, Step step, OnSaveListener l) { super(ctx); this.step = step; this.listener = l; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_step_config);
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ((TextView) findViewById(R.id.tv_config_title)).setText("配置: " + step.type.label);

        EditText etLabel = findViewById(R.id.et_label);
        EditText etX1 = findViewById(R.id.et_x1);
        EditText etY1 = findViewById(R.id.et_y1);
        EditText etX2 = findViewById(R.id.et_x2);
        EditText etY2 = findViewById(R.id.et_y2);
        EditText etDuration = findViewById(R.id.et_duration);
        EditText etImage = findViewById(R.id.et_image_path);
        EditText etText = findViewById(R.id.et_match_text);
        EditText etInput = findViewById(R.id.et_input_text);
        EditText etJump = findViewById(R.id.et_jump_target);
        EditText etWb = findViewById(R.id.et_wait_before);
        EditText etWa = findViewById(R.id.et_wait_after);
        EditText etRc = findViewById(R.id.et_retry_count);
        EditText etRd = findViewById(R.id.et_retry_delay);

        etLabel.setText(step.label != null ? step.label : "");
        etX1.setText(String.valueOf(step.x1));
        etY1.setText(String.valueOf(step.y1));
        etX2.setText(String.valueOf(step.x2));
        etY2.setText(String.valueOf(step.y2));
        etDuration.setText(String.valueOf(step.duration));
        etWb.setText(String.valueOf(step.waitBefore));
        etWa.setText(String.valueOf(step.waitAfter));
        etRc.setText(String.valueOf(step.retryCount));
        etRd.setText(String.valueOf(step.retryDelay));
        if (step.matchImagePath != null) etImage.setText(step.matchImagePath);
        if (step.matchText != null) etText.setText(step.matchText);
        if (step.inputText != null) etInput.setText(step.inputText);
        etJump.setText(String.valueOf(step.jumpTarget));

        // 根据类型显示隐藏
        findViewById(R.id.layout_coords).setVisibility(
            (step.type == StepType.CLICK || step.type == StepType.LONG_PRESS || step.type == StepType.SWIPE) ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_coords2).setVisibility(step.type == StepType.SWIPE ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_duration).setVisibility(
            (step.type == StepType.LONG_PRESS || step.type == StepType.SWIPE) ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_image).setVisibility(step.type == StepType.IMAGE_MATCH ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_text_match).setVisibility(step.type == StepType.TEXT_MATCH ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_input).setVisibility(step.type == StepType.INPUT_TEXT ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_jump).setVisibility(step.type == StepType.JUMP ? View.VISIBLE : View.GONE);

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            step.label = etLabel.getText().toString().trim();
            step.x1 = pi(etX1); step.y1 = pi(etY1);
            step.x2 = pi(etX2); step.y2 = pi(etY2);
            step.duration = pl(etDuration);
            step.waitBefore = pl(etWb); step.waitAfter = pl(etWa);
            step.retryCount = pi(etRc); step.retryDelay = pl(etRd);
            step.matchImagePath = etImage.getText().toString().trim();
            step.matchText = etText.getText().toString().trim();
            step.inputText = etInput.getText().toString().trim();
            step.jumpTarget = pi(etJump);
            listener.onSave(step);
            dismiss();
        });
        findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
    }

    private int pi(EditText et) { try { return Integer.parseInt(et.getText().toString().trim()); } catch (Exception e) { return 0; } }
    private long pl(EditText et) { try { return Long.parseLong(et.getText().toString().trim()); } catch (Exception e) { return 0; } }
}
