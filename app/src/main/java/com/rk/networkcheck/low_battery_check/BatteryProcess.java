package com.rk.networkcheck.low_battery_check;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by User-01 on 09-07-2019.
 */

public class BatteryProcess {
    private static final String TAG = "BatteryProcess";
    private static BatteryProcess ourInstance;
    static Context globalContext;
    static boolean register = false;
    private Handler timerHandler;
    int mLowLevel = 20;

    public static BatteryProcess getInstance(Context mcontext) {
        globalContext = mcontext;
        if (ourInstance == null)
            ourInstance = new BatteryProcess();

        return ourInstance;
    }

    //Broadcast receiver for battery
    private final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // Unregistering battery receiver

//            unregisterBatteryReceiver();

            int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            Log.e(TAG, "onReceive: " + currentLevel);
            int status_ = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status_ == BatteryManager.BATTERY_STATUS_CHARGING;


            if (currentLevel < mLowLevel && !isCharging) {
                if (timerHandler != null) {
                    Message msg = timerHandler.obtainMessage();
                    msg.what = 2;
                    msg.arg1 = currentLevel;
                    timerHandler.sendMessage(msg);
                }
            }

        }
    };

    public void registerBatteryReceiver(Handler timerHandler) {
        // Registering battery receiver
        if (!register) {
            this.timerHandler = timerHandler;
            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            globalContext.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
            register = true;
        }
    }

    public void unregisterBatteryReceiver() {
        // Unregistering battery receiver
        if (register) {
            timerHandler = null;
            try {
                globalContext.unregisterReceiver(batteryLevelReceiver);
            } catch (Exception e) {
                Log.e("Battery unregister exception: ", e.toString());
            }
            register = false;
        }
    }


}
