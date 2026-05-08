package com.superclicker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class OcrHelper {
    public static int[] findText(Context context, Bitmap bitmap, String target) {
        if (bitmap == null || target == null || target.isEmpty()) return null;
        TextRecognizer rec = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<int[]> result = new AtomicReference<>(null);
        rec.process(image).addOnSuccessListener(text -> {
            for (com.google.mlkit.vision.text.Text.TextBlock block : text.getTextBlocks()) {
                for (com.google.mlkit.vision.text.Text.Line line : block.getLines()) {
                    if (line.getText().contains(target)) {
                        Rect bounds = line.getBoundingBox();
                        if (bounds != null) {
                            result.set(new int[]{(bounds.left + bounds.right) / 2, (bounds.top + bounds.bottom) / 2});
                            latch.countDown();
                            return;
                        }
                    }
                }
            }
            latch.countDown();
        }).addOnFailureListener(e -> latch.countDown());
        try { latch.await(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        rec.close();
        return result.get();
    }
}
