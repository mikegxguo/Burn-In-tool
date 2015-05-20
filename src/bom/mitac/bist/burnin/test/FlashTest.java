package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.os.Messenger;
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
 * Time: 下午6:37
 */
public class FlashTest extends TestClass {

    private File folder;

    private boolean keepTheDiffer;

    public FlashTest(Messenger messenger, Activity activity, int cycles, boolean keepTheDiffer) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.FlashTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.keepTheDiffer = keepTheDiffer;
    }

    @Override
    public boolean classSetup() {
        this.folder = new File(BISTApplication.BASE_PATH, "Temp");
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
        File file = new File(folder, "50M.txt");
        if (Validator.isFile(file) && file.length() == 50 * 1024 * 1024) {
            sendMessage("50MB is already existed", true);
            return true;
        }
        sendMessage("Creating 50MB file on the disk", true);
        if (FileManager.create(file, 50 * 1024)) {
            sendMessage("Succeed", true);
            return true;
        } else {
            sendMessage("Failed", true);
            return false;
        }
    }

    @Override
    public boolean testBegin() {
        boolean result = true;
        int[] time = new int[5];
        for (int i = 1; i <= 1; i++) {
            sendMessage("Copying", true);
//            sendMessage("Copying No." + i + " file", true);
            if (isStopped)
                break;
            File fromFile = new File(folder, "50M.txt");
            File toFile = new File(folder, TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_S_TYPE));
            if (fromFile.exists()) {
                time[i - 1] = (int) FileManager.copy(fromFile, toFile);
                if (time[i - 1] == -1) {
                    result = false;
                    continue;
                }

                if (Validator.isFile(toFile)) {
                    if (FileManager.compare(fromFile, toFile)) {
                        sendMessage("Success. 50M file Copying time: " + time[i - 1] + "ms", true);
                    } else {
                        result = false;
                        sendMessage("Failed. 50M file Copying time: " + time[i - 1] + "ms." + " The copy is not identified with original file", true);
                        if (keepTheDiffer) {
                            File errorFile = new File(toFile.toString() + "IamDiffer");
                            toFile.renameTo(errorFile);
                            sendMessage("The copy file has been saved to " + errorFile.toString(), true);
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
