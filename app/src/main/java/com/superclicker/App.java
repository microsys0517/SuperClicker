package com.superclicker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class App extends Application {
    private static final String TAG = "SuperClickerApp";
    public static final String CHANNEL_ID = "superclicker_service";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "超级点击器", NotificationManager.IMPORTANCE_LOW);
                ch.setDescription("超级点击器后台服务");
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) nm.createNotificationChannel(ch);
            }
            Log.i(TAG, "App 启动成功");
        } catch (Exception e) {
            Log.e(TAG, "App 启动异常: " + e.getMessage());
        }
    }
}
