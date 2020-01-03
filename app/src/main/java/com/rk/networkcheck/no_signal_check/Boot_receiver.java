package com.rk.networkcheck.no_signal_check;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Boot_receiver extends BroadcastReceiver {
    public Boot_receiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.e("Boot_receiver", "started");
        context.startService(new Intent(context,MyService.class));
        Log.e("Boot_receiver", "done");
    }
}
