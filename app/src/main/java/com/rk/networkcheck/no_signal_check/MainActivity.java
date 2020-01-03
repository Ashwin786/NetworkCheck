package com.rk.networkcheck.no_signal_check;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rk.networkcheck.R;
import com.rk.networkcheck.call_statistics.CallPresenter;

public class MainActivity extends AppCompatActivity implements UpdateUI {


    private static final String TAG = "MainActivity";
    private Button button;
    private TextView tv_service_status, tv_signal_Status;
    private MainPresenter presenter;
    protected int OVERLAY_PERMISSION_CODE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = MainPresenter.getInstance(this);
        presenter.checkANDgetpermission();
        init();

    }


    @Override
    protected void onResume() {
        super.onResume();
        presenter.check_service();

    }


    private void init() {

        button = (Button) findViewById(R.id.button);
        tv_service_status = (TextView) findViewById(R.id.tv_service_status);
        tv_signal_Status = (TextView) findViewById(R.id.tv_signal_Status);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.toggle_service();
            }
        });

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
                    presenter.check_overlay();
                }
            }, 1500);

        }
    }

    @Override
    public void startActivityForResult() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
    }

    @Override
    public void update_signal(String value) {
        tv_signal_Status.setText(value);
    }

    @Override
    public void stopService() {
        stopService(new Intent(this, MyService.class));

    }
    @Override
    public void startService() {
//        startService(new Intent(this, MyService.class));
        Intent intent = new Intent(this, MyService.class);
        startService(intent);


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
                presenter.checkANDgetpermission();
            else {
                presenter.addOverlay();
            }
        }


    }


    @Override
    public void stopButton() {
        button.setText("Start");
        tv_service_status.setText("Service Status : Stopped");
        unbindService();
    }

    private void unbindService() {
        try {
            if (presenter.mIsBound) {
                unbindService(presenter.serviceConnection);
                presenter.mIsBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startButton() {
        button.setText("Stop");
        tv_service_status.setText("Service Status : Started");
        if (!presenter.mIsBound) {
            Intent intent = new Intent(this, MyService.class);
            bindService(intent, presenter.serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: ");
        unbindService();
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
