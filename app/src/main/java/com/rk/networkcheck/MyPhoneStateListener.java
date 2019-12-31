package com.rk.networkcheck;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;


public class MyPhoneStateListener extends PhoneStateListener {
    Handler handler;

    public MyPhoneStateListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.obj =signalStrength;
        handler.sendMessage(msg);
    }
}
