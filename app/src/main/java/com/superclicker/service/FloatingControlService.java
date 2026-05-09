package com.superclicker.service;
import android.app.Notification;import android.app.NotificationChannel;import android.app.NotificationManager;
import android.app.PendingIntent;import android.app.Service;import android.content.Intent;
import android.graphics.PixelFormat;import android.os.Build;import android.os.IBinder;import android.util.Log;
import android.view.Gravity;import android.view.LayoutInflater;import android.view.MotionEvent;import android.view.View;
import android.view.WindowManager;import android.widget.ImageButton;import android.widget.TextView;
import com.superclicker.MainActivity;import com.superclicker.R;
import com.superclicker.engine.ScriptEngine;import com.superclicker.model.Step;
public class FloatingControlService extends Service{
private static final String TAG="FloatSvc";private static FloatingControlService inst;
private WindowManager wm;private View fv;private WindowManager.LayoutParams lp;
private TextView tvS,tvSt,tvL;private ScriptEngine engine;private boolean hidden=false;
public static FloatingControlService getInstance(){return inst;}
@Override public void onCreate(){super.onCreate();inst=this;try{startFg();wm=(WindowManager)getSystemService(WINDOW_SERVICE);createFv();}catch(Exception e){Log.e(TAG,"onCreate: "+e.getMessage());}}
private void startFg(){try{
if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){NotificationManager nm=getSystemService(NotificationManager.class);if(nm!=null&&nm.getNotificationChannel("service_channel")==null)nm.createNotificationChannel(new NotificationChannel("service_channel","后台",NotificationManager.IMPORTANCE_LOW));}
Intent i=new Intent(this,MainActivity.class);int f=PendingIntent.FLAG_UPDATE_CURRENT;
if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)f|=PendingIntent.FLAG_IMMUTABLE;
PendingIntent pi=PendingIntent.getActivity(this,0,i,f);
Notification.Builder nb=Build.VERSION.SDK_INT>=Build.VERSION_CODES.O?new Notification.Builder(this,"service_channel"):new Notification.Builder(this);
startForeground(1001,nb.setContentTitle("超级点击器").setContentText("运行中").setSmallIcon(android.R.drawable.ic_media_play).setContentIntent(pi).build());
}catch(Exception e){Log.e(TAG,"startFg: "+e.getMessage());}}
private void createFv(){try{
fv=LayoutInflater.from(this).inflate(R.layout.floating_control,null);
lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
lp.gravity=Gravity.TOP|Gravity.START;lp.x=20;lp.y=500;
wm.addView(fv,lp);
tvS=fv.findViewById(R.id.tv_floating_status);tvSt=fv.findViewById(R.id.tv_floating_step);tvL=fv.findViewById(R.id.tv_floating_loop);
ImageButton bp=fv.findViewById(R.id.btn_floating_play);ImageButton bpa=fv.findViewById(R.id.btn_floating_pause);
ImageButton bs=fv.findViewById(R.id.btn_floating_stop);ImageButton bh=fv.findViewById(R.id.btn_floating_hide);
ImageButton bc=fv.findViewById(R.id.btn_floating_close);
if(bp!=null)bp.setOnClickListener(v->{try{Intent i=new Intent(this,MainActivity.class);i.setAction("PLAY");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception e){Log.e(TAG,"play:"+e.getMessage());}});
if(bpa!=null)bpa.setOnClickListener(v->{try{if(engine!=null)engine.togglePause();}catch(Exception e){}});
if(bs!=null)bs.setOnClickListener(v->{try{if(engine!=null)engine.stop();}catch(Exception e){}});
if(bh!=null)bh.setOnClickListener(v->{try{int vis=hidden?View.VISIBLE:View.GONE;View c=fv.findViewById(R.id.layout_controls);View info=fv.findViewById(R.id.layout_info);if(c!=null)c.setVisibility(vis);if(info!=null)info.setVisibility(vis);hidden=!hidden;}catch(Exception e){}});
if(bc!=null)bc.setOnClickListener(v->stopSelf());
fv.setOnTouchListener(new View.OnTouchListener(){int ix,iy;float tx,ty;boolean dg;
public boolean onTouch(View v,MotionEvent ev){try{switch(ev.getAction()){
case MotionEvent.ACTION_DOWN:ix=lp.x;iy=lp.y;tx=ev.getRawX();ty=ev.getRawY();dg=false;return true;
case MotionEvent.ACTION_MOVE:float dx=ev.getRawX()-tx;float dy=ev.getRawY()-ty;if(Math.abs(dx)>10||Math.abs(dy)>10)dg=true;lp.x=ix+(int)dx;lp.y=iy+(int)dy;wm.updateViewLayout(fv,lp);return true;
case MotionEvent.ACTION_UP:return dg;}}catch(Exception e){}return false;}});
Log.i(TAG,"悬浮窗创建成功");}catch(Exception e){Log.e(TAG,"createFv: "+e.getMessage());}}
public void bindEngine(ScriptEngine eng){this.engine=eng;
eng.setListener(new ScriptEngine.StateListener(){
public void onStateChanged(ScriptEngine.State state){if(tvS==null)return;
switch(state){case IDLE:tvS.setText("就绪");break;case RUNNING:tvS.setText("运行中");break;
case PAUSED:tvS.setText("已暂停");break;case STOPPING:tvS.setText("停止中");break;}}
public void onStepChanged(int c,int t,Step s){if(tvSt!=null)tvSt.setText("步骤"+(c+1)+"/"+t);}
public void onLoopChanged(int c,int t){if(tvL!=null)tvL.setText("第"+c+"轮");}
public void onError(int i,Step s,String e){if(tvS!=null)tvS.setText("错误");}
public void onComplete(){if(tvS!=null)tvS.setText("已完成");}});}
@Override public int onStartCommand(Intent i,int f,int s){return START_STICKY;}
@Override public void onDestroy(){super.onDestroy();inst=null;try{if(fv!=null&&wm!=null)wm.removeView(fv);}catch(Exception e){}}
@Override public IBinder onBind(Intent i){return null;}}
