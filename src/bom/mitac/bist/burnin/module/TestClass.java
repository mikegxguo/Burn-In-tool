package bom.mitac.bist.burnin.module;

import android.app.Activity;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import bom.mitac.bist.burnin.util.Recorder;
import bom.mitac.bist.burnin.util.TimeStamp;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-12
 * Time: 下午6:10
 */
public abstract class TestClass implements StandardTestMethod {

    protected boolean isRunning;
    protected boolean isStopped;
    protected boolean isContinuous;
    protected boolean isPassed;
    protected int cycles;
    protected int passTime;
    protected int failedTime;
    protected int id;
    protected Messenger messenger;
    protected Activity activity;
    protected static File logFile;

    public TestClass(Messenger messenger, Activity activity) {
        this.messenger = messenger;
        this.activity = activity;
        this.isContinuous = false;
        this.cycles = 1;
        this.logFile = new File(Recorder.getInstance().strTestFolder, BISTApplication.LOG_NAME);
    }

    public TestClass(Messenger messenger, Activity activity, int cycles) {
        this(messenger, activity);
        this.isContinuous = false;
        this.cycles = cycles;
    }

    public TestClass(Messenger messenger, Activity activity, boolean isContinuous) {
        this(messenger, activity);
        if (isContinuous) {
            this.isContinuous = true;
        } else {
            this.isContinuous = false;
            this.cycles = 1;
        }
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
        isStopped = false;
        isRunning = true;

        passTime = Recorder.getInstance().getPassTimes(id);
        failedTime = Recorder.getInstance().getFailTimes(id);
        Log.d("feong", "Test begin:" + toString() + " " + passTime + "/" + failedTime);
        sendMessage("Test begin", true);
        if (!testSetup()) {
            failedTime++;
            isPassed = false;
            isStopped = true;
            sendMessage("Setup failed", true);
        }

        for (int i = 0; i < cycles || isContinuous; i++) {
            if (isStopped) {
                break;
            }
            if (testBegin()) {
                passTime++;
            } else {
                failedTime++;
                isPassed = false;
            }
            SystemClock.sleep(1000);
        }

//        isPassed = isPassed && testCleanup();         // Maybe change isPassed in testCleanup method
        testCleanup();
        sendMessage("Test end: " + (isPassed ? "Result PASS!" : "Result FAIL!") + " PASS= " + passTime + " FAIL=" + failedTime, true);
        Recorder.getInstance().setPassTime(id, passTime);
        Recorder.getInstance().setFailTime(id, failedTime);
        isRunning = false;
    }

//    public boolean start() {
//        isPassed = true;
//        isStopped = false;
////        log("classSetup");
////        if (!classSetup()) {
////            failedTime++;
////            isPassed = false;
////            return isPassed;
////        }
//
//        for (int i = 0; i < cycles || isContinuous; i++) {
//            if (isStopped) {
//                break;
//            }
////            log("testSetup");
//            if (!testSetup()) {
//                isPassed = false;
//                SystemClock.sleep(3000);
//                continue;
//            }
//            if (isStopped) {
////                log("testCleanup");
//                isPassed = isPassed && testCleanup();
//                break;
//            }
//            sendMessage("Test begin", true);
////            log("testBegin");
//            if (isPassed && testBegin()) {
//                passTime++;
//                isPassed = true;
//            } else {
//                failedTime++;
//                isPassed = false;
//            }
//            if (isStopped) {
////                log("testCleanup");
//                isPassed = isPassed && testCleanup();
//                break;
//            }
////            log("testCleanup");
//            isPassed = isPassed && testCleanup();
//            SystemClock.sleep(1000);
//        }
////        log("classCleanup");
////        if (!classCleanup()) {
////            isPassed = false;
////        }
////        log("finished");
//        return isPassed;
//    }

    @Override
    public void stop() {
        isStopped = true;
        testEnd();
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
    public boolean testEnd() {
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
