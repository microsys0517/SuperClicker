package com.superclicker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class App extends Application {

    private static final String TAG = "SuperClickerApp";
    public static final String CHANNEL_SERVICE = "service_channel";
    public static final String CHANNEL_ALERT = "alert_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createNotificationChannels();
            Log.i(TAG, "App 初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "App 初始化失败: " + e.getMessage(), e);
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) {
                Log.w(TAG, "NotificationManager 为 null");
                return;
            }

            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE,
                "后台服务",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("超级点击器后台运行");
            nm.createNotificationChannel(serviceChannel);

            NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ALERT,
                "提醒通知",
                NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("脚本执行提醒");
            nm.createNotificationChannel(alertChannel);

            Log.i(TAG, "通知渠道创建成功");
        }
    }
}
