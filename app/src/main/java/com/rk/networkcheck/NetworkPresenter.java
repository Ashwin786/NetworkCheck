package com.rk.networkcheck;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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

import java.util.Timer;

class NetworkPresenter {

    private static NetworkPresenter ourInstance;
    private static Context context;
    private MyTimerTask timerTask;
    MyPhoneStateListener psListener;
    protected Handler handler;
    private TelephonyManager telephonyManager;
    private static final String TAG = "NetworkPresenter";
    private int past_SignalStrength = -1;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;


    public static NetworkPresenter getInstance(Context mcontext) {
        context = mcontext;
        if (ourInstance == null)
            ourInstance = new NetworkPresenter();

        return ourInstance;
    }

    public void startMonitor() {
        initHandler();
        initListener();

    }

    private void initListener() {
        psListener = new MyPhoneStateListener(handler);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (checksignal() == 0) {
                        show_warning();
                    }
                    return;
                }
                SignalStrength signalStrength = (SignalStrength) msg.obj;
                int signalStrengthValue = 0;
                Log.e(TAG, "getCallState: " + telephonyManager.getCallState());
                if (telephonyManager.getCallState() != 0) {
                    Log.e(TAG, "handleMessage: " + "On call");
                    return;
                }
                String networkType = getNetworkClass();
                if (isAirplaneModeOn(context)) {
                    Log.e(TAG, "handleMessage: " + "Airplane mode on");
                    return;
                }


                Log.e(TAG, "networkType: " + networkType);
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

                    if (signalStrengthValue == past_SignalStrength) {

                        Log.e(TAG, "handleMessage: " + "Same signal strength");
                        return;
                    }
                    past_SignalStrength = signalStrengthValue;
                    Log.e("signalStrengthValue: ", "" + signalStrengthValue);
                    if (signalStrengthValue < 3) {
                        timer_on();
                    } else
                        timer_off();


                }

            }
        };

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

    private void timer_off() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
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
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void timer_on() {
        if (timerTask == null)
            timerTask = new MyTimerTask(this);
        //running timer task as daemon thread
        Timer timer = new Timer(true);
//        timer.schedule(timerTask, 0);
        timer.scheduleAtFixedRate(timerTask, 2*1000, 60 * 1000);
        Log.e(TAG, "TimerTask started" );
    }

    public void stopMonitor() {
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
