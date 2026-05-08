package com.superclicker.util;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ColorMatcher {
    public static int[] findColor(Bitmap screen, int target, int tolerance, int[] region) {
        if (screen == null) return null;
        int l = 0, t = 0, r = screen.getWidth(), b = screen.getHeight();
        if (region != null && region.length == 4) { l = region[0]; t = region[1]; r = region[2]; b = region[3]; }
        int tr = Color.red(target), tg = Color.green(target), tb = Color.blue(target);
        for (int y = t; y < b; y += 3) {
            for (int x = l; x < r; x += 3) {
                int px = screen.getPixel(x, y);
                if (Math.abs(Color.red(px) - tr) <= tolerance &&
                    Math.abs(Color.green(px) - tg) <= tolerance &&
                    Math.abs(Color.blue(px) - tb) <= tolerance) return new int[]{x, y};
            }
        }
        return null;
    }
}
