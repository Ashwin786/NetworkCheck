package com.rk.networkcheck.call_statistics;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import java.util.Date;

public class CallPresenter {
    private static CallPresenter ourInstance;
    private static Context mContext;

    public CallPresenter() {

    }

    public static CallPresenter getInstance(Context mcontext) {
        mContext = mcontext;
        if (ourInstance == null)
            ourInstance = new CallPresenter();

        return ourInstance;
    }

    public void getCallDetails() {

        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, CallLog.Calls.DATE + " DESC");
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            String names = managedCursor.getString(name); // mobile number
            String phNumber = managedCursor.getString(number); // mobile number
            String callType = managedCursor.getString(type); // call type
            String callDate = managedCursor.getString(date); // call date
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\n Name:--- " + names + "\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");
        }
        managedCursor.close();
        Log.e("Agil value --- ", sb.toString());
    }

    public int getOutgoingCallTime(long from_time, long to_time, int type) {
        String callType = "";
        String[] selArgs = new String[]{Long.toString(from_time), Long.toString(to_time)};
        if (type != 0) {
            callType = " and " + CallLog.Calls.TYPE + " = ? ";
            selArgs = new String[]{Long.toString(from_time), Long.toString(to_time), Integer.toString(type)};
        }
        String selection = CallLog.Calls.DATE + " > ? and "
                + CallLog.Calls.DATE + " < ? and " + CallLog.Calls.DURATION + " <> 0" + callType;
        Cursor managedCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.DURATION},
                selection, selArgs, CallLog.Calls.DATE + " DESC");

        int callDuration = 0;
        while (managedCursor.moveToNext()) {
            callDuration += managedCursor.getInt(0);
        }
        managedCursor.close();
        Log.e("callDuration value --- ", "" + callDuration);
        return callDuration;
    }
}
