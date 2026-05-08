package com.superclicker.ui.editor;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.superclicker.R;
import com.superclicker.model.Step;
import com.superclicker.model.StepType;
import java.util.*;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.VH> {
    public interface OnStepClickListener { void onClick(Step step); }
    private List<Step> data = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private boolean selectionMode = false;
    private final OnStepClickListener listener;

    public StepAdapter(OnStepClickListener l) { this.listener = l; }
    public void setData(List<Step> l) { data = l != null ? l : new ArrayList<>(); notifyDataSetChanged(); }
    public List<Step> getSelectedSteps() {
        List<Step> r = new ArrayList<>();
        for (Step s : data) if (selectedIds.contains(s.id)) r.add(s);
        return r;
    }
    public void clearSelection() { selectedIds.clear(); selectionMode = false; notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_step, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Step step = data.get(pos);
        h.tvNumber.setText(String.valueOf(step.order));
        h.tvType.setText("[" + step.type.label + "]");
        h.tvType.setTextColor(getColor(step.type.category));
        h.tvDetail.setText(getDetail(step));
        String wait = "";
        if (step.waitBefore > 0) wait += "前" + step.waitBefore + "ms ";
        if (step.waitAfter > 0) wait += "后" + step.waitAfter + "ms ";
        h.tvWait.setText(wait.trim());
        h.switchEnabled.setChecked(step.enabled);
        h.switchEnabled.setOnCheckedChangeListener((b, c) -> step.enabled = c);
        h.cbSelect.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        h.cbSelect.setChecked(selectedIds.contains(step.id));
        h.cbSelect.setOnCheckedChangeListener((b, c) -> { if (c) selectedIds.add(step.id); else selectedIds.remove(step.id); });
        h.itemView.setOnClickListener(v -> {
            if (selectionMode) { if (selectedIds.contains(step.id)) selectedIds.remove(step.id); else selectedIds.add(step.id); notifyItemChanged(pos); }
            else listener.onClick(step);
        });
        h.itemView.setOnLongClickListener(v -> { selectionMode = true; selectedIds.add(step.id); notifyDataSetChanged(); return true; });
    }

    @Override public int getItemCount() { return data.size(); }

    private String getDetail(Step s) {
        switch (s.type) {
            case CLICK: return "(" + s.x1 + "," + s.y1 + ")";
            case SWIPE: return "(" + s.x1 + "," + s.y1 + ")->(" + s.x2 + "," + s.y2 + ")";
            case LONG_PRESS: return "(" + s.x1 + "," + s.y1 + ") " + s.duration + "ms";
            case JUMP: return "→步骤" + s.jumpTarget;
            case INPUT_TEXT: return s.inputText != null ? s.inputText : "";
            case TEXT_MATCH: return "查找:" + (s.matchText != null ? s.matchText : "");
            default: return s.type.description;
        }
    }

    private int getColor(StepType.Category c) {
        switch (c) {
            case ACTION: return 0xFF1976D2;
            case RECOGNIZE: return 0xFF388E3C;
            case CONDITION: return 0xFFF57C00;
            case TOOL: return 0xFF7B1FA2;
            case CONTROL: return 0xFFD32F2F;
            default: return 0xFF757575;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNumber, tvType, tvDetail, tvWait;
        CheckBox cbSelect;
        Switch switchEnabled;
        VH(View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tv_step_number);
            tvType = v.findViewById(R.id.tv_step_type);
            tvDetail = v.findViewById(R.id.tv_step_detail);
            tvWait = v.findViewById(R.id.tv_step_wait);
            cbSelect = v.findViewById(R.id.cb_select);
            switchEnabled = v.findViewById(R.id.switch_enabled);
        }
    }
}
