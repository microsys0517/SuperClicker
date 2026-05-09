package com.superclicker.util;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
public class ImageMatcher {
    public static Bitmap loadBitmap(String path) {
        try { return BitmapFactory.decodeFile(path); } catch (Exception e) { return null; }
    }
    public static int[] findTemplate(Bitmap screen, Bitmap template) {
        if (screen == null || template == null) return null;
        int sw = screen.getWidth(), sh = screen.getHeight();
        int tw = template.getWidth(), th = template.getHeight();
        if (tw > sw || th > sh) return null;
        int[] sp = new int[sw * sh];
        int[] tp = new int[tw * th];
        screen.getPixels(sp, 0, sw, 0, 0, sw, sh);
        template.getPixels(tp, 0, tw, 0, 0, tw, th);
        double best = 0; int bx = -1, by = -1;
        for (int y = 0; y <= sh - th; y += 3) {
            for (int x = 0; x <= sw - tw; x += 3) {
                double score = ncc(sp, sw, x, y, tp, tw, th);
                if (score > best) { best = score; bx = x; by = y; }
            }
        }
        return best >= 0.85 ? new int[]{bx, by} : null;
    }
    private static double ncc(int[] s, int sw, int sx, int sy, int[] t, int tw, int th) {
        long ss = 0, st = 0, tt = 0; int n = tw * th;
        for (int ty = 0; ty < th; ty++) {
            for (int tx = 0; tx < tw; tx++) {
                int sp = s[(sy + ty) * sw + (sx + tx)];
                int tp = t[ty * tw + tx];
                int sg = ((sp >> 16) & 0xFF) * 77 + ((sp >> 8) & 0xFF) * 150 + (sp & 0xFF) * 29 >> 8;
                int tg = ((tp >> 16) & 0xFF) * 77 + ((tp >> 8) & 0xFF) * 150 + (tp & 0xFF) * 29 >> 8;
                ss += sg; st += (long) sg * tg; tt += tg;
            }
        }
        double ms = (double) ss / n, mt = (double) tt / n, vs = 0, vt = 0, cov = 0;
        for (int ty = 0; ty < th; ty++) {
            for (int tx = 0; tx < tw; tx++) {
                int sp = s[(sy + ty) * sw + (sx + tx)];
                int tp = t[ty * tw + tx];
                int sg = ((sp >> 16) & 0xFF) * 77 + ((sp >> 8) & 0xFF) * 150 + (sp & 0xFF) * 29 >> 8;
                int tg = ((tp >> 16) & 0xFF) * 77 + ((tp >> 8) & 0xFF) * 150 + (tp & 0xFF) * 29 >> 8;
                vs += (sg - ms) * (sg - ms); vt += (tg - mt) * (tg - mt); cov += (sg - ms) * (tg - mt);
            }
        }
        double denom = Math.sqrt(vs * vt);
        return denom < 1 ? 0 : cov / denom;
    }
}
