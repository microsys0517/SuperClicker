package com.superclicker.ui.editor;
import android.os.Bundle;import android.view.*;import android.widget.*;import androidx.annotation.*;import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;import androidx.recyclerview.widget.*;import com.superclicker.R;import com.superclicker.db.AppDatabase;
import com.superclicker.engine.ScriptEngine;import com.superclicker.service.FloatingControlService;
import com.superclicker.model.*;import java.util.*;
public class StepEditorFragment extends Fragment{
private RecyclerView rv;private TextView tvN,tvC;private SA ad;private AppDatabase db;private Script cur;
@Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle s){
View v=i.inflate(R.layout.fragment_step_editor,c,false);rv=v.findViewById(R.id.rv_steps);tvN=v.findViewById(R.id.tv_script_name);tvC=v.findViewById(R.id.tv_step_count);
v.findViewById(R.id.btn_back).setOnClickListener(x->{if(getActivity()!=null)getActivity().getSupportFragmentManager().popBackStack();});
v.findViewById(R.id.btn_add_step).setOnClickListener(x->addStep());
v.findViewById(R.id.btn_record).setOnClickListener(x->Toast.makeText(requireContext(),"请手动添加步骤",Toast.LENGTH_LONG).show());
v.findViewById(R.id.btn_delete_step).setOnClickListener(x->delSel());
v.findViewById(R.id.btn_play).setOnClickListener(x->exec());
v.findViewById(R.id.btn_config).setOnClickListener(x->cfg());return v;}
@Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){super.onViewCreated(v,s);
db=AppDatabase.getInstance(requireContext());ad=new SA(step->cfgStep(step));
rv.setLayoutManager(new LinearLayoutManager(requireContext()));rv.setAdapter(ad);}
public void loadScript(Script sc){cur=sc;if(tvN!=null)tvN.setText(sc.name);refresh();}
private void refresh(){if(cur==null||db==null)return;List<Step> ls=db.stepDao().getByScriptIdSync(cur.id);ad.set(ls);tvC.setText(ls.size()+"步");}
private void addStep(){if(cur==null){Toast.makeText(requireContext(),"请先选择脚本",Toast.LENGTH_SHORT).show();return;}
new AddStepDialog(requireContext(),type->{Step st=new Step();st.scriptId=cur.id;st.type=type;st.label=type.label;
st.order=db.stepDao().getMaxOrder(cur.id)+1;long id=db.stepDao().insert(st);st.id=id;refresh();cfgStep(st);}).show();}
private void cfgStep(Step st){new StepConfigDialog(requireContext(),st,upd->{db.stepDao().update(upd);refresh();}).show();}
private void cfg(){if(cur==null)return;String[] items={"无限循环","循环1次","循环3次","循环5次","循环10次","间隔1秒","间隔3秒","间隔5秒"};
new AlertDialog.Builder(requireContext()).setTitle("脚本设置").setItems(items,(d,w)->{
switch(w){case 0:cur.config.loopCount=0;break;case 1:cur.config.loopCount=1;break;case 2:cur.config.loopCount=3;break;
case 3:cur.config.loopCount=5;break;case 4:cur.config.loopCount=10;break;case 5:cur.config.loopInterval=1000;break;
case 6:cur.config.loopInterval=3000;break;case 7:cur.config.loopInterval=5000;break;}
db.scriptDao().update(cur);Toast.makeText(requireContext(),"已保存",Toast.LENGTH_SHORT).show();}).show();}
private void delSel(){List<Step> sel=ad.getSelected();if(sel.isEmpty()){Toast.makeText(requireContext(),"长按选择步骤后删除",Toast.LENGTH_SHORT).show();return;}
new AlertDialog.Builder(requireContext()).setTitle("删除").setMessage("删除"+sel.size()+"个步骤？").setPositiveButton("删除",(d,w)->{
List<Long> ids=new ArrayList<>();for(Step s:sel)ids.add(s.id);db.stepDao().deleteByIds(ids);ad.clearSel();refresh();}).setNegativeButton("取消",null).show();}
private void exec(){if(cur==null){Toast.makeText(requireContext(),"没有脚本",Toast.LENGTH_SHORT).show();return;}
List<Step> all=db.stepDao().getByScriptIdSync(cur.id);List<Step> en=new ArrayList<>();for(Step s:all)if(s.enabled)en.add(s);
if(en.isEmpty()){Toast.makeText(requireContext(),"没有启用的步骤",Toast.LENGTH_SHORT).show();return;}
ScriptEngine eng=new ScriptEngine(requireContext());FloatingControlService fcs=FloatingControlService.getInstance();
if(fcs!=null)fcs.bindEngine(eng);eng.start(cur,en);Toast.makeText(requireContext(),"开始执行("+en.size()+"步)",Toast.LENGTH_SHORT).show();}
private class SA extends RecyclerView.Adapter<SA.VH>{private List<Step> d=new ArrayList<>();private final Set<Long> sel=new HashSet<>();private boolean sm=false;
private final StepAdapter.OnStepClickListener lis;SA(StepAdapter.OnStepClickListener l){lis=l;}
void set(List<Step> l){d=l!=null?l:new ArrayList<>();notifyDataSetChanged();}
List<Step> getSelected(){List<Step> r=new ArrayList<>();for(Step s:d)if(sel.contains(s.id))r.add(s);return r;}
void clearSel(){sel.clear();sm=false;notifyDataSetChanged();}
@NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int t){return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_step,p,false));}
@Override public void onBindViewHolder(@NonNull VH h,int p){Step st=d.get(p);
h.tvN.setText(String.valueOf(st.order));h.tvT.setText("["+st.type.label+"]");h.tvT.setTextColor(getC(st.type.category));
h.tvD.setText(getD(st));
StringBuilder w=new StringBuilder();if(st.waitBefore>0)w.append("前").append(st.waitBefore).append("ms ");
if(st.waitAfter>0)w.append("后").append(st.waitAfter).append("ms ");if(st.retryCount>1)w.append("重试").append(st.retryCount).append("次");
h.tvW.setText(w.toString().trim());
h.sw.setOnCheckedChangeListener(null);h.sw.setChecked(st.enabled);h.sw.setOnCheckedChangeListener((b,c)->st.enabled=c);
h.cb.setVisibility(sm?View.VISIBLE:View.GONE);h.cb.setOnCheckedChangeListener(null);h.cb.setChecked(sel.contains(st.id));
h.cb.setOnCheckedChangeListener((b,c)->{if(c)sel.add(st.id);else sel.remove(st.id);});
h.itemView.setOnClickListener(v->{if(sm){if(sel.contains(st.id))sel.remove(st.id);else sel.add(st.id);notifyItemChanged(p);}else lis.onClick(st);});
h.itemView.setOnLongClickListener(v->{sm=true;sel.add(st.id);notifyDataSetChanged();return true;});}
@Override public int getItemCount(){return d.size();}
private String getD(Step s){switch(s.type){case CLICK:return "("+s.x1+","+s.y1+")";
case SWIPE:return "("+s.x1+","+s.y1+")->("+s.x2+","+s.y2+")";case LONG_PRESS:return "("+s.x1+","+s.y1+") "+s.duration+"ms";
case JUMP:return "→步骤"+s.jumpTarget;case INPUT_TEXT:return s.inputText!=null?s.inputText:"";
case TEXT_MATCH:return "查找:"+(s.matchText!=null?s.matchText:"");default:return s.type.description;}}
private int getC(StepType.Category c){switch(c){case ACTION:return 0xFF1976D2;case RECOGNIZE:return 0xFF388E3C;
case CONDITION:return 0xFFF57C00;case TOOL:return 0xFF7B1FA2;case CONTROL:return 0xFFD32F2F;default:return 0xFF757575;}}
class VH extends RecyclerView.ViewHolder{TextView tvN,tvT,tvD,tvW;CheckBox cb;Switch sw;
VH(View v){super(v);tvN=v.findViewById(R.id.tv_step_number);tvT=v.findViewById(R.id.tv_step_type);
tvD=v.findViewById(R.id.tv_step_detail);tvW=v.findViewById(R.id.tv_step_wait);cb=v.findViewById(R.id.cb_select);sw=v.findViewById(R.id.switch_enabled);}}}}
