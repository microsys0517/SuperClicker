package com.superclicker.ui.editor;
import android.app.Dialog;import android.content.Context;import android.os.Bundle;import android.view.*;import android.widget.*;
import androidx.annotation.NonNull;import androidx.recyclerview.widget.*;import com.google.android.material.tabs.TabLayout;
import com.superclicker.R;import com.superclicker.model.StepType;import java.util.*;
public class AddStepDialog extends Dialog{
public interface Lis{void onSelected(StepType type);}private final Lis lis;
public AddStepDialog(@NonNull Context c,Lis l){super(c);lis=l;}
@Override protected void onCreate(Bundle s){super.onCreate(s);requestWindowFeature(Window.FEATURE_NO_TITLE);
setContentView(R.layout.dialog_add_step);if(getWindow()!=null)getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
TabLayout tab=findViewById(R.id.tab_category);RecyclerView rv=findViewById(R.id.rv_step_types);
rv.setLayoutManager(new LinearLayoutManager(getContext()));TA ad=new TA();rv.setAdapter(ad);
for(StepType.Category c:StepType.Category.values())tab.addTab(tab.newTab().setText(c.label));
show(ad,StepType.Category.ACTION);tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
public void onTabSelected(TabLayout.Tab t){show(ad,StepType.Category.values()[t.getPosition()]);}
public void onTabUnselected(TabLayout.Tab t){}public void onTabReselected(TabLayout.Tab t){}});}
private void show(TA ad,StepType.Category cat){List<StepType> ts=new ArrayList<>();
for(StepType t:StepType.values())if(t.category==cat)ts.add(t);ad.set(ts);}
private class TA extends RecyclerView.Adapter<TA.VH>{private List<StepType> d=new ArrayList<>();
void set(List<StepType> l){d=l;notifyDataSetChanged();}
@NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int t){return new VH(LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2,p,false));}
@Override public void onBindViewHolder(@NonNull VH h,int p){StepType t=d.get(p);h.t1.setText(t.label);h.t2.setText(t.description);
h.itemView.setOnClickListener(v->{lis.onSelected(t);dismiss();});}
@Override public int getItemCount(){return d.size();}
class VH extends RecyclerView.ViewHolder{TextView t1,t2;VH(View v){super(v);t1=v.findViewById(android.R.id.text1);t2=v.findViewById(android.R.id.text2);}}}}
