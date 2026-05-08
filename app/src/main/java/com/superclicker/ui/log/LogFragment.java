package com.superclicker.ui.log;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.superclicker.R;
import com.superclicker.util.Logger;
import java.util.*;

public class LogFragment extends Fragment implements Logger.LogListener {
    private LogAdapter adapter;
    private RecyclerView rvLogs;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        rvLogs = view.findViewById(R.id.rv_logs);
        view.findViewById(R.id.btn_clear_log).setOnClickListener(v -> { Logger.clear(); adapter.setData(new ArrayList<>()); });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new LogAdapter();
        rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLogs.setAdapter(adapter);
        adapter.setData(Logger.getLogs());
    }

    @Override public void onResume() { super.onResume(); Logger.addListener(this); }
    @Override public void onPause() { super.onPause(); Logger.removeListener(this); }

    @Override
    public void onNewLog(String msg) {
        if (adapter != null) {
            adapter.appendLog(msg);
            rvLogs.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    static class LogAdapter extends RecyclerView.Adapter<LogAdapter.VH> {
        private final List<String> data = new ArrayList<>();
        void setData(List<String> l) { data.clear(); data.addAll(l); notifyDataSetChanged(); }
        void appendLog(String s) { data.add(s); if (data.size() > 5000) data.remove(0); notifyItemInserted(data.size() - 1); }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
            TextView tv = new TextView(p.getContext());
            tv.setTextSize(11); tv.setPadding(4, 2, 4, 2); tv.setTypeface(Typeface.MONOSPACE);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            String line = data.get(pos);
            ((TextView) h.itemView).setText(line);
            ((TextView) h.itemView).setTextColor(line.contains("[E/") ? 0xFFF44336 : line.contains("[W/") ? 0xFFFF9800 : 0xFF333333);
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder { VH(View v) { super(v); } }
    }
}
