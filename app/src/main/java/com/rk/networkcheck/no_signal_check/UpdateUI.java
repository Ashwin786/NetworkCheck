package com.rk.networkcheck.no_signal_check;

interface UpdateUI {

    void startService();

    void stopService();

    void startActivityForResult();

    void update_signal(String value);

    void stopButton();

    void startButton();
}
