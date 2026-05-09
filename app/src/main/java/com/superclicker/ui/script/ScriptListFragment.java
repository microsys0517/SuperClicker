package com.superclicker.ui.script;
import android.os.Bundle;import android.view.*;import android.widget.*;import androidx.annotation.*;import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;import androidx.recyclerview.widget.*;import com.superclicker.*;import com.superclicker.db.AppDatabase;
import com.superclicker.model.Script;import java.text.SimpleDateFormat;import java.util.*;import java.util.Locale;
public class ScriptListFragment extends Fragment{
private RecyclerView rv;private TextView tvE;private SA ad;private AppDatabase db;
@Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle s){
View v=i.inflate(R.layout.fragment_scripts,c,false);rv=v.findViewById(R.id.rv_scripts);tvE=v.findViewById(R.id.tv_empty);
v.findViewById(R.id.btn_add_script).setOnClickListener(x->create());return v;}
@Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){super.onViewCreated(v,s);
db=AppDatabase.getInstance(requireContext());ad=new SA();rv.setLayoutManager(new LinearLayoutManager(requireContext()));rv.setAdapter(ad);
db.scriptDao().getAll().observe(getViewLifecycleOwner(),list->{ad.set(list);
tvE.setVisibility(list==null||list.isEmpty()?View.VISIBLE:View.GONE);
rv.setVisibility(list==null||list.isEmpty()?View.GONE:View.VISIBLE);});}
private void create(){EditText et=new EditText(requireContext());et.setHint("脚本名称");et.setPadding(60,40,60,20);
new AlertDialog.Builder(requireContext()).setTitle("新建脚本").setView(et)
.setPositiveButton("创建",(d,w)->{String n=et.getText().toString().trim();if(n.isEmpty())n="未命名脚本";
Script sc=new Script();sc.name=n;long id=db.scriptDao().insert(sc);sc.id=id;open(sc);}).setNegativeButton("取消",null).show();}
private void open(Script sc){if(getActivity()instanceof MainActivity){((MainActivity)getActivity()).getEditorFragment().loadScript(sc);((MainActivity)getActivity()).switchToEditor();}}
private class SA extends RecyclerView.Adapter<SA.VH>{private List<Script> d=new ArrayList<>();
void set(List<Script> l){d=l!=null?l:new ArrayList<>();notifyDataSetChanged();}
@NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int t){return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_script,p,false));}
@Override public void onBindViewHolder(@NonNull VH h,int p){Script s=d.get(p);h.n.setText(s.name);h.ds.setText("点击进入编辑步骤");
h.inf.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date(s.updateTime)));
h.itemView.setOnClickListener(x->open(s));h.del.setOnClickListener(x->new AlertDialog.Builder(requireContext()).setTitle("删除").setMessage("删除「"+s.name+"」？").setPositiveButton("删除",(dd,ww)->db.scriptDao().delete(s)).setNegativeButton("取消",null).show());}
@Override public int getItemCount(){return d.size();}
class VH extends RecyclerView.ViewHolder{TextView n,ds,inf;ImageButton del;
VH(View v){super(v);n=v.findViewById(R.id.tv_script_name);ds=v.findViewById(R.id.tv_script_desc);inf=v.findViewById(R.id.tv_script_info);del=v.findViewById(R.id.btn_delete_script);}}}}
