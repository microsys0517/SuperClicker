package com.superclicker.service;
import android.accessibilityservice.AccessibilityService;import android.accessibilityservice.GestureDescription;
import android.graphics.Path;import android.util.DisplayMetrics;import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
public class ClickAccessibilityService extends AccessibilityService{
private static final String TAG="A11y";private static ClickAccessibilityService inst;
public interface GestureCallback{void onCompleted();void onCancelled();}
@Override public void onServiceConnected(){super.onServiceConnected();inst=this;Log.i(TAG,"connected");}
public static ClickAccessibilityService getInstance(){return inst;}
public static boolean isRunning(){return inst!=null;}
@Override public void onAccessibilityEvent(AccessibilityEvent e){}
@Override public void onInterrupt(){}
@Override public void onDestroy(){super.onDestroy();inst=null;}
public void performClick(int x,int y,final GestureCallback cb){
Path p=new Path();p.moveTo(x,y);GestureDescription.Builder b=new GestureDescription.Builder();
b.addStroke(new GestureDescription.StrokeDescription(p,0,50));
dispatchGesture(b.build(),new GestureResultCallback(){
@Override public void onCompleted(GestureDescription d){if(cb!=null)cb.onCompleted();}
@Override public void onCancelled(GestureDescription d){if(cb!=null)cb.onCancelled();}},null);}
public void performLongPress(int x,int y,long dur,final GestureCallback cb){
Path p=new Path();p.moveTo(x,y);GestureDescription.Builder b=new GestureDescription.Builder();
b.addStroke(new GestureDescription.StrokeDescription(p,0,dur));
dispatchGesture(b.build(),new GestureResultCallback(){
@Override public void onCompleted(GestureDescription d){if(cb!=null)cb.onCompleted();}
@Override public void onCancelled(GestureDescription d){if(cb!=null)cb.onCancelled();}},null);}
public void performSwipe(int x1,int y1,int x2,int y2,long dur,final GestureCallback cb){
Path p=new Path();p.moveTo(x1,y1);p.lineTo(x2,y2);GestureDescription.Builder b=new GestureDescription.Builder();
b.addStroke(new GestureDescription.StrokeDescription(p,0,dur));
dispatchGesture(b.build(),new GestureResultCallback(){
@Override public void onCompleted(GestureDescription d){if(cb!=null)cb.onCompleted();}
@Override public void onCancelled(GestureDescription d){if(cb!=null)cb.onCancelled();}},null);}
public void performMultiTouch(int[][] pts,long[] delays,final GestureCallback cb){
if(pts==null||pts.length==0){if(cb!=null)cb.onCancelled();return;}
GestureDescription.Builder b=new GestureDescription.Builder();
for(int i=0;i<pts.length;i++){Path p=new Path();p.moveTo(pts[i][0],pts[i][1]);
long s=(delays!=null&&i<delays.length)?delays[i]:0;b.addStroke(new GestureDescription.StrokeDescription(p,s,100));}
dispatchGesture(b.build(),new GestureResultCallback(){
@Override public void onCompleted(GestureDescription d){if(cb!=null)cb.onCompleted();}
@Override public void onCancelled(GestureDescription d){if(cb!=null)cb.onCancelled();}},null);}
public void pressBack(){performGlobalAction(GLOBAL_ACTION_BACK);}
public void pressHome(){performGlobalAction(GLOBAL_ACTION_HOME);}
public void pressRecents(){performGlobalAction(GLOBAL_ACTION_RECENTS);}
public int[] getScreenSize(){DisplayMetrics dm=getResources().getDisplayMetrics();return new int[]{dm.widthPixels,dm.heightPixels};}}
