package com.rk.networkcheck.no_signal_check;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import com.rk.networkcheck.R;
import com.rk.networkcheck.low_battery_check.BatteryProcess;

import java.util.Timer;

class NetworkPresenter {

    private static NetworkPresenter ourInstance;
    private static Context context;
    private Ringtone ringtone;
    private MyTimerTask timerTask;
    MyPhoneStateListener psListener;
    protected Handler handler;
    protected Handler activityHandler;
    private TelephonyManager telephonyManager;
    private static final String TAG = "NetworkPresenter";
    private int past_SignalStrength = -1;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private Timer timer;
    private AudioManager audioManager;
    private int signalStrengthValue;
    protected Handler timerHandler;
    private String networkType = "Unknown";
    private int signal;

    public NetworkPresenter() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        init_ringtone();
    }

    public static NetworkPresenter getInstance(Context mcontext) {
        context = mcontext;
        if (ourInstance == null)
            ourInstance = new NetworkPresenter();

        return ourInstance;
    }

    public static NetworkPresenter getInstance() {
        return ourInstance;
    }

    public Handler setActivityHandler(Handler activityHandler) {
        this.activityHandler = activityHandler;
        past_SignalStrength = -1;
        return activityHandler;
    }


    public void monitorByTimer() {
        timerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: activityHandler " + activityHandler);
                /*Battery */
                if (msg.what == 2) {
                    show_warning("Battery Low");
                    return;
                }
//                BatteryProcess.getInstance(context).registerBatteryReceiver(timerHandler);
                if (telephonyManager.getCallState() != 0) {
                    Log.e(TAG, "handleMessage: " + "On call");
                    return;
                }

                if (isAirplaneModeOn(context)) {
                    Log.e(TAG, "handleMessage: " + "Airplane mode on");
                    return;
                }

                signalStrengthValue = checksignal();

                if (signalStrengthValue == 0) {
                    show_warning("No Network");
                }

            }
        };
        timer_on();
        BatteryProcess.getInstance(context).registerBatteryReceiver(timerHandler);
    }


    private void initListener() {
        psListener = new MyPhoneStateListener(handler);
        telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    protected void monitorBySignal() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: activityHandler " + activityHandler);

                SignalDetails signal = checksignalDetails();

                if (activityHandler != null) {
                    Message activityMsg = activityHandler.obtainMessage();
                    activityMsg.what = 1;

                    activityMsg.obj = signal;
                    activityHandler.sendMessage(activityMsg);
                }
            }
        };
        initListener();
    }

    private SignalDetails checksignalDetails() {
        SignalDetails signaldetail = new SignalDetails();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (telephonyManager.getAllCellInfo() == null || telephonyManager.getAllCellInfo().size() == 0)
                return signaldetail;
            for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
                if (cellInfo.isRegistered()) {
                    Log.e(TAG, "cellInfo : " + cellInfo);

                    signaldetail.setDbmValue(getSignalStrengthDbm(cellInfo));
                    signaldetail.setSignalValue(getSignalStrengthAsu(cellInfo));
                    signaldetail.setNetworkType(networkType);
                    break;
                }
            }
        }

        return signaldetail;

    }


    protected int checksignal() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (telephonyManager.getAllCellInfo() == null || telephonyManager.getAllCellInfo().size() == 0)
                return signal;
            for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
                if (cellInfo.isRegistered()) {
                    Log.e(TAG, "cellInfo : " + cellInfo);
                    signal = getSignalStrengthDbm(cellInfo);
                    Log.e(TAG, "checksignal dbm: " + signal);
//                    signal = 140 + signal;
                    Log.e(TAG, "checksignal value: " + signal);

                    break;
                }
            }
        } else {
            if (telephonyManager.getCellLocation() == null)
                return signal;
            CellLocation cellInfo = telephonyManager.getCellLocation();
            Log.e(TAG, "cellInfo : " + cellInfo);
            Log.e(TAG, "checksignal : " + signal);
        }


        return signal;
       /* CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
        cellSignalStrengthGsm.getDbm();*/
    }

    protected void stopMonitorByTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null)
            timer.cancel();
        hide_dialog();
        BatteryProcess.getInstance(context).unregisterBatteryReceiver();
    }

    private void hide_dialog() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    protected void show_warning(String msg) {
        alarm();
        show_dialog(msg);
    }

    private void show_dialog(String msg) {
        if (alertDialog != null && alertDialog.isShowing())
            return;
        builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));
        builder.setTitle("Warning!!");
        builder.setMessage(msg);
        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ringtone != null && ringtone.isPlaying())
                    ringtone.stop();
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        alertDialog.show();
    }

    protected void alarm() {
//        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 500);
        try {
            if (ringtone == null) {
                init_ringtone();
            }
            if (!ringtone.isPlaying()) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 100, 0);
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init_ringtone() {
        try {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(context, notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void timer_on() {

        if (timerTask == null) {
            timerTask = new MyTimerTask(this);
            //running timer task as daemon thread
            timer = new Timer(true);
//        timer.schedule(timerTask, 0);
            timer.scheduleAtFixedRate(timerTask, 3000, 15 * 60 * 1000);
//            timer.scheduleAtFixedRate(timerTask, 3000, 5 * 1000);
            Log.e(TAG, "TimerTask started");
        }
    }

    public void stopMonitorBySignal() {
        if (psListener != null)
            telephonyManager.listen(psListener, PhoneStateListener.LISTEN_NONE);
//        BatteryProcess.getInstance(context).unregisterBatteryReceiver();
    }

    private boolean isAirplaneModeOn(Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }

    private int getSignalStrengthDbm(CellInfo cellInfo) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (cellInfo instanceof CellInfoCdma) {
                networkType = "2G";
                return ((CellInfoCdma) cellInfo).getCellSignalStrength().getDbm();
            }
            if (cellInfo instanceof CellInfoGsm) {
                networkType = "2G";
                return ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
            }
            if (cellInfo instanceof CellInfoLte) {
                networkType = "4G";
                return ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
            }
            if (cellInfo instanceof CellInfoWcdma) {
                networkType = "3G";
                return ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
            }
        }
        networkType = "Unknown";
        return 0;
    }

    private int getSignalStrengthAsu(CellInfo cellInfo) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (cellInfo instanceof CellInfoCdma) {
                return ((CellInfoCdma) cellInfo).getCellSignalStrength().getAsuLevel();
            }
            if (cellInfo instanceof CellInfoGsm) {
                return ((CellInfoGsm) cellInfo).getCellSignalStrength().getAsuLevel();
            }
            if (cellInfo instanceof CellInfoLte) {
                return ((CellInfoLte) cellInfo).getCellSignalStrength().getAsuLevel();
            }
            if (cellInfo instanceof CellInfoWcdma) {
                return ((CellInfoWcdma) cellInfo).getCellSignalStrength().getAsuLevel();
            }
        }
        return 0;
    }


}
