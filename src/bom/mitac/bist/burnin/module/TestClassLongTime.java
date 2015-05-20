package bom.mitac.bist.burnin.module;

import android.app.Activity;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import bom.mitac.bist.burnin.util.Recorder;
import bom.mitac.bist.burnin.util.TimeStamp;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-31
 * Time: 下午6:10
 */
public abstract class TestClassLongTime implements StandardTestMethod {

    protected boolean isRunning;
    protected boolean isPassed;
    protected int passTime;
    protected int failedTime;
    protected int id;
    protected Messenger messenger;
    protected Activity activity;
    protected static File logFile;

    public TestClassLongTime(Messenger messenger, Activity activity) {
        this.messenger = messenger;
        this.activity = activity;
        this.logFile = new File(Recorder.getInstance().strTestFolder, BISTApplication.LOG_NAME);
    }

    public static void saveLog(String log) {
        if (log == null || log.isEmpty()) {
            return;
        } else if (logFile == null) {
            return;
        } else if (!logFile.getParentFile().exists()) {
            if (!logFile.getParentFile().mkdirs())
                return;
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(logFile, true);
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

    protected void sendMessage(String log, boolean saveLog) {
        if (messenger == null)
            return;
        log = TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_L_TYPE) + " |" + BISTApplication.ID_NAME.get(id) + "| " + log + "\r\n";
        log(log);
        Message message = Message.obtain();
        message.what = id;
        message.obj = log;
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (saveLog) {
            saveLog(log);
        }
    }

    @Override
    public void start() {
        if (isRunning) {
            sendMessage("Error! Last test is still running", true);
            return;
        }
        isPassed = true;
        isRunning = true;

        passTime = Recorder.getInstance().getPassTimes(id);
        failedTime = Recorder.getInstance().getFailTimes(id);
        Log.d("feong", "Test begin:" + toString() + " " + passTime + "/" + failedTime);
        sendMessage("Test begin", true);
        if (!testSetup()) {
            failedTime++;
            isPassed = false;
            sendMessage("Setup failed", true);
        }

        isPassed = testBegin();
    }

    @Override
    public void stop() {
        testEnd();
//        isPassed = isPassed && testCleanup();         // Maybe change isPassed in testCleanup method
        testCleanup();
        if (isPassed) {
            passTime++;
        } else {
            failedTime++;
        }
        sendMessage("Test end: " + (isPassed ? "PASS" : "FAIL") + " " + passTime + "/" + failedTime, true);
        Recorder.getInstance().setPassTime(id, passTime);
        Recorder.getInstance().setFailTime(id, failedTime);
        isRunning = false;
    }

    public void log(String str) {
        Log.d("feong", str);
    }

    @Override
    public String toString() {
        return BISTApplication.ID_NAME.get(id);
    }

    @Override
    public boolean testSetup() {
        return true;
    }

    @Override
    public boolean testCleanup() {
        return true;
    }

    @Override
    public boolean classCleanup() {
        return true;
    }
}
