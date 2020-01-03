package com.rk.networkcheck.no_signal_check;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import com.rk.networkcheck.R;

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

    public NetworkPresenter() {

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

    public void startMonitor() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        initHandler();
//        initListener();
        init_timer();
    }

    private void init_timer() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: activityHandler " + activityHandler);
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
                    show_warning();
                }

                if (activityHandler != null) {
                    Message activityMsg = activityHandler.obtainMessage();
                    activityMsg.what = 0;
                    activityMsg.arg1 = signalStrengthValue;
                    activityHandler.sendMessage(activityMsg);
                }

            }
        };
        timer_on();
    }

    private void initListener() {
        psListener = new MyPhoneStateListener(handler);
        telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG, "handleMessage: activityHandler " + activityHandler);

                if (msg.what == 1) {
                    if (checksignal() == 0) {
                        show_warning();
                    }
                    return;
                }

                int signalStrengthValue = processSignalStrenth(msg);
                if (signalStrengthValue == past_SignalStrength) {
                    Log.e(TAG, "handleMessage: " + "Same signal strength");
                    return;
                }
                if (activityHandler != null) {
                    Message activityMsg = activityHandler.obtainMessage();
                    activityMsg.obj = msg.obj;
                    activityHandler.sendMessage(activityMsg);
                }
                past_SignalStrength = signalStrengthValue;
                if (signalStrengthValue < 5) {
                    timer_on();
                } else
                    timer_off();
            }
        };

    }

    protected int processSignalStrenth(Message msg) {
        SignalStrength signalStrength = (SignalStrength) msg.obj;
        Log.e(TAG, "getCallState: " + telephonyManager.getCallState());
        if (telephonyManager.getCallState() != 0) {
            Log.e(TAG, "handleMessage: " + "On call");
            return 0;
        }

        if (isAirplaneModeOn(context)) {
            Log.e(TAG, "handleMessage: " + "Airplane mode on");
            return 0;
        }

        String networkType = getNetworkClass();
        Log.e(TAG, "networkType: " + networkType);
        int signalStrengthValue = 0;
        if (signalStrength.isGsm()) {
            String ssignal = signalStrength.toString();

            String[] parts = ssignal.split(" ");
                   /* for (int i = 0; i < parts.length; i++) {
                        Log.e("parts : [" + i + "]", "" + parts[i]);
                    }*/


            if (networkType.equals("4G"))
                signalStrengthValue = Integer.parseInt(parts[8]);
            else if (networkType.equals("3G"))
                signalStrengthValue = Integer.parseInt(parts[15]);
            else if (networkType.equals("2G"))
                signalStrengthValue = Integer.parseInt(parts[1]);
            else
                signalStrengthValue = 0;

            if (signalStrengthValue == 99)
                signalStrengthValue = 0;


            Log.e("signalStrengthValue: ", "" + signalStrengthValue);


        }
        return signalStrengthValue;
    }

    protected int checksignal() {
        int signal = 0;
        if (telephonyManager.getAllCellInfo() == null || telephonyManager.getAllCellInfo().size() == 0)
            return signal;

        for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
            if (cellInfo.isRegistered()) {
                signal = getSignalStrengthDbm(cellInfo);
                Log.e(TAG, "cellInfo : " + cellInfo);
                Log.e(TAG, "checksignal : " + signal);
                break;
            }
        }
        return signal;
       /* CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
        cellSignalStrengthGsm.getDbm();*/
    }

    protected void timer_off() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null)
            timer.cancel();
        hide_dialog();
    }

    private void hide_dialog() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    protected void show_warning() {
        alarm();
        show_dialog();
    }

    private void show_dialog() {
        if (alertDialog != null && alertDialog.isShowing())
            return;
        builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));
        builder.setTitle("Warning!!");
        builder.setMessage("No Network");
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
            timer.scheduleAtFixedRate(timerTask, 3000, 15 * 1000);
//            timer.scheduleAtFixedRate(timerTask, 3000, 5 * 1000);
            Log.e(TAG, "TimerTask started");
        }
    }

    public void stopMonitor() {
        timer_off();
        if (psListener != null)
            telephonyManager.listen(psListener, PhoneStateListener.LISTEN_NONE);
    }

    public String getNetworkClass() {

        int networkType = telephonyManager.getNetworkType();


        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    private boolean isAirplaneModeOn(Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }

    private static int getSignalStrengthDbm(CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoCdma) {
            return ((CellInfoCdma) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoGsm) {
            return ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoLte) {
            return ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoWcdma) {
            return ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
        }
        return 0;
    }
}
