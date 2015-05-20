package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Messenger;
import android.os.SystemClock;
import android.view.View;
import android.widget.VideoView;
import bom.mitac.bist.burnin.activity.VideoActivity;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;
import bom.mitac.bist.burnin.module.TestClassLongTime;
import bom.mitac.bist.burnin.util.Rebooter;
import bom.mitac.bist.burnin.util.Validator;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-19
 * Time: 下午3:13
 */
public class VideoTest extends TestClassLongTime {

    public static boolean isVideoTestPassed = true;

    private String videoPath;
    private boolean adjustVolume;
    private long videoTime;

    public VideoTest(Messenger messenger, Activity activity, boolean adjustVolume, String videoPath, long videoTime) {
        super(messenger, activity);
        this.id = BISTApplication.VideoTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");

        this.adjustVolume = adjustVolume;
        this.videoPath = videoPath;
        this.videoTime = videoTime;
    }

    @Override
    public boolean classSetup() {
        if (!Validator.isString(videoPath))
            return false;
        File media = new File(videoPath);
        return Validator.isFile(media);
    }

    @Override
    public boolean testBegin() {
        Intent intent = new Intent();
        intent.setClass(activity, VideoActivity.class);
        intent.putExtra(BISTApplication.VIDEO_PATH, videoPath);
        intent.putExtra(BISTApplication.VIDEO_TIME, videoTime);
        intent.putExtra(BISTApplication.VIDEO_VOLUME, adjustVolume);
        activity.startActivity(intent);
        return true;
    }

    @Override
    public boolean testEnd() {
        isPassed =  isVideoTestPassed;
        if (!isPassed && Rebooter.isRebooterInstalled(activity)) {
            Rebooter.reboot(activity);
        }
        return true;
    }

}
