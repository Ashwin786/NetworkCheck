package com.rk.networkcheck;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyApp extends Application {
    private static final String TAG = "Network check";

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.d(TAG, "Uncaught exception start!");
                paramThrowable.printStackTrace();

                //Same as done in onTaskRemoved()
                PendingIntent service = PendingIntent.getService(
                        getApplicationContext(),
                        1001,
                        new Intent(getApplicationContext(), MyService.class),
                        PendingIntent.FLAG_ONE_SHOT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
                System.exit(2);
            }
        });
    }
}
