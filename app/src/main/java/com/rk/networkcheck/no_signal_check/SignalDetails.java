package com.rk.networkcheck.no_signal_check;

class SignalDetails {
    public SignalDetails() {

    }

    public void setSignalValue(int signalValue) {
        this.signalValue = signalValue;
    }

    private int signalValue = 0;

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    private String networkType;
    private int dbmValue = 0;

    public String getSignalDesc() {
        return signalDesc;
    }

    public void setSignalDesc(String signalDesc) {
        this.signalDesc = signalDesc;
    }

    String signalDesc;

    public int getSignalValue() {
        return signalValue;
    }

    public String getNetworkType() {
        return networkType;
    }

    public SignalDetails(int signalValue, String networkType) {
        this.signalValue = signalValue;
        this.networkType = networkType;
    }

    public int getDbmValue() {
        return dbmValue;
    }

    public void setDbmValue(int dbmValue) {
        this.dbmValue = dbmValue;
    }
}
