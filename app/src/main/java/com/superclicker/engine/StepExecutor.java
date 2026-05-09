package com.superclicker.engine;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import com.google.gson.Gson;
import com.superclicker.model.Step;
import com.superclicker.service.ClickAccessibilityService;
import com.superclicker.util.Logger;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
public class StepExecutor {
    private static final String TAG = "StepExec";
    private final Context context;
    public StepExecutor(Context context) { this.context = context; }
    public boolean executeClick(ClickAccessibilityService svc, Step step) {
        final CountDownLatch latch = new CountDownLatch(1);
        svc.performClick(step.x1, step.y1, new ClickAccessibilityService.GestureCallback() {
            public void onCompleted() { latch.countDown(); }
            public void onCancelled() { latch.countDown(); }
        });
        await(latch, 5000);
        return true;
    }
    public boolean executeSwipe(ClickAccessibilityService svc, Step step) {
        final CountDownLatch latch = new CountDownLatch(1);
        svc.performSwipe(step.x1, step.y1, step.x2, step.y2, step.duration, new ClickAccessibilityService.GestureCallback() {
            public void onCompleted() { latch.countDown(); }
            public void onCancelled() { latch.countDown(); }
        });
        await(latch, 10000);
        return true;
    }
    public boolean executeLongPress(ClickAccessibilityService svc, Step step) {
        final CountDownLatch latch = new CountDownLatch(1);
        svc.performLongPress(step.x1, step.y1, step.duration, new ClickAccessibilityService.GestureCallback() {
            public void onCompleted() { latch.countDown(); }
            public void onCancelled() { latch.countDown(); }
        });
        await(latch, step.duration + 5000);
        return true;
    }
    public boolean executeMultiTouch(ClickAccessibilityService svc, Step step) {
        if (step.multiPoints == null || step.multiPoints.isEmpty()) return false;
        try {
            int[][] points = new Gson().fromJson(step.multiPoints, int[][].class);
            final CountDownLatch latch = new CountDownLatch(1);
            svc.performMultiTouch(points, null, new ClickAccessibilityService.GestureCallback() {
                public void onCompleted() { latch.countDown(); }
                public void onCancelled() { latch.countDown(); }
            });
            await(latch, 10000);
            return true;
        } catch (Exception e) { return false; }
    }
    public boolean executeImageMatch(ClickAccessibilityService svc, Step step) {
        Logger.d(TAG, "识图: " + (step.matchImagePath != null ? step.matchImagePath : "未设置"));
        return false;
    }
    public boolean executeColorMatch(ClickAccessibilityService svc, Step step) {
        Logger.d(TAG, "识色: #" + Integer.toHexString(step.matchColor));
        return false;
    }
    public boolean executeTextMatch(ClickAccessibilityService svc, Step step) {
        Logger.d(TAG, "识字: " + (step.matchText != null ? step.matchText : ""));
        return false;
    }
    public boolean executeNumberCompare(Step step) {
        try { return compare(Double.parseDouble(step.compareValue1), Double.parseDouble(step.compareValue2), step.compareOperator); }
        catch (Exception e) { return false; }
    }
    public boolean executeTextCompare(Step step) {
        String v1 = step.compareValue1 == null ? "" : step.compareValue1;
        String v2 = step.compareValue2 == null ? "" : step.compareValue2;
        if ("==".equals(step.compareOperator)) return v1.equals(v2);
        if ("!=".equals(step.compareOperator)) return !v1.equals(v2);
        if ("contains".equals(step.compareOperator)) return v1.contains(v2);
        return false;
    }
    public boolean executeTimeCompare(Step step) {
        try { return compare(System.currentTimeMillis(), Long.parseLong(step.compareValue2), step.compareOperator); }
        catch (Exception e) { return false; }
    }
    public void executeCounter(Step step, Map<String, Integer> counters) {
        String name = step.counterName != null ? step.counterName : "default";
        int current = counters.getOrDefault(name, 0);
        String op = step.counterOp != null ? step.counterOp : "set";
        if ("set".equals(op)) current = step.counterValue;
        else if ("add".equals(op)) current += step.counterValue;
        else if ("sub".equals(op)) current -= step.counterValue;
        counters.put(name, current);
        Logger.d(TAG, "计数器[" + name + "] = " + current);
    }
    public boolean executeInputText(Step step) {
        if (step.inputText == null || step.inputText.isEmpty()) return false;
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("sc", step.inputText));
            return true;
        } catch (Exception e) { return false; }
    }
    private boolean compare(double v1, double v2, String op) {
        if (op == null) return false;
        switch (op) {
            case ">": return v1 > v2;
            case "<": return v1 < v2;
            case "==": return Math.abs(v1 - v2) < 0.001;
            case ">=": return v1 >= v2;
            case "<=": return v1 <= v2;
            case "!=": return Math.abs(v1 - v2) >= 0.001;
            default: return false;
        }
    }
    private void await(CountDownLatch latch, long ms) { try { latch.await(ms, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {} }
}
