package com.rk.networkcheck;

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
        Message msg = networkPresenter.handler.obtainMessage();
        msg.what = 1;
        networkPresenter.handler.sendMessage(msg);
    }

}
