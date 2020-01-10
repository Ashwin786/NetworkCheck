package com.rk.networkcheck;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
//                System.out.println("dbm :" + getSignalDbmDesc(-111));
        int value = 110-100;
        System.out.println("dbm :" + Math.round(value*1.66));
    }

    private String getSignalDbmDesc(long signalValue) {
        String signalDesc = "No Signal";
        if (signalValue >= -60) {
            signalDesc = "Excellent";
        } else if (signalValue >= -65) {
            signalDesc = "Very Good";
        } else if (signalValue >= -75) {
            signalDesc = "Good";
        } else if (signalValue >= -85) {
            signalDesc = "Average";
        } else if (signalValue >= -90) {
            signalDesc = "Weak";
        }else if (signalValue >= -109) {
            signalDesc = "Very Weak";
        }else{
            if (0 > 5)
                signalDesc = "nooooooooo";

        }
        return signalDesc;
    }
}