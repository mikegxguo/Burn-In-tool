package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Messenger;
import bom.mitac.bist.burnin.activity.CameraActivity;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClassLongTime;
import bom.mitac.bist.burnin.module.TestFactory;
import bom.mitac.bist.burnin.util.Rebooter;


/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-20
 * Time: 下午1:52
 */
public class CameraTest extends TestClassLongTime {
    public static final int FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public static boolean isCameraTestPassed;
    private int facing;
    private int times;
    private boolean deleteFile;

    public CameraTest(Messenger messenger, Activity activity, int facing, int times, boolean deleteFile) {
        super(messenger, activity);

        if (facing == FACING_BACK)
            this.id = BISTApplication.CameraTest_BACK_ID;
        else
            this.id = BISTApplication.CameraTest_FRONT_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.facing = facing;
        this.times = times;
        this.deleteFile = deleteFile;
    }

    @Override
    public boolean classSetup() {
        if (facing != FACING_BACK && facing != FACING_FRONT)
            return false;

        return true;
    }

    @Override
    public boolean testBegin() {

        if (facing == FACING_FRONT) {
            sendMessage("Test front-camera", true);
        } else {
            sendMessage("Test back-camera", true);
        }
        Intent intent = new Intent();
        intent.setClass(activity, CameraActivity.class);
        intent.putExtra(BISTApplication.CAMERA_FACING, facing);
        intent.putExtra(BISTApplication.CAMERA_SHOT_TIMES, times);
        intent.putExtra(BISTApplication.CAMERA_DELETE_FILE, deleteFile);
        activity.startActivity(intent);
        return true;
    }

    @Override
    public boolean testEnd() {
        isPassed = isCameraTestPassed;
        // add for stop test if get camera fail
//        if (!isPassed){
//        	TestFactory.SetEndtoTrue();
//        }
        //add for reboot if get camera fail
        if (!isPassed && Rebooter.isRebooterInstalled(activity)) {
            Rebooter.reboot(activity);
        }
        return true;
    }
}
