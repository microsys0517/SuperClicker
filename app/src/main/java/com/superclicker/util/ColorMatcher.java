package com.superclicker.util;
import android.graphics.Bitmap;
import android.graphics.Color;
public class ColorMatcher {
    public static int[] findColor(Bitmap screen, int target, int tolerance, int[] region) {
        if (screen == null) return null;
        int left = 0, top = 0, right = screen.getWidth(), bottom = screen.getHeight();
        if (region != null && region.length == 4) { left = region[0]; top = region[1]; right = region[2]; bottom = region[3]; }
        int tr = Color.red(target), tg = Color.green(target), tb = Color.blue(target);
        for (int y = top; y < bottom; y += 3) {
            for (int x = left; x < right; x += 3) {
                int px = screen.getPixel(x, y);
                if (Math.abs(Color.red(px) - tr) <= tolerance && Math.abs(Color.green(px) - tg) <= tolerance && Math.abs(Color.blue(px) - tb) <= tolerance) return new int[]{x, y};
            }
        }
        return null;
    }
}
