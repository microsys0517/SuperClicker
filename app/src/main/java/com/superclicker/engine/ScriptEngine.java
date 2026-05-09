package com.superclicker.engine;
import android.content.Context;import android.os.Handler;import android.os.Looper;import android.os.SystemClock;
import com.superclicker.model.*;import com.superclicker.service.ClickAccessibilityService;import com.superclicker.util.Logger;
import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;
public class ScriptEngine{
private static final String TAG="Engine";
public enum State{IDLE,RUNNING,PAUSED,STOPPING}
public interface StateListener{
void onStateChanged(State state);void onStepChanged(int cur,int total,Step step);
void onLoopChanged(int cur,int total);void onError(int idx,Step step,String err);void onComplete();}
private final Context ctx;private final Handler mh=new Handler(Looper.getMainLooper());
private final ExecutorService ex=Executors.newSingleThreadExecutor();private final StepExecutor se;
private Script script;private List<Step> steps;private ExecutionConfig cfg;private State state=State.IDLE;
private StateListener lis;private final AtomicBoolean paused=new AtomicBoolean(false);
private final AtomicBoolean stopped=new AtomicBoolean(false);private final AtomicInteger curLoop=new AtomicInteger(0);
private final Map<String,Integer> counters=new HashMap<>();
public ScriptEngine(Context c){ctx=c;se=new StepExecutor(c);}
public void setListener(StateListener l){lis=l;}public State getState(){return state;}
public void start(Script s,List<Step> st){
if(state==State.RUNNING)return;script=s;steps=st;cfg=s.config;counters.clear();curLoop.set(0);
stopped.set(false);paused.set(false);setState(State.RUNNING);
Logger.i(TAG,"开始: "+s.name+"("+st.size()+"步)");ex.execute(this::runLoop);}
public void stop(){stopped.set(true);paused.set(false);setState(State.STOPPING);Logger.i(TAG,"停止");}
public void togglePause(){
if(state==State.RUNNING){paused.set(true);setState(State.PAUSED);}
else if(state==State.PAUSED){paused.set(false);synchronized(paused){paused.notifyAll();}setState(State.RUNNING);}}
private void runLoop(){int tl=cfg.loopCount==0?Integer.MAX_VALUE:cfg.loopCount;
for(int lp=0;lp<tl&&!stopped.get();lp++){curLoop.set(lp+1);notifyLoop(lp+1,cfg.loopCount);
for(int i=cfg.startStep;i<steps.size()&&!stopped.get();i++){waitP();if(stopped.get())break;
Step st=steps.get(i);if(!st.enabled)continue;notifyStep(i,steps.size(),st);
if(st.waitBefore>0)sleep(st.waitBefore);boolean ok=execRetry(st);
if(st.type==StepType.JUMP&&ok){int t=st.jumpTarget;if(t>=0&&t<steps.size()){i=t-1;continue;}}
if(st.type==StepType.STOP){stopped.set(true);break;}
if(st.type==StepType.PAUSE){paused.set(true);setState(State.PAUSED);waitP();}
if(st.waitAfter>0)sleep(st.waitAfter);}if(stopped.get())break;
if(lp<tl-1&&cfg.loopInterval>0)sleep(cfg.loopInterval);}
setState(State.IDLE);Logger.i(TAG,"执行完成");mh.post(()->{if(lis!=null)lis.onComplete();});}
private boolean execRetry(Step st){int mr=Math.max(st.retryCount,1);
for(int a=0;a<mr&&!stopped.get();a++){if(a>0)sleep(st.retryDelay);
try{boolean r=exec(st);st.lastResult=r;if(r)return true;}catch(Exception e){Logger.e(TAG,"步"+st.order+": "+e.getMessage());}}return false;}
private boolean exec(Step st)throws Exception{ClickAccessibilityService sv=ClickAccessibilityService.getInstance();
if(sv==null)throw new Exception("无障碍未开启");
switch(st.type){case CLICK:return se.click(sv,st);case SWIPE:return se.swipe(sv,st);
case LONG_PRESS:return se.longPress(sv,st);case MULTI_TOUCH:return se.multiTouch(sv,st);
case IMAGE_MATCH:return se.imgMatch(sv,st);case COLOR_MATCH:return se.colorMatch(sv,st);
case TEXT_MATCH:return se.textMatch(sv,st);case NUMBER_COMPARE:return se.numCompare(st);
case TEXT_COMPARE:return se.txtCompare(st);case TIME_COMPARE:return se.timeCompare(st);
case COUNTER:se.counter(st,counters);return true;case INPUT_TEXT:return se.inputText(st);
case BACK_KEY:sv.pressBack();return true;case HOME_KEY:sv.pressHome();return true;
case MENU_KEY:sv.pressRecents();return true;
case JUMP:case STOP:case PAUSE:case EMPTY:case SUB_RULE:return true;default:return false;}}
private void waitP(){if(paused.get()&&!stopped.get())synchronized(paused){try{while(paused.get()&&!stopped.get())paused.wait();}catch(InterruptedException ignored){}}}
private void sleep(long ms){if(ms<=0)return;long e=SystemClock.elapsedRealtime()+ms;
while(SystemClock.elapsedRealtime()<e&&!stopped.get()){try{Thread.sleep(Math.min(e-SystemClock.elapsedRealtime(),100));}catch(InterruptedException x){if(stopped.get())break;}}}
private void setState(State s){state=s;mh.post(()->{if(lis!=null)lis.onStateChanged(s);});}
private void notifyStep(int c,int t,Step s){mh.post(()->{if(lis!=null)lis.onStepChanged(c,t,s);});}
private void notifyLoop(int c,int t){mh.post(()->{if(lis!=null)lis.onLoopChanged(c,t);});}}
