package com.superclicker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;

public class ScreenUtil {
    private static MediaProjection mediaProjection;
    public static void setMediaProjection(MediaProjection p) { mediaProjection = p; }
    public static Bitmap captureScreen(Context context) {
        Logger.d("ScreenUtil", "截屏（需要MediaProjection）");
        return null;
    }
}
