package com.rk.networkcheck.no_signal_check;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by user1 on 19/10/17.
 */
public class MyService extends Service {

    private static final String TAG = "NoSignal_Service";

    public NetworkPresenter getNetworkPresenter() {
        return networkPresenter;
    }

    private NetworkPresenter networkPresenter;

    public IBinder onBind(Intent arg0) {
        Log.e(TAG, "onUnbind: ");
        return binder;
    }

    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "Service started");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: ");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "onRebind: ");
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand started");
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 500);

        networkPresenter = NetworkPresenter.getInstance(this);
        networkPresenter.monitorByTimer();
//        networkPresenter.timer_on();
        return START_STICKY;
    }

    public void setActivityHandler(Handler activityHandler) {
        if (networkPresenter != null)
            networkPresenter.setActivityHandler(activityHandler);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Service stopped");
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
        if (networkPresenter != null)
            networkPresenter.stopMonitor();
//        networkPresenter.timer_off();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved");
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);


    }

    IBinder binder = new MyBinder();

    class MyBinder extends Binder {
        MyService getServices() {
            return MyService.this;
        }
    }


}

