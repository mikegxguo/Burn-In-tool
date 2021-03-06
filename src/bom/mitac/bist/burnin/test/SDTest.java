package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.os.Messenger;
import android.util.Log;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.util.FileManager;
import bom.mitac.bist.burnin.util.TimeStamp;
import bom.mitac.bist.burnin.util.Validator;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-12
 * Time: 6:37PM
 */
public class SDTest extends TestClass {

    private File folder;

    private boolean keepTheDiffer;
    private Context context;
    private String EXT_SDCARD_PATH;

    public SDTest(Messenger messenger, Activity activity, boolean isContinuous, boolean keepTheDiffer) {
        super(messenger, activity, isContinuous);
        this.id = BISTApplication.SDTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
        context = activity;

        this.keepTheDiffer = keepTheDiffer;
    }

    public SDTest(Messenger messenger, Activity activity, int cycles, boolean keepTheDiffer) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.SDTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.keepTheDiffer = keepTheDiffer;
    }

    @Override
    public boolean classSetup() {
        //TO DO: If the platform is Android 6.0, then we need the code below to run this case;
        //TO DO: If the platform is Android 4.2, then we need another code;
        File[] fs = context.getExternalFilesDirs(null);
        String extPath = null;
        // Index 0: the internal storage, /sdcard/
        // Index 1: the real external..., /storage/xxxx-xxxx
        if (fs != null && fs.length >= 2) {
            extPath = fs[1].getPath();
            Log.d("feong",extPath);
        }

        EXT_SDCARD_PATH = extPath;
        this.folder = new File(EXT_SDCARD_PATH, "HEAVY_LOADING_TEST");
//        this.folder = new File(BISTApplication.EXT_SDCARD_PATH, "HEAVY_LOADING_TEST");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return Validator.isDirectory(folder);
    }

    @Override
    public boolean testSetup() {
        if (!folder.exists()) {
            return false;
        }
        for (int i = 1; i <= 10; i++) {
            if (isStopped) {
                break;
            }
            File file = new File(folder, i + "M.txt");
            if (Validator.isFile(file) && file.length() == i * 1024 * 1024) {
//                sendMessage("Orignal files are already existed", true);
            } else {
                sendMessage("Creating " + i + "MB file on the disk", true);
                if (FileManager.create(file, i * 1024)) {
                    sendMessage("Succeed", true);
                } else {
                    sendMessage("Failed", true);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean testBegin() {
        boolean result = true;
        int[] time = new int[10];
        for (int i = 1; i <= 10; i++) {
            if (isStopped)
                break;
            File fromFile = new File(folder, i + "M.txt");
            File toFile = new File(folder, "_" + i + "M.txt");
            if (fromFile.exists()) {
                time[i - 1] = (int) FileManager.copy(fromFile, toFile);
                if (time[i - 1] == -1) {
                    result = false;
                    continue;
                }

                if (Validator.isFile(toFile)) {
                    if (FileManager.compare(fromFile, toFile)) {
                        sendMessage("Success. " + i + "M file Copying time: " + time[i - 1] + "ms", true);
                    } else {
                        result = false;
                        sendMessage("Failed. " + i + "M file Copying time: " + time[i - 1] + "ms." + " The copy is not identified with original file", true);
                        if (keepTheDiffer) {
                            File errorFile = new File(toFile.getParent(), TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_S_TYPE) + "IamDiffer");
                            toFile.renameTo(errorFile);
                            sendMessage("The copy file has been saved to "+errorFile.toString(), true);
                        }
                    }
                    toFile.delete();
                }

            } else {
                continue;
            }
        }
        return result;
    }

}
