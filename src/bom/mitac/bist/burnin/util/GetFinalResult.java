package bom.mitac.bist.burnin.util;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import bom.mitac.bist.burnin.module.BISTApplication;


import java.math.BigDecimal;

public class GetFinalResult {
    private static Recorder recorder;
    private static double failRate;
    private static int passResult;
    private static int failResult;
    private static boolean finalJudge;
    private static double normalStandard;
    private static double wifiStandard;
    private static double btStandard;
    private static double bcrStandard;
    private static double suspendStandard;
    public static String finalResult;
    private static int batteryId;
    private static int btId;
    private static int nfcId;
    private static int sdId;
    private static int inandId;
    private static int wifiId;
    private static int videoId;
    private static int bklId;
    private static int bcrId;
    private static int cameraId;
    private static int sensorId;
    private static int gpsId;
    private static int flashId;
    private static int suspendId;
    private static int rebootId;
    public static StringBuilder strFailRate;
    public static SpannableString span;

    // FailRate is int
//    public static boolean GetFinalResult() {
//        init();
//        recorder = Recorder.getInstance();
//        strFailRate = new StringBuilder();
//
//        int i = -1;
//        int[] indexSpan = new int[Recorder.MAX_RECORD];
//        int[] indexSpanLength = new int[Recorder.MAX_RECORD];
//
//
//        int passTimes;
//        int failTimes;
//        int failRate;
//        boolean result = true;
//        for (int id : BISTApplication.IDS) {
//            passTimes = recorder.getPassTimes(id);
//            failTimes = recorder.getFailTimes(id);
//            if (passTimes > 0 || failTimes > 0) {
//                failRate = failTimes * 100 / (passTimes + failTimes);
//                int startIndex = strFailRate.length();
//                strFailRate.append(String.format("%-10s", BISTApplication.ID_NAME.get(id))).append(":\t")
//                        .append("Pass = ").append(passTimes).append("\t")
//                        .append("Fail = ").append(failTimes).append("\t")
//                        .append("Fail Rate = ").append(failRate).append("%\r\n");
//
//                if (failTimes != 0 && failRate >= getFailRate(id)) {
//                    result = false;
//
//                    if (failRate == 0) {
//                        i++;
//                        indexSpan[i] = strFailRate.substring(startIndex).indexOf("Fail = ");
//                        indexSpan[i] += startIndex;
//                        indexSpanLength[i] = "Fail = ".length() + String.valueOf(failTimes).length() + 1;
//                    } else {
//                        i++;
//                        indexSpan[i] = strFailRate.substring(startIndex).indexOf("Fail Rate = ");
//                        indexSpan[i] += startIndex;
//                        indexSpanLength[i] = "Fail Rate = ".length() + String.valueOf(failRate).length() + 1;
//                    }
//
//                }
//            }
//        }
//        span = new SpannableString(strFailRate);
//        for (int j = 0; j <= i; j++) {
//            span.setSpan(new ForegroundColorSpan(Color.BLACK), indexSpan[j], indexSpan[j] + indexSpanLength[j], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            span.setSpan(new BackgroundColorSpan(Color.YELLOW), indexSpan[j], indexSpan[j] + indexSpanLength[j], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        return result;
//
//
////        finalResult = "\n" + "Battery = Pass:" + getPassResult(batteryId)
////                + " Fail:" + getFailResult(batteryId) + " Fail Rate:"
////                + getFailRate(batteryId) + "%" + "\n"
////
////                + "BT = Pass:" + getPassResult(btId) + " Fail:"
////                + getFailResult(btId) + " Fail Rate:" + getFailRate(btId)
////                + "%" + "\n"
////
////                + "NFC = Pass:" + getPassResult(nfcId) + " Fail:"
////                + getFailResult(nfcId) + " Fail Rate:" + getFailRate(nfcId)
////                + "%" + "\n"
////
////                + "SD = Pass:" + getPassResult(sdId) + " Fail:"
////                + getFailResult(sdId) + " Fail Rate:" + getFailRate(sdId)
////                + "%" + "\n"
////
////                + "iNAND = Pass:" + getPassResult(inandId) + " Fail:"
////                + getFailResult(inandId) + " Fail Rate:" + getFailRate(inandId)
////                + "%" + "\n"
////
////                + "Wifi = Pass:" + getPassResult(wifiId) + " Fail:"
////                + getFailResult(wifiId) + " Fail Rate:" + getFailRate(wifiId)
////                + "%" + "\n"
////
////                + "Video = Pass:" + getPassResult(videoId) + " Fail:"
////                + getFailResult(videoId) + " Fail Rate:" + getFailRate(videoId)
////                + "%" + "\n"
////
////                + "BKL = Pass:" + getPassResult(bklId) + " Fail:"
////                + getFailResult(bklId) + " Fail Rate:" + getFailRate(bklId)
////                + "%" + "\n"
////
////                + "BCR = Pass:" + getPassResult(bcrId) + " Fail:"
////                + getFailResult(bcrId) + " Fail Rate:" + getFailRate(bcrId)
////                + "%" + "\n"
////
////                + "Camera = Pass:" + getPassResult(cameraId) + " Fail:"
////                + getFailResult(cameraId) + " Fail Rate:"
////                + getFailRate(cameraId) + "%" + "\n"
////
////                + "Sensor = Pass:" + getPassResult(sensorId) + " Fail:"
////                + getFailResult(sensorId) + " Fail Rate:"
////                + getFailRate(sensorId) + "%" + "\n"
////
////                + "GPS = Pass:" + getPassResult(gpsId) + " Fail:"
////                + getFailResult(gpsId) + " Fail Rate:" + getFailRate(gpsId)
////                + "%" + "\n"
////
////                + "Flash = Pass:" + getPassResult(flashId) + " Fail:"
////                + getFailResult(flashId) + " Fail Rate:" + getFailRate(flashId)
////                + "%" + "\n"
////
////                + "Suspend = Pass:" + getPassResult(suspendId) + " Fail:"
////                + getFailResult(suspendId) + " Fail Rate:"
////                + getFailRate(suspendId) + "%" + "\n"
////
////                + "Reboot = Pass:" + getPassResult(rebootId) + " Fail:"
////                + getFailResult(rebootId) + " Fail Rate:"
////                + getFailRate(rebootId) + "%" + "\n";
////
////        finalJudge = (getFailRate(batteryId) == normalStandard)
////                && (getFailRate(btId) < btStandard)
////                && (getFailRate(nfcId) == normalStandard)
////                && (getFailRate(sdId) == normalStandard)
////                && (getFailRate(inandId) == normalStandard)
////                && (getFailRate(wifiId) < wifiStandard)
////                && (getFailRate(videoId) == normalStandard)
////                && (getFailRate(bklId) == normalStandard)
////                && (getFailRate(bcrId) < bcrStandard)
////                && (getFailRate(cameraId) == normalStandard)
////                && (getFailRate(sensorId) == normalStandard)
////                && (getFailRate(gpsId) == normalStandard)
////                && (getFailRate(flashId) == normalStandard)
////                && (getFailRate(suspendId) < suspendStandard)
////                && (getFailRate(rebootId) == normalStandard);
////
////        if (finalJudge) {
////            return true;
////        } else {
////            return false;
////        }
//    }

    // FailRate is float
    public static boolean GetFinalResult() {
        init();
        recorder = Recorder.getInstance();
        strFailRate = new StringBuilder();

        int i = -1;
        int[] indexSpan = new int[Recorder.MAX_RECORD];
        int[] indexSpanLength = new int[Recorder.MAX_RECORD];


        int passTimes;
        int failTimes;
        double failRate;
        boolean result = true;
        for (int id : BISTApplication.IDS) {
            passTimes = recorder.getPassTimes(id);
            failTimes = recorder.getFailTimes(id);
            if (passTimes > 0 || failTimes > 0) {
                failRate = round((double) failTimes * 100 / (passTimes + failTimes), 2);
//                failRate = Math.round(failTimes * 10000 / (passTimes + failTimes)) / 100;
                int startIndex = strFailRate.length();
                strFailRate.append(String.format("%-10s", BISTApplication.ID_NAME.get(id))).append(":\t")
                        .append("Pass = ").append(passTimes).append("\t")
                        .append("Fail = ").append(failTimes).append("\t")
                        .append("Fail Rate = ").append(failRate).append("%\r\n");

                if (failTimes != 0 && failRate >= getFailRate(id)) {
                    result = false;
                    i++;
                    indexSpan[i] = strFailRate.substring(startIndex).indexOf("Fail Rate = ");
                    indexSpan[i] += startIndex;
                    indexSpanLength[i] = "Fail Rate = ".length() + String.valueOf(failRate).length() + 1;
                }
            }
        }
        span = new SpannableString(strFailRate);
        for (int j = 0; j <= i; j++) {
            span.setSpan(new ForegroundColorSpan(Color.BLACK), indexSpan[j], indexSpan[j] + indexSpanLength[j], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new BackgroundColorSpan(Color.YELLOW), indexSpan[j], indexSpan[j] + indexSpanLength[j], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;


//        finalResult = "\n" + "Battery = Pass:" + getPassResult(batteryId)
//                + " Fail:" + getFailResult(batteryId) + " Fail Rate:"
//                + getFailRate(batteryId) + "%" + "\n"
//
//                + "BT = Pass:" + getPassResult(btId) + " Fail:"
//                + getFailResult(btId) + " Fail Rate:" + getFailRate(btId)
//                + "%" + "\n"
//
//                + "NFC = Pass:" + getPassResult(nfcId) + " Fail:"
//                + getFailResult(nfcId) + " Fail Rate:" + getFailRate(nfcId)
//                + "%" + "\n"
//
//                + "SD = Pass:" + getPassResult(sdId) + " Fail:"
//                + getFailResult(sdId) + " Fail Rate:" + getFailRate(sdId)
//                + "%" + "\n"
//
//                + "iNAND = Pass:" + getPassResult(inandId) + " Fail:"
//                + getFailResult(inandId) + " Fail Rate:" + getFailRate(inandId)
//                + "%" + "\n"
//
//                + "Wifi = Pass:" + getPassResult(wifiId) + " Fail:"
//                + getFailResult(wifiId) + " Fail Rate:" + getFailRate(wifiId)
//                + "%" + "\n"
//
//                + "Video = Pass:" + getPassResult(videoId) + " Fail:"
//                + getFailResult(videoId) + " Fail Rate:" + getFailRate(videoId)
//                + "%" + "\n"
//
//                + "BKL = Pass:" + getPassResult(bklId) + " Fail:"
//                + getFailResult(bklId) + " Fail Rate:" + getFailRate(bklId)
//                + "%" + "\n"
//
//                + "BCR = Pass:" + getPassResult(bcrId) + " Fail:"
//                + getFailResult(bcrId) + " Fail Rate:" + getFailRate(bcrId)
//                + "%" + "\n"
//
//                + "Camera = Pass:" + getPassResult(cameraId) + " Fail:"
//                + getFailResult(cameraId) + " Fail Rate:"
//                + getFailRate(cameraId) + "%" + "\n"
//
//                + "Sensor = Pass:" + getPassResult(sensorId) + " Fail:"
//                + getFailResult(sensorId) + " Fail Rate:"
//                + getFailRate(sensorId) + "%" + "\n"
//
//                + "GPS = Pass:" + getPassResult(gpsId) + " Fail:"
//                + getFailResult(gpsId) + " Fail Rate:" + getFailRate(gpsId)
//                + "%" + "\n"
//
//                + "Flash = Pass:" + getPassResult(flashId) + " Fail:"
//                + getFailResult(flashId) + " Fail Rate:" + getFailRate(flashId)
//                + "%" + "\n"
//
//                + "Suspend = Pass:" + getPassResult(suspendId) + " Fail:"
//                + getFailResult(suspendId) + " Fail Rate:"
//                + getFailRate(suspendId) + "%" + "\n"
//
//                + "Reboot = Pass:" + getPassResult(rebootId) + " Fail:"
//                + getFailResult(rebootId) + " Fail Rate:"
//                + getFailRate(rebootId) + "%" + "\n";
//
//        finalJudge = (getFailRate(batteryId) == normalStandard)
//                && (getFailRate(btId) < btStandard)
//                && (getFailRate(nfcId) == normalStandard)
//                && (getFailRate(sdId) == normalStandard)
//                && (getFailRate(inandId) == normalStandard)
//                && (getFailRate(wifiId) < wifiStandard)
//                && (getFailRate(videoId) == normalStandard)
//                && (getFailRate(bklId) == normalStandard)
//                && (getFailRate(bcrId) < bcrStandard)
//                && (getFailRate(cameraId) == normalStandard)
//                && (getFailRate(sensorId) == normalStandard)
//                && (getFailRate(gpsId) == normalStandard)
//                && (getFailRate(flashId) == normalStandard)
//                && (getFailRate(suspendId) < suspendStandard)
//                && (getFailRate(rebootId) == normalStandard);
//
//        if (finalJudge) {
//            return true;
//        } else {
//            return false;
//        }
    }

    public static boolean GetFinalResult(SpannableString span) {
        init();
        recorder = Recorder.getInstance();
        strFailRate = new StringBuilder();

        int i = -1;
        int[] indexSpan = new int[Recorder.MAX_RECORD];
        int[] indexSpanLength = new int[Recorder.MAX_RECORD];


        int passTimes;
        int failTimes;
        int failRate;
        boolean result = true;
        for (int id : BISTApplication.IDS) {
            passTimes = recorder.getPassTimes(id);
            failTimes = recorder.getFailTimes(id);
            if (passTimes > 0 || failTimes > 0) {
                failRate = failTimes * 100 / (passTimes + failTimes);
                strFailRate.append(BISTApplication.ID_NAME.get(id)).append(":\t")
                        .append("Pass = ").append(passTimes).append("\t")
                        .append("Fail = ").append(failTimes).append("\t")
                        .append("Fail Rate = ").append(failRate).append("%\r\n");
                if (result && failTimes != 0 && failRate >= getFailRate(id)) {
                    result = false;
                }
                if (failRate != 0) {
                    i++;
                    indexSpan[i] = strFailRate.indexOf("Fail Rate = ") + "Fail Rate = ".length();
                    indexSpanLength[i] = String.valueOf(failRate).length() + 1;
                }
            }
        }
        span = new SpannableString(strFailRate);
        for (int j = 0; j <= i; j++) {
            span.setSpan(new ForegroundColorSpan(Color.RED), indexSpan[j], indexSpan[j] + indexSpanLength[j], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new BackgroundColorSpan(Color.YELLOW), indexSpan[j], indexSpan[j] + indexSpanLength[j], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    public static int getFailRate(int id) {
        switch (id) {
            case BISTApplication.WIFITest_ID:
                return 20;
            case BISTApplication.BTTest_ID:
                return 5;
            case BISTApplication.BCRTest_ID:
                return 2;
            case BISTApplication.GPSTest_ID:
                return 1;
            case BISTApplication.SuspendTest_ID:
                return 1;
            default:
                return 0;
        }
    }

    public static void init() {
        finalResult = "";
        finalJudge = true;
        normalStandard = 0.00;
        wifiStandard = 20.0;
        btStandard = 5.0;
        bcrStandard = 2.0;
        suspendStandard = 1.0;
        batteryId = BISTApplication.BatteryTest_ID;
        btId = BISTApplication.BTTest_ID;
        nfcId = BISTApplication.NFCTest_ID;
        sdId = BISTApplication.SDTest_ID;
        inandId = BISTApplication.INANDTest_ID;
        wifiId = BISTApplication.WIFITest_ID;
        videoId = BISTApplication.VideoTest_ID;
        bklId = BISTApplication.BKLTest_ID;
        bcrId = BISTApplication.BCRTest_ID;
        cameraId = BISTApplication.CameraTest_BACK_ID;
        sensorId = BISTApplication.SensorTest_ID;
        gpsId = BISTApplication.GPSTest_ID;
        flashId = BISTApplication.FlashTest_ID;
        suspendId = BISTApplication.SuspendTest_ID;
        rebootId = BISTApplication.RebootTest_ID;
    }

//    public static double getFailRate(int id) {
//
//        double total;
//        double rate;
//        double pass;
//        double fail;
//        pass = getPassResult(id);
//        fail = getFailResult(id);
//        total = pass + fail;
//        rate = fail / total;
//        failRate = round(rate * 100, 2);
//
//        return failRate;
//    }

    public static int getPassResult(int id) {

        passResult = recorder.getPassTimes(id);

        return passResult;
    }

    public static int getFailResult(int id) {

        failResult = recorder.getFailTimes(id);

        return failResult;
    }

    public static double round(Double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = null == v ? new BigDecimal("0.0") : new BigDecimal(
                Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
