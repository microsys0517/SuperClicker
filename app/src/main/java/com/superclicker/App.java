package com.superclicker;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
public class App extends Application {
    private static final String TAG = "SCApp";
    public static final String CHANNEL_SERVICE = "service_channel";
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) {
                    nm.createNotificationChannel(new NotificationChannel(CHANNEL_SERVICE, "后台服务", NotificationManager.IMPORTANCE_LOW));
                }
            }
        } catch (Exception e) { Log.e(TAG, "init error: " + e.getMessage()); }
    }
}
