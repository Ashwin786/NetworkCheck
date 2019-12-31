package com.rk.networkcheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by user1 on 19/10/17.
 */
public class MyService extends Service {

    private static final String TAG = " App lock MyService";
    private NetworkPresenter networkPresenter;

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        Log.e("Network check", "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Network check", "onStartCommand started");
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 500);
        networkPresenter = NetworkPresenter.getInstance(this);
        networkPresenter.startMonitor();
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("App lock", "Service stopped");
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
        networkPresenter.stopMonitor();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved");
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);


    }
}

