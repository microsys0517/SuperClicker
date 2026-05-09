package com.superclicker.ui.editor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.superclicker.R;
import com.superclicker.model.Step;
import com.superclicker.model.StepType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.VH> {

    public interface OnClickListener { void onClick(Step step); }

    private List<Step> data = new ArrayList<>();
    private final Set<Long> selected = new HashSet<>();
    private boolean selMode = false;
    private final OnClickListener listener;

    public StepAdapter(OnClickListener l) { this.listener = l; }
    public void setData(List<Step> l) { data = l != null ? l : new ArrayList<>(); notifyDataSetChanged(); }
    public List<Step> getSelectedSteps() {
        List<Step> r = new ArrayList<>();
        for (Step s : data) if (selected.contains(s.id)) r.add(s);
        return r;
    }
    public void clearSelection() { selected.clear(); selMode = false; notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_step, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Step s = data.get(pos);
        h.tvNum.setText(String.valueOf(pos + 1));
        h.tvType.setText("[" + s.type.label + "]");
        h.tvType.setTextColor(color(s.type.category));
        h.tvDetail.setText(detail(s));

        StringBuilder w = new StringBuilder();
        if (s.waitBefore > 0) w.append("前").append(s.waitBefore).append("ms ");
        if (s.waitAfter > 0) w.append("后").append(s.waitAfter).append("ms ");
        h.tvWait.setText(w.toString().trim());

        h.sw.setOnCheckedChangeListener(null);
        h.sw.setChecked(s.enabled);
        h.sw.setOnCheckedChangeListener((b, c) -> s.enabled = c);

        h.cb.setVisibility(selMode ? View.VISIBLE : View.GONE);
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(selected.contains(s.id));
        h.cb.setOnCheckedChangeListener((b, c) -> { if (c) selected.add(s.id); else selected.remove(s.id); });

        h.itemView.setOnClickListener(v -> {
            if (selMode) {
                if (selected.contains(s.id)) selected.remove(s.id); else selected.add(s.id);
                notifyItemChanged(pos);
            } else {
                listener.onClick(s);
            }
        });
        h.itemView.setOnLongClickListener(v -> {
            selMode = true;
            selected.add(s.id);
            notifyDataSetChanged();
            return true;
        });
    }

    @Override public int getItemCount() { return data.size(); }

    private String detail(Step s) {
        switch (s.type) {
            case CLICK: return "(" + s.x1 + "," + s.y1 + ")";
            case SWIPE: return "(" + s.x1 + "," + s.y1 + ")->(" + s.x2 + "," + s.y2 + ") " + s.duration + "ms";
            case LONG_PRESS: return "(" + s.x1 + "," + s.y1 + ") " + s.duration + "ms";
            case JUMP: return "→步骤" + (s.jumpTarget + 1);
            case INPUT_TEXT: return s.inputText != null ? s.inputText : "";
            case TEXT_MATCH: return "查找:" + (s.matchText != null ? s.matchText : "");
            case IMAGE_MATCH: return s.matchImagePath != null ? s.matchImagePath : "";
            default: return s.type.description;
        }
    }

    private int color(StepType.Category c) {
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
        TextView tvNum, tvType, tvDetail, tvWait;
        CheckBox cb;
        Switch sw;
        VH(View v) {
            super(v);
            tvNum = v.findViewById(R.id.tv_step_number);
            tvType = v.findViewById(R.id.tv_step_type);
            tvDetail = v.findViewById(R.id.tv_step_detail);
            tvWait = v.findViewById(R.id.tv_step_wait);
            cb = v.findViewById(R.id.cb_select);
            sw = v.findViewById(R.id.switch_enabled);
        }
    }
}
