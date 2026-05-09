package com.superclicker.ui.editor;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
        final EditText etLabel = findViewById(R.id.et_label);
        final EditText etX1 = findViewById(R.id.et_x1);
        final EditText etY1 = findViewById(R.id.et_y1);
        final EditText etX2 = findViewById(R.id.et_x2);
        final EditText etY2 = findViewById(R.id.et_y2);
        final EditText etDuration = findViewById(R.id.et_duration);
        final EditText etImage = findViewById(R.id.et_image_path);
        final EditText etText = findViewById(R.id.et_match_text);
        final EditText etInput = findViewById(R.id.et_input_text);
        final EditText etJump = findViewById(R.id.et_jump_target);
        final EditText etWb = findViewById(R.id.et_wait_before);
        final EditText etWa = findViewById(R.id.et_wait_after);
        final EditText etRc = findViewById(R.id.et_retry_count);
        final EditText etRd = findViewById(R.id.et_retry_delay);
        etLabel.setText(step.label != null ? step.label : "");
        etX1.setText(String.valueOf(step.x1)); etY1.setText(String.valueOf(step.y1));
        etX2.setText(String.valueOf(step.x2)); etY2.setText(String.valueOf(step.y2));
        etDuration.setText(String.valueOf(step.duration));
        etWb.setText(String.valueOf(step.waitBefore)); etWa.setText(String.valueOf(step.waitAfter));
        etRc.setText(String.valueOf(step.retryCount)); etRd.setText(String.valueOf(step.retryDelay));
        if (step.matchImagePath != null) etImage.setText(step.matchImagePath);
        if (step.matchText != null) etText.setText(step.matchText);
        if (step.inputText != null) etInput.setText(step.inputText);
        etJump.setText(String.valueOf(step.jumpTarget));
        LinearLayout lc = findViewById(R.id.layout_coords);
        LinearLayout lc2 = findViewById(R.id.layout_coords2);
        LinearLayout ld = findViewById(R.id.layout_duration);
        LinearLayout li = findViewById(R.id.layout_image);
        LinearLayout lt = findViewById(R.id.layout_text_match);
        LinearLayout lin = findViewById(R.id.layout_input);
        LinearLayout lj = findViewById(R.id.layout_jump);
        lc.setVisibility(View.GONE); lc2.setVisibility(View.GONE); ld.setVisibility(View.GONE);
        li.setVisibility(View.GONE); lt.setVisibility(View.GONE); lin.setVisibility(View.GONE); lj.setVisibility(View.GONE);
        switch (step.type) {
            case CLICK: case LONG_PRESS: lc.setVisibility(View.VISIBLE); if (step.type == StepType.LONG_PRESS) ld.setVisibility(View.VISIBLE); break;
            case SWIPE: lc.setVisibility(View.VISIBLE); lc2.setVisibility(View.VISIBLE); ld.setVisibility(View.VISIBLE); break;
            case IMAGE_MATCH: li.setVisibility(View.VISIBLE); break;
            case TEXT_MATCH: lt.setVisibility(View.VISIBLE); break;
            case INPUT_TEXT: lin.setVisibility(View.VISIBLE); break;
            case JUMP: lj.setVisibility(View.VISIBLE); break;
            default: break;
        }
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            step.label = etLabel.getText().toString().trim();
            step.x1 = pi(etX1); step.y1 = pi(etY1); step.x2 = pi(etX2); step.y2 = pi(etY2);
            step.duration = pl(etDuration); step.waitBefore = pl(etWb); step.waitAfter = pl(etWa);
            step.retryCount = pi(etRc); step.retryDelay = pl(etRd);
            step.matchImagePath = etImage.getText().toString().trim();
            step.matchText = etText.getText().toString().trim();
            step.inputText = etInput.getText().toString().trim();
            step.jumpTarget = pi(etJump);
            listener.onSave(step); dismiss();
        });
        findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
    }
    private int pi(EditText et) { try { return Integer.parseInt(et.getText().toString().trim()); } catch (Exception e) { return 0; } }
    private long pl(EditText et) { try { return Long.parseLong(et.getText().toString().trim()); } catch (Exception e) { return 0; } }
}
