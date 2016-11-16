package bom.mitac.bist.burnin.util;

import android.os.Environment;
import android.util.Log;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-4-28
 * Time: 11:09AM
 */
@Root
public class Recorder {
    public static final int MAX_RECORD = 50;
    private static final File DEFAULT_FILE = new File(Environment.getExternalStorageDirectory(), "recorder.xml");
    private static Recorder instance;

    @Element
    public boolean isTesting;
    @Element
    public long startTime;
    @Element
    public int cycle;
    @Element(required = false)
    public String strFailureLog;
    @Element(required = false)
    public String strTestFolder;
    @Element(required = false)
    public String strTestCondition;
    @Element
    public int indexPass;
    @Element
    public int indexFail;
    @Element
    public int[] passTime;
    @Element
    public int[] failTime;
    @Element
    public int rebootTimes;
    @Element
    public long rebootMoment;
    @Element
    public int resetTimes;
    @Element
    public int rebootDelay;
    @Element
    public boolean BCRCheck;

    private Recorder() {
        init();
    }

    public static Recorder getInstance() {
        if (instance == null) {
            instance = new Recorder();
        }
        return instance;
    }

    public void init() {
    	BCRCheck = true;
        startTime = 0;
        cycle = 0;
        isTesting = false;
        rebootDelay = 5;
        resetTimes = -1;
        strFailureLog = "";
        strTestFolder = null;
        indexPass = 0;
        indexFail = 0;
        passTime = new int[MAX_RECORD];
        failTime = new int[MAX_RECORD];
    }

    public synchronized void write() {
        Serializer serializer = new Persister();
        try {
            serializer.write(this, DEFAULT_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        if (Validator.isFile(DEFAULT_FILE))
            DEFAULT_FILE.delete();
    }

    public static Recorder read() {
//        if (instance != null) {
//
//            Log.d("feong", "1"+instance.isTesting);
//            return instance;
//        }

        if (DEFAULT_FILE.exists()) {
            Serializer serializer = new Persister();
            try {
                instance = serializer.read(Recorder.class, DEFAULT_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            instance = new Recorder();
        }

        return instance;
    }

    public void setPassTime(int id, int times) {
        for (int i = 0; i < indexPass; i++) {
            if (passTime[i] == id && (i + 1) < MAX_RECORD) {
                passTime[i + 1] = times;
                write();
                return;
            }
        }
        if ((indexPass + 1) < MAX_RECORD) {
            passTime[indexPass++] = id;
            passTime[indexPass++] = times;
        }
        write();
    }

    public int getPassTimes(int id) {
        for (int i = 0; i < indexPass; i++) {
            if (passTime[i] == id) {
                return passTime[i + 1];
            }
        }
        return 0;
    }

    public void setFailTime(int id, int times) {
        for (int i = 0; i < indexFail; i++) {
            if (failTime[i] == id && (i + 1) < MAX_RECORD) {
                failTime[i + 1] = times;
                write();
                return;
            }
        }
        if ((indexFail + 1) < MAX_RECORD) {
            failTime[indexFail++] = id;
            failTime[indexFail++] = times;
        }
        write();
    }

    public int getFailTimes(int id) {
        for (int i = 0; i < indexFail; i++) {
            if (failTime[i] == id) {
                return failTime[i + 1];
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "isTesting = " + isTesting
                + " startTime = " + startTime
                + " cycle = " + cycle
                + " strFailureLog = " + strFailureLog
                + " strTestFolder = " + strTestFolder
                + " strTestCondition = " + strTestCondition
                + " rebootTimes = " + rebootTimes
                + " resetTimes = " + resetTimes;
    }
}
