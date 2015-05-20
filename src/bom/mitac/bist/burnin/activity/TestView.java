package bom.mitac.bist.burnin.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestFactory;
import bom.mitac.bist.burnin.util.*;

import bom.mitac.bist.burnin.R;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-5-8
 * Time: 下午2:24
 */
public class TestView extends RelativeLayout implements Button.OnClickListener {

    private TextView tvShow;
    private RelativeLayout rlBackground;
    private ScrollView svLog;

    private Button btnStop;
    private Button btnInfo;

    private View mainView;

    private AlertDialog alertDialog;

    private Context context;
    private Activity activity;
    private TestFactory testFactory;
    private Recorder recorder;

    private boolean reallyWannaStop;

    public TestView(Context context, Activity activity) {
        super(context);
        this.context = context;
        this.activity = activity;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mainView = layoutInflater.inflate(R.layout.view_test, null);
        addView(mainView);
        findViews();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_stop:
                saveLog("Click stop button\r\n", BISTApplication.LOG_NAME);
                if (reallyWannaStop) {
                    if (testFactory != null) {
                        testFactory.stopTest();
                        sendMessage("TEST END");
                    }
                } else {
                    showToast("Click stop again if you need stop test");
                    reallyWannaStop = true;
                }
                break;
            case R.id.btn_info:
                saveLog("Click info button\r\n", BISTApplication.LOG_NAME);
                showInfo();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (reallyWannaStop) {
                if (testFactory != null) {
                    testFactory.stopTest();
                    sendMessage("TEST END");
                }
                return true;
            } else {
                showToast("Click back again if you need stop test");
                reallyWannaStop = true;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void findViews() {
        tvShow = (TextView) findViewById(R.id.tv_show);
        rlBackground = (RelativeLayout) findViewById(R.id.rl_background);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnInfo = (Button) findViewById(R.id.btn_info);
        svLog = (ScrollView) findViewById(R.id.sv_show);

        btnStop.setOnClickListener(this);
        btnInfo.setOnClickListener(this);
    }

    public void autoRun(TestFactory testFactory, Recorder recorder) {
        if (testFactory == null || recorder == null)
            return;

        this.testFactory = testFactory;
        this.recorder = recorder;

        if (!recorder.isTesting) {
            // The first run
            recorder.isTesting = true;
            recorder.write();
            placeOldLogs();
            if (Rebooter.isRebooterInstalled(context)) {
                // Ensure the environment is clean, so we reboot at the first time.
                new Thread() {
                    @Override
                    public void run() {
                        SystemClock.sleep(3000);
                        Rebooter.reboot(context);
                    }
                }.start();
                return;
            }
        } else {
            // Continual run
        }
        sendMessage("START TEST");

    }

    private void showInfo() {

        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Test Info")
                    .create();
        }

        if (GetFinalResult.GetFinalResult()) {
            alertDialog.setTitle("PASS   Reset Times: " + recorder.resetTimes);
            alertDialog.setMessage(GetFinalResult.span);
        } else {
            alertDialog.setTitle("FAIL   Reset Times: " + recorder.resetTimes);
            alertDialog.setMessage(GetFinalResult.span);
        }

        alertDialog.show();
    }

    public void showToast(String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    private Handler handler = new Handler() {
        private boolean isRed;
        private boolean vibratorIsKey = true;
        private boolean batteryIsKey = true;
        private boolean btIsKey = true;
        private boolean nfcIsKey = true;
        private boolean sdIsKey = true;
        private boolean inandIsKey = true;
        private boolean wifiIsKey = true;
        private boolean videoIsKey = true;
        private boolean bklIsKey = true;
        private boolean cellularIsKey = true;
        private boolean bcrIsKey = true;
        private boolean cameraIsKey = true;
        private boolean sensorIsKey = true;
        private boolean gpsIsKey = true;
        private boolean flashIsKey = true;
        private boolean suspendIsKey = true;

        private String cycle = "";
        private String time = "";

        @Override
        public void handleMessage(Message msg) {
            if (testFactory == null || recorder == null || rlBackground == null || !recorder.isTesting)
                return;

            String str = String.valueOf(msg.obj);
            switch (msg.what) {
                case BISTApplication.COMMAND:

                    if (str.equals("START TEST")) {
                        recorder.isTesting = true;
                        testFactory.startTest();
                        checkTurnRed(recorder.strFailureLog);
                        saveLog("START TEST\r\n" + recorder.strTestCondition, BISTApplication.LOG_NAME);
//                        rlBackground.setBackgroundColor(Color.BLACK);
                    } else if (str.equals("TEST END")) {
                        recorder.isTesting = false;
                        tvShow.setTextColor(Color.BLACK);
                        if (GetFinalResult.GetFinalResult()) {
                            rlBackground.setBackgroundColor(Color.GREEN);
                            tvShow.setText(GetFinalResult.span);
                            tvShow.append("\r\n" + "Reset Times: " + recorder.resetTimes + "\r\nTest Result: PASS\r\n");
                        } else {
                            rlBackground.setBackgroundColor(Color.RED);
                            tvShow.setText(GetFinalResult.span);
                            tvShow.append("\r\n" + "Reset Times: " + recorder.resetTimes + "\r\nTest Result: FAIL\r\n");
                        }
                        saveLog(tvShow.getText().toString(), BISTApplication.TOTAL_LOG_NAME);
                        btnInfo.setVisibility(View.GONE);
                        btnStop.setVisibility(View.GONE);

                        // TODO Launch the reboot agency tool Directly. It's better if judged by the config.
                        if(TestFactory.rebootTime != 0){
                            Rebooter.runRebootAgency(activity);
                        }
                    }
                    str += "\r\n";
                    recorder.write();
                    break;
                case BISTApplication.KEY:
                    activateKeys(str);
                    break;
                case BISTApplication.TIME_CYCLE:
                    if (str.startsWith("Cycle")) {
                        reallyWannaStop = false;
                        cycle = str;
//                        checkTurnRed(strFailureLog.toString());
                        if (GetFinalResult.GetFinalResult()) {
                            rlBackground.setBackgroundColor(Color.BLACK);
                        } else {
                            rlBackground.setBackgroundColor(Color.RED);
                        }
                    } else if (str.startsWith("Time")) {
                        time = str;
                    }
                    btnStop.setText(cycle + " " + time + "\nStop");
                    return;
//                case BISTApplication.VibratorTest_ID:
//                    if (vibratorIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.BatteryTest_ID:
//                    if (batteryIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.NFCTest_ID:
//                    if (nfcIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.SDTest_ID:
//                    if (sdIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.BKLTest_ID:
//                    if (bklIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.INANDTest_ID:
//                    if (inandIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.SensorTest_ID:
//                    if (sensorIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.SuspendTest_ID:
//                    if (suspendIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.BCRTest_ID:
//                    if (bcrIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.GPSTest_ID:
//                    if (gpsIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.WIFITest_ID:
//                    if (wifiIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.CellularTest_ID:
//                    if (cellularIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.VideoTest_ID:
//                    if (videoIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.BTTest_ID:
//                    if (btIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.FlashTest_ID:
//                    if (flashIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
//                case BISTApplication.CameraTest_ID:
//                    if (cameraIsKey) {
//                        checkTurnRed(str);
//                    }
//                    break;
            }
            if (str.contains("Test end: FAIL")) {
//                checkTurnRed(str);
                if (recorder != null) {
                    recorder.strFailureLog += str;
                    recorder.write();
                }
            }
            if (tvShow.getText().toString().length() > 5000)
                tvShow.setText("");
            tvShow.append(str);
            svLog.post(new Runnable() {
                @Override
                public void run() {
                    svLog.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        private void activateKeys(String str) {
            vibratorIsKey = false;
            batteryIsKey = false;
            btIsKey = false;
            nfcIsKey = false;
            sdIsKey = false;
            inandIsKey = false;
            wifiIsKey = false;
            videoIsKey = false;
            bklIsKey = false;
            cellularIsKey = false;
            bcrIsKey = false;
            cameraIsKey = false;
            sensorIsKey = false;
            gpsIsKey = false;
            flashIsKey = false;
            suspendIsKey = false;
            if (str.contains("Vibrator")) {
                vibratorIsKey = true;
            }
            if (str.contains("Battery")) {
                batteryIsKey = true;
            }
            if (str.contains("BT")) {
                btIsKey = true;
            }
            if (str.contains("NFC")) {
                nfcIsKey = true;
            }
            if (str.contains("SD")) {
                sdIsKey = true;
            }
            if (str.contains("iNAND")) {
                inandIsKey = true;
            }
            if (str.contains("Wifi")) {
                wifiIsKey = true;
            }
            if (str.contains("Video")) {
                videoIsKey = true;
            }
            if (str.contains("BKL")) {
                bklIsKey = true;
            }
            if (str.contains("3G")) {
                cellularIsKey = true;
            }
            if (str.contains("BCR")) {
                bcrIsKey = true;
            }
            if (str.contains("Camera")) {
                cameraIsKey = true;
            }
            if (str.contains("GPS")) {
                gpsIsKey = true;
            }
            if (str.contains("Flash")) {
                flashIsKey = true;
            }
            if (str.contains("Suspend")) {
                suspendIsKey = true;
            }
        }

        private void checkTurnRed(String str) {
            if (!isRed && str != null && str.contains("Test end: FAIL")) {
                rlBackground.setBackgroundColor(Color.RED);
                isRed = true;
            }
        }
    };
    public Messenger messenger = new Messenger(handler);

    private void sendMessage(String log) {
        if (messenger == null || !Validator.isString(log))
            return;

        Message message = Message.obtain();
        message.what = BISTApplication.COMMAND;
        message.obj = log;
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void saveLog(String log, String logFile) {
        File file = new File(recorder.strTestFolder, logFile);
        if (log == null || log.isEmpty()) {
            return;
        } else if (logFile == null) {
            return;
        } else if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs())
                return;
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            fileWriter.append(log);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void placeOldLogs() {
        File bistFolder = new File(BISTApplication.BASE_PATH);
        File oldLogFolder = new File(BISTApplication.OLD_LOG_PATH);
        if (!bistFolder.exists() || !bistFolder.isDirectory()) {
            return;
        } else {
            File[] logFolders = bistFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if (s.startsWith("20")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            if (logFolders.length > 0 && !oldLogFolder.exists()) {
                oldLogFolder.mkdirs();
            }
            for (File logFolder : logFolders) {
                if (logFolder.isDirectory()) {
                    logFolder.renameTo(new File(oldLogFolder, logFolder.getName()));
                    // TODO If there is another folder in old folder with the same name
                }
            }
        }

    }
}
