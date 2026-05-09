package com.superclicker.engine;
import android.content.*;import com.google.gson.Gson;import com.superclicker.model.Step;
import com.superclicker.service.ClickAccessibilityService;import com.superclicker.util.Logger;
import java.util.Map;import java.util.concurrent.CountDownLatch;import java.util.concurrent.TimeUnit;
public class StepExecutor{private static final String TAG="SE";private final Context ctx;
public StepExecutor(Context c){ctx=c;}
public boolean click(ClickAccessibilityService sv,Step st){final CountDownLatch l=new CountDownLatch(1);
sv.performClick(st.x1,st.y1,new ClickAccessibilityService.GestureCallback(){public void onCompleted(){l.countDown();}public void onCancelled(){l.countDown();}});
await(l,5000);Logger.d(TAG,"click("+st.x1+","+st.y1+")");return true;}
public boolean swipe(ClickAccessibilityService sv,Step st){final CountDownLatch l=new CountDownLatch(1);
sv.performSwipe(st.x1,st.y1,st.x2,st.y2,st.duration,new ClickAccessibilityService.GestureCallback(){public void onCompleted(){l.countDown();}public void onCancelled(){l.countDown();}});
await(l,10000);return true;}
public boolean longPress(ClickAccessibilityService sv,Step st){final CountDownLatch l=new CountDownLatch(1);
sv.performLongPress(st.x1,st.y1,st.duration,new ClickAccessibilityService.GestureCallback(){public void onCompleted(){l.countDown();}public void onCancelled(){l.countDown();}});
await(l,st.duration+5000);return true;}
public boolean multiTouch(ClickAccessibilityService sv,Step st){if(st.multiPoints==null||st.multiPoints.isEmpty())return false;
try{int[][] pts=new Gson().fromJson(st.multiPoints,int[][].class);final CountDownLatch l=new CountDownLatch(1);
sv.performMultiTouch(pts,null,new ClickAccessibilityService.GestureCallback(){public void onCompleted(){l.countDown();}public void onCancelled(){l.countDown();}});
await(l,10000);return true;}catch(Exception e){return false;}}
public boolean imgMatch(ClickAccessibilityService sv,Step st){Logger.d(TAG,"imgMatch");return false;}
public boolean colorMatch(ClickAccessibilityService sv,Step st){Logger.d(TAG,"colorMatch");return false;}
public boolean textMatch(ClickAccessibilityService sv,Step st){Logger.d(TAG,"textMatch");return false;}
public boolean numCompare(Step st){try{return cmp(Double.parseDouble(st.compareValue1),Double.parseDouble(st.compareValue2),st.compareOperator);}catch(Exception e){return false;}}
public boolean txtCompare(Step st){String a=st.compareValue1==null?"":st.compareValue1,b=st.compareValue2==null?"":st.compareValue2;
if("==".equals(st.compareOperator))return a.equals(b);if("!=".equals(st.compareOperator))return !a.equals(b);
if("contains".equals(st.compareOperator))return a.contains(b);return false;}
public boolean timeCompare(Step st){try{return cmp(System.currentTimeMillis(),Long.parseLong(st.compareValue2),st.compareOperator);}catch(Exception e){return false;}}
public void counter(Step st,Map<String,Integer> m){String n=st.counterName!=null?st.counterName:"def";
int v=m.getOrDefault(n,0);String o=st.counterOp!=null?st.counterOp:"set";
if("set".equals(o))v=st.counterValue;else if("add".equals(o))v+=st.counterValue;else if("sub".equals(o))v-=st.counterValue;
m.put(n,v);}
public boolean inputText(Step st){if(st.inputText==null||st.inputText.isEmpty())return false;
try{ClipboardManager cm=(ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
cm.setPrimaryClip(ClipData.newPlainText("sc",st.inputText));return true;}catch(Exception e){return false;}}
private boolean cmp(double a,double b,String o){if(o==null)return false;
switch(o){case">":return a>b;case"<":return a<b;case"==":return Math.abs(a-b)<0.001;
case">=":return a>=b;case"<=":return a<=b;case"!=":return Math.abs(a-b)>=0.001;default:return false;}}
private void await(CountDownLatch l,long ms){try{l.await(ms,TimeUnit.MILLISECONDS);}catch(InterruptedException ignored){}}}
