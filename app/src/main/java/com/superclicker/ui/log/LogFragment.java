package com.superclicker.ui.log;
import android.graphics.Typeface;import android.os.Bundle;import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;import androidx.recyclerview.widget.*;
import com.superclicker.R;import com.superclicker.util.Logger;import java.util.*;
public class LogFragment extends Fragment implements Logger.LogListener{
private LA ad;private RecyclerView rv;
@Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle s){
View v=i.inflate(R.layout.fragment_log,c,false);rv=v.findViewById(R.id.rv_logs);
v.findViewById(R.id.btn_clear_log).setOnClickListener(x->{Logger.clear();ad.set(new ArrayList<String>());});return v;}
@Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){super.onViewCreated(v,s);
ad=new LA();rv.setLayoutManager(new LinearLayoutManager(requireContext()));rv.setAdapter(ad);ad.set(Logger.getLogs());}
@Override public void onResume(){super.onResume();Logger.addListener(this);}
@Override public void onPause(){super.onPause();Logger.removeListener(this);}
@Override public void onNewLog(String m){if(ad!=null){ad.ap(m);rv.scrollToPosition(ad.getItemCount()-1);}}
static class LA extends RecyclerView.Adapter<LA.VH>{private final List<String> d=new ArrayList<>();
void set(List<String> l){d.clear();if(l!=null)d.addAll(l);notifyDataSetChanged();}
void ap(String s){d.add(s);while(d.size()>5000)d.remove(0);notifyItemInserted(d.size()-1);}
@NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int t){TextView tv=new TextView(p.getContext());tv.setTextSize(10);tv.setPadding(4,2,4,2);tv.setTypeface(Typeface.MONOSPACE);return new VH(tv);}
@Override public void onBindViewHolder(@NonNull VH h,int p){String l=d.get(p);((TextView)h.itemView).setText(l);
((TextView)h.itemView).setTextColor(l.contains("[E/")?0xFFF44336:l.contains("[W/")?0xFFFF9800:0xFF333333);}
@Override public int getItemCount(){return d.size();}
static class VH extends RecyclerView.ViewHolder{VH(View v){super(v);}}}}
