package com.superclicker.ui.log;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.superclicker.R;
import com.superclicker.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends Fragment implements Logger.LogListener {

    private LogAdapter adapter;
    private RecyclerView rv;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.fragment_log, c, false);
        rv = v.findViewById(R.id.rv_logs);
        v.findViewById(R.id.btn_clear_log).setOnClickListener(x -> {
            Logger.clear();
            adapter.setData(new ArrayList<>());
        });
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        adapter = new LogAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        adapter.setData(Logger.getLogs());
    }

    @Override public void onResume() { super.onResume(); Logger.addListener(this); }
    @Override public void onPause() { super.onPause(); Logger.removeListener(this); }
    @Override public void onNewLog(String msg) {
        if (adapter != null) { adapter.add(msg); rv.scrollToPosition(adapter.getItemCount() - 1); }
    }

    static class LogAdapter extends RecyclerView.Adapter<LogAdapter.VH> {
        private final List<String> data = new ArrayList<>();
        void setData(List<String> l) { data.clear(); if (l != null) data.addAll(l); notifyDataSetChanged(); }
        void add(String s) { data.add(s); if (data.size() > 5000) data.remove(0); notifyItemInserted(data.size() - 1); }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            TextView tv = new TextView(p.getContext());
            tv.setTextSize(11); tv.setPadding(4, 2, 4, 2); tv.setTypeface(Typeface.MONOSPACE);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            String line = data.get(pos);
            ((TextView) h.itemView).setText(line);
            ((TextView) h.itemView).setTextColor(
                line.contains("[E/") ? 0xFFF44336 : line.contains("[W/") ? 0xFFFF9800 : 0xFF333333);
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder { VH(View v) { super(v); } }
    }
}
