package com.rk.networkcheck.no_signal_check;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class MainPresenter {
    private static MainPresenter ourInstance;
    private static UpdateUI updateUI;
    private final Context mcontext;
    private SharedPreferences sp;
    private Handler activity_handler;
    private static final String TAG = "MainPresenter";
    private MyService myService;

    public MainPresenter(Context mcontext) {
        this.mcontext = mcontext;
        sp = this.mcontext.getSharedPreferences("network", Context.MODE_PRIVATE);
        initHandler();
    }

    public static MainPresenter getInstance(Context mcontext) {
        updateUI = (UpdateUI) mcontext;
        if (ourInstance == null)
            ourInstance = new MainPresenter(mcontext);

        return ourInstance;
    }

    protected int checkANDgetpermission() {
        String[] network = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CALL_LOG};
        ArrayList<String> list = new ArrayList();
        int j = 0;
        for (int i = 0; i < network.length; i++) {
            if (ContextCompat.checkSelfPermission(mcontext, network[i]) != PackageManager.PERMISSION_GRANTED) {
                list.add(network[i]);
                j++;
            }
        }

        if (list.size() > 0) {
            String[] get = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                get[i] = list.get(i);
            }
            ActivityCompat.requestPermissions((Activity) mcontext, get, 0);
        } else
            addOverlay();
        return j;
    }

    static boolean canDrawOverlays(Context context) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Settings.canDrawOverlays(context))
            return true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            //USING APP OPS MANAGER
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (manager != null) {
                try {
                    int result = manager.checkOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, Binder.getCallingUid(), context.getPackageName());
                    return result == AppOpsManager.MODE_ALLOWED;
                } catch (Exception ignore) {
                }
            }
        }

        try {
            //IF This Fails, we definitely can't do it
            WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (mgr == null) return false; //getSystemService might return null
            View viewToAdd = new View(context);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
            viewToAdd.setLayoutParams(params);
            mgr.addView(viewToAdd, params);
            mgr.removeView(viewToAdd);
            return true;
        } catch (Exception ignore) {
        }
        return false;

    }

    protected void check_overlay() {
        if (canDrawOverlays(mcontext)) {
//               Common.getInstance(this).block_ui();
            sp.edit().putBoolean("permission_status", true).commit();
        } else {
            Toast.makeText(mcontext, "Kindly switch on the permission button", Toast.LENGTH_SHORT).show();
            addOverlay();
        }
    }

    public boolean addOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mcontext)) {
                updateUI.startActivityForResult();
                return false;
            }
        }
        return true;
    }

    protected void toggle_service() {
        if (isMyServiceRunning(MyService.class)) {
            updateUI.stopService();
            updateUI.stopButton();

        } else {
            updateUI.startService();
            updateUI.startButton();

        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) mcontext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void check_service() {
        if (isMyServiceRunning(MyService.class)) {
            updateUI.startButton();
        } else {
            updateUI.stopButton();
        }
    }

    private void initHandler() {
        activity_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: " + "New signal strength");
                if (msg.what == 0)
                    updateUI.update_signal(getValue(msg.arg1));
                else {
                    int signal = NetworkPresenter.getInstance().processSignalStrenth(msg);
                    updateUI.update_signal(getValue(signal));
                }

            }
        };

//        NetworkPresenter.getInstance().setActivityHandler(activity_handler);

    }

    private String getValue(int signalStrengthValue) {
        String signalDesc = "No Signal";
        if (signalStrengthValue >= 35) {
            signalDesc = "Signal : Very Good";
        } else if (signalStrengthValue >= 30) {
            signalDesc = "Signal : Good";
        } else if (signalStrengthValue > 20 && signalStrengthValue < 30) {
            signalDesc = "Signal  : Average";
        } else if (signalStrengthValue < 20 && signalStrengthValue > 3) {
            signalDesc = "Signal  : Weak";
        } else if (signalStrengthValue < 3 && signalStrengthValue > 0) {
            signalDesc = "Signal  : Very weak";
        }
        return NetworkPresenter.getInstance().getNetworkClass() + " " + signalDesc + " / " + signalStrengthValue;
    }

    protected boolean mIsBound = false;
    protected ServiceConnection serviceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("serviceConnection", "onServiceConnected");
            MyService.MyBinder binder = (MyService.MyBinder) service;
            myService = binder.getServices();
            myService.setActivityHandler(activity_handler);
            myService.getNetworkPresenter().monitorBySignal();
            mIsBound = true;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e("serviceConnection", "onBindingDied");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("serviceConnection", "onServiceDisconnected");
            mIsBound = false;
            myService = null;
        }
    };


}
