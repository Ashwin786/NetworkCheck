package com.rk.networkcheck;

import android.os.Message;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    NetworkPresenter networkPresenter;
    public MyTimerTask(NetworkPresenter networkPresenter) {
        this.networkPresenter = networkPresenter;
    }

    @Override
    public void run() {
        Message msg = networkPresenter.handler.obtainMessage();
        msg.what = 1;
        networkPresenter.handler.sendMessage(msg);
    }

}
