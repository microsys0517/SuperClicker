package com.superclicker.util;
import android.graphics.Bitmap;import android.graphics.BitmapFactory;
public class ImageMatcher{public static Bitmap load(String p){try{return BitmapFactory.decodeFile(p);}catch(Exception e){return null;}}
public static int[] find(Bitmap s,Bitmap t){return null;}}
