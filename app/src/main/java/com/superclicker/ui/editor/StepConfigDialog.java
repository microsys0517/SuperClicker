package com.superclicker.ui.editor;
import android.app.Dialog;import android.content.Context;import android.os.Bundle;import android.view.*;import android.widget.*;
import androidx.annotation.NonNull;import com.superclicker.R;import com.superclicker.model.Step;import com.superclicker.model.StepType;
public class StepConfigDialog extends Dialog{
public interface Lis{void onSave(Step step);}private final Step step;private final Lis lis;
public StepConfigDialog(@NonNull Context c,Step st,Lis l){super(c);step=st;lis=l;}
@Override protected void onCreate(Bundle s){super.onCreate(s);requestWindowFeature(Window.FEATURE_NO_TITLE);
setContentView(R.layout.dialog_step_config);if(getWindow()!=null)getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
((TextView)findViewById(R.id.tv_config_title)).setText("配置: "+step.type.label);
final EditText eL=findViewById(R.id.et_label),eX1=findViewById(R.id.et_x1),eY1=findViewById(R.id.et_y1),
eX2=findViewById(R.id.et_x2),eY2=findViewById(R.id.et_y2),eD=findViewById(R.id.et_duration),
eI=findViewById(R.id.et_image_path),eT=findViewById(R.id.et_match_text),eIn=findViewById(R.id.et_input_text),
eJ=findViewById(R.id.et_jump_target),eWb=findViewById(R.id.et_wait_before),eWa=findViewById(R.id.et_wait_after),
eRc=findViewById(R.id.et_retry_count),eRd=findViewById(R.id.et_retry_delay);
eL.setText(step.label!=null?step.label:"");eX1.setText(""+step.x1);eY1.setText(""+step.y1);
eX2.setText(""+step.x2);eY2.setText(""+step.y2);eD.setText(""+step.duration);
eWb.setText(""+step.waitBefore);eWa.setText(""+step.waitAfter);eRc.setText(""+step.retryCount);eRd.setText(""+step.retryDelay);
if(step.matchImagePath!=null)eI.setText(step.matchImagePath);if(step.matchText!=null)eT.setText(step.matchText);
if(step.inputText!=null)eIn.setText(step.inputText);eJ.setText(""+step.jumpTarget);
LinearLayout lc=findViewById(R.id.layout_coords),lc2=findViewById(R.id.layout_coords2),ld=findViewById(R.id.layout_duration),
li=findViewById(R.id.layout_image),lt=findViewById(R.id.layout_text_match),lin=findViewById(R.id.layout_input),lj=findViewById(R.id.layout_jump);
lc.setVisibility(View.GONE);lc2.setVisibility(View.GONE);ld.setVisibility(View.GONE);
li.setVisibility(View.GONE);lt.setVisibility(View.GONE);lin.setVisibility(View.GONE);lj.setVisibility(View.GONE);
switch(step.type){case CLICK:lc.setVisibility(View.VISIBLE);break;
case LONG_PRESS:lc.setVisibility(View.VISIBLE);ld.setVisibility(View.VISIBLE);break;
case SWIPE:lc.setVisibility(View.VISIBLE);lc2.setVisibility(View.VISIBLE);ld.setVisibility(View.VISIBLE);break;
case IMAGE_MATCH:li.setVisibility(View.VISIBLE);break;case TEXT_MATCH:lt.setVisibility(View.VISIBLE);break;
case INPUT_TEXT:lin.setVisibility(View.VISIBLE);break;case JUMP:lj.setVisibility(View.VISIBLE);break;default:break;}
findViewById(R.id.btn_save).setOnClickListener(v->{step.label=eL.getText().toString().trim();
step.x1=pi(eX1);step.y1=pi(eY1);step.x2=pi(eX2);step.y2=pi(eY2);step.duration=pl(eD);
step.waitBefore=pl(eWb);step.waitAfter=pl(eWa);step.retryCount=pi(eRc);step.retryDelay=pl(eRd);
step.matchImagePath=eI.getText().toString().trim();step.matchText=eT.getText().toString().trim();
step.inputText=eIn.getText().toString().trim();step.jumpTarget=pi(eJ);lis.onSave(step);dismiss();});
findViewById(R.id.btn_cancel).setOnClickListener(v->dismiss());}
private int pi(EditText e){try{return Integer.parseInt(e.getText().toString().trim());}catch(Exception x){return 0;}}
private long pl(EditText e){try{return Long.parseLong(e.getText().toString().trim());}catch(Exception x){return 0;}}}
