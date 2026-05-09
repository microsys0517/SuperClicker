package com.superclicker.ui.editor;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.superclicker.R;
import com.superclicker.model.StepType;

import java.util.ArrayList;
import java.util.List;

public class AddStepDialog extends Dialog {

    public interface OnTypeSelectedListener {
        void onSelected(StepType type);
    }

    private final OnTypeSelectedListener listener;

    public AddStepDialog(@NonNull Context ctx, OnTypeSelectedListener l) {
        super(ctx);
        this.listener = l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_step);
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TabLayout tab = findViewById(R.id.tab_category);
        RecyclerView rv = findViewById(R.id.rv_step_types);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        TypeAdapter adapter = new TypeAdapter();
        rv.setAdapter(adapter);

        for (StepType.Category cat : StepType.Category.values()) {
            tab.addTab(tab.newTab().setText(cat.label));
        }
        showCategory(adapter, StepType.Category.ACTION);

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab t) {
                showCategory(adapter, StepType.Category.values()[t.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab t) {}

            @Override
            public void onTabReselected(TabLayout.Tab t) {}
        });
    }

    private void showCategory(TypeAdapter adapter, StepType.Category cat) {
        List<StepType> types = new ArrayList<>();
        for (StepType t : StepType.values()) {
            if (t.category == cat) types.add(t);
        }
        adapter.setData(types);
    }

    private class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.ViewHolder> {
        private List<StepType> data = new ArrayList<>();

        void setData(List<StepType> l) {
            data = l;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            StepType t = data.get(pos);
            h.t1.setText(t.label);
            h.t2.setText(t.description);
            h.itemView.setOnClickListener(v -> {
                listener.onSelected(t);
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView t1, t2;

            ViewHolder(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
