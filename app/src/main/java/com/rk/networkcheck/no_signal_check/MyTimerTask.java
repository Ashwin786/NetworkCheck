package com.rk.networkcheck.no_signal_check;

import android.os.Message;
import android.util.Log;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    NetworkPresenter networkPresenter;
    private static final String TAG = "MyTimerTask";
    public MyTimerTask(NetworkPresenter networkPresenter) {
        this.networkPresenter = networkPresenter;
    }

    @Override
    public void run() {
        Log.e(TAG, "run: ");
        Message msg = networkPresenter.timerHandler.obtainMessage();
        msg.what = 1;
        networkPresenter.timerHandler.sendMessage(msg);
    }

}
