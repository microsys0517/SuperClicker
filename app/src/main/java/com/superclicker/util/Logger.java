package com.superclicker.util;
import android.os.Handler;import android.os.Looper;
import java.text.SimpleDateFormat;import java.util.*;import java.util.Locale;
public class Logger{public interface LogListener{void onNewLog(String msg);}
private static final List<String> logs=Collections.synchronizedList(new ArrayList<>());
private static final List<LogListener> lis=Collections.synchronizedList(new ArrayList<>());
private static final SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss.SSS",Locale.getDefault());
private static final Handler h=new Handler(Looper.getMainLooper());
public static void addListener(LogListener l){lis.add(l);}
public static void removeListener(LogListener l){lis.remove(l);}
public static void d(String t,String m){log("D",t,m);}
public static void i(String t,String m){log("I",t,m);}
public static void w(String t,String m){log("W",t,m);}
public static void e(String t,String m){log("E",t,m);}
private static void log(String lv,String t,String m){final String line=sdf.format(new Date())+" ["+lv+"/"+t+"] "+m;
logs.add(line);while(logs.size()>5000)logs.remove(0);
h.post(()->{for(LogListener l:lis)l.onNewLog(line);});}
public static List<String> getLogs(){return new ArrayList<>(logs);}
public static void clear(){logs.clear();}}
