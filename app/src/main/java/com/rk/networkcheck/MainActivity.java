package com.rk.networkcheck;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private int OVERLAY_PERMISSION_CODE = 0;
    private SharedPreferences sp;
    private Button button;
    private TextView tv_service_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkANDgetpermission();
        init();
        check_service();
    }

    private void init() {
        sp = getSharedPreferences("network", Context.MODE_PRIVATE);
        button = (Button) findViewById(R.id.button);
        tv_service_status = (TextView) findViewById(R.id.tv_service_status);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                NetworkPresenter.getInstance(MainActivity.this).checksignal();
                toggle_service();
            }
        });

    }

    private void check_service() {
        if (isMyServiceRunning(MyService.class)) {
            button.setText("Stop");
            tv_service_status.setText("Service Status : Running");
        } else {
            button.setText("Start");
            tv_service_status.setText("Service Status : Stopped");
        }

    }

    private void toggle_service() {
        if (isMyServiceRunning(MyService.class)) {
            stopService(new Intent(this, MyService.class));
            button.setText("Start");
            tv_service_status.setText("Service Status : Stopped");
        } else {
            startService(new Intent(this, MyService.class));
            button.setText("Stop");
            tv_service_status.setText("Service Status : Started");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    check_overlay();
                }
            }, 1500);

        }
    }

    private void check_overlay() {
        if (canDrawOverlays(this)) {
//               Common.getInstance(this).block_ui();
            sp.edit().putBoolean("permission_status", true).commit();
        } else {
            Toast.makeText(this, "Kindly switch on the permission button", Toast.LENGTH_SHORT).show();
            addOverlay();
        }
    }

    public boolean addOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE);

                return false;
            }
        }
        return true;
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

    public boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private int checkANDgetpermission() {
        String[] network = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION};
        ArrayList<String> list = new ArrayList();
        int j = 0;
        for (int i = 0; i < network.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, network[i]) != PackageManager.PERMISSION_GRANTED) {
                list.add(network[i]);
                j++;
            }
        }

        if (list.size() > 0) {
            String[] get = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                get[i] = list.get(i);
            }
            ActivityCompat.requestPermissions(MainActivity.this, get, 0);
        } else
            addOverlay();
        return j;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            boolean permission_granted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    permission_granted = false;
                }
            }
            if (!permission_granted)
                checkANDgetpermission();
            else {
                addOverlay();
            }
        }


    }

/*parts[1] = GsmSignalStrength

parts[2] = GsmBitErrorRate

parts[3] = CdmaDbm

parts[4] = CdmaEcio

parts[5] = EvdoDbm

parts[6] = EvdoEcio

parts[7] = EvdoSnr

parts[8] = LteSignalStrength

parts[9] = LteRsrp

parts[10] = LteRsrq

parts[11] = LteRssnr

parts[12] = LteCqi

parts[13] = gsm|lte|cdma

parts[14] = _not really sure what this number is_*/
}
