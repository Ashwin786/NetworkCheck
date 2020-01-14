package com.rk.networkcheck.no_signal_check;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rk.networkcheck.R;
import com.rk.networkcheck.call_statistics.CallStatActivity;

public class MainActivity extends AppCompatActivity implements UpdateUI {


    private static final String TAG = "MainActivity";
    private Button button;
    private ImageButton btn_call_history;
    private TextView tv_networkType, tv_signal_Status, tv_signal_value, tv_dbm_value;
    private MainPresenter presenter;
    protected int OVERLAY_PERMISSION_CODE = 0;
    private ConstraintLayout cl_details;


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
        btn_call_history = (ImageButton) findViewById(R.id.btn_call_history);
        tv_networkType = (TextView) findViewById(R.id.tv_networkType);
        tv_signal_value = (TextView) findViewById(R.id.tv_signal_value);
        tv_signal_Status = (TextView) findViewById(R.id.tv_signal_Status);
        tv_dbm_value = (TextView) findViewById(R.id.tv_dbm_value);
        cl_details = (ConstraintLayout) findViewById(R.id.cl_details);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.toggle_service();
            }
        });
        btn_call_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CallStatActivity.class));
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
    public void update_signal(SignalDetails signalDetails) {
        tv_signal_Status.setText(signalDetails.getSignalDesc());
        tv_networkType.setText(signalDetails.getNetworkType());
        tv_signal_value.setText("" + (signalDetails.getSignalValue() <= 0 ? "" : "" + signalDetails.getSignalValue()));
        tv_dbm_value.setText("" + (signalDetails.getDbmValue() <= MyApp.MAX_DBM ?  "" : "" + signalDetails.getDbmValue()));
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
        cl_details.setVisibility(View.INVISIBLE);
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
        cl_details.setVisibility(View.VISIBLE);
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
