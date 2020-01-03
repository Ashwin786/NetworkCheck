package com.rk.networkcheck.no_signal_check;

import android.os.Handler;
import android.os.Looper;

public class LooperThread extends Thread {
    public Handler threadUpdateHandler;

    public LooperThread() {
    }

    @Override
    public void run() {
        Looper.prepare();
        threadUpdateHandler = new Handler();
        Looper.loop();
    }
}
