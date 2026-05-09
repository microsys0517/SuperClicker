package com.superclicker.util;
import android.content.Context;import android.net.Uri;import com.google.gson.Gson;
import com.superclicker.model.*;import java.io.*;import java.util.*;
public class FileUtils{private static final Gson g=new Gson();
public static String export(Script s,List<Step> st){Map<String,Object> m=new HashMap<>();m.put("script",s);m.put("steps",st);return g.toJson(m);}}
