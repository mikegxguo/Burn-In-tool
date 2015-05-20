package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-20
 * Time: 下午2:54
 */
public class NFCTest extends TestClass {
    private static final String ACTION_NFC_CHANGE_STATUS = "android.action.NFC_CHANGE_STATUS";
    private static final String EXTRA_NFC_STATE = "android.nfc.extra.STATE";
    private static final int NFC_STATE_DISABLE = 1;
    private static final int NFC_STATE_ENABLE = 2;
    private static final int NFC_STATE_REINIT = 3;

    private static boolean scanned;
    private static String strUID;
    private static String strCardType;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    public NFCTest(Messenger messenger, Activity activity, boolean isContinuous) {
        super(messenger, activity, isContinuous);
        this.id = BISTApplication.NFCTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    public NFCTest(Messenger messenger, Activity activity, int cycles) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.NFCTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i <= src.length - 1; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    private void restartNFCReader() {
        Intent intent = new Intent(ACTION_NFC_CHANGE_STATUS);
        intent.putExtra(EXTRA_NFC_STATE, NFC_STATE_REINIT);
        activity.sendBroadcast(intent);
    }

    private void enableNFCReader() {
        Intent intent = new Intent(ACTION_NFC_CHANGE_STATUS);
        intent.putExtra(EXTRA_NFC_STATE, NFC_STATE_ENABLE);
        activity.sendBroadcast(intent);
    }

    private void disableNFCReader() {
        Intent intent = new Intent(ACTION_NFC_CHANGE_STATUS);
        intent.putExtra(EXTRA_NFC_STATE, NFC_STATE_DISABLE);
        activity.sendBroadcast(intent);
    }

    public static void setScanned(byte[] uid, String type) {
        scanned = true;
        strUID = bytesToHexString(uid);
        strCardType = type;
    }

    private void initPM() {
        pendingIntent = PendingIntent
                .getActivity(activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
        }
        intentFiltersArray = new IntentFilter[]{ndef};
        techListsArray = new String[][]{
                new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcF.class.getName()},
                new String[]{NfcV.class.getName()}};
    }

    private void enableForegroundDispatch() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray);
            }
        });
    }

    private void disableForegroundDispatch() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nfcAdapter.disableForegroundDispatch(activity);
            }
        });
    }

    @Override
    public boolean classSetup() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            // There is no NFC module
            return false;
        }

        scanned = false;
        for (int i = 0; i < 60; i++) {
            // NFC module is not ON
            if (!nfcAdapter.isEnabled()) {
                if (i % 20 == 0) {
                    enableNFCReader();
                }
                SystemClock.sleep(1000);
            } else {
                disableNFCReader();
                enableNFCReader();
                for (int j = 0; j < 50; j++) {
                    SystemClock.sleep(100);
                    if(scanned)
                        return true;
                }
                return false;
            }
        }
//        initPM();
        return false;
    }

    @Override
    public boolean testBegin() {
        scanned = false;
        sendMessage("Scanning card", true);
        disableNFCReader();
        enableNFCReader();
        SystemClock.sleep(5000);
        sendMessage("NFC enabled = " + nfcAdapter.isEnabled(), true);
        if (scanned) {
            sendMessage("Scanned card [" + strCardType + "] uid [" + strUID + "]", true);
        } else {
            sendMessage("Missing scanning this card!", true);
        }
        return scanned;
    }

    @Override
    public boolean testCleanup() {
        disableNFCReader();
        return true;
    }
}
