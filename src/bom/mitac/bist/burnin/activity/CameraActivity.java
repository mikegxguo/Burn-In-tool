package bom.mitac.bist.burnin.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.test.CameraTest;
import bom.mitac.bist.burnin.util.AXCamera;
import bom.mitac.bist.burnin.util.Panel;
import bom.mitac.bist.burnin.util.Recorder;
import bom.mitac.bist.burnin.util.ScreenShot;
import bom.mitac.bist.burnin.util.TimeStamp;
import bom.mitac.bist.burnin.util.Validator;
import bom.mitac.bist.burnin.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-4-1
 * Time: 上午9:59
 */
public class CameraActivity extends Activity {

    private SurfaceHolder shCameraPreview;
    private AXCamera camera;
    private int facing;
    private int times;
    private boolean deleteFile;
    private Timer timer;
    private boolean isTesting;
    private SurfaceView svCamera;
    private Panel mPanel;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_camera);

        facing = getIntent().getIntExtra(BISTApplication.CAMERA_FACING, CameraTest.FACING_BACK);
        times = getIntent().getIntExtra(BISTApplication.CAMERA_SHOT_TIMES, 0);
        deleteFile = getIntent().getBooleanExtra(BISTApplication.CAMERA_DELETE_FILE, true);

        svCamera = (SurfaceView) findViewById(R.id.sv_camera);
        if (svCamera.getVisibility() != View.VISIBLE) {
            saveLog("Failed");
            CameraTest.isCameraTestPassed = false;
            this.finish();
        }
        shCameraPreview = svCamera.getHolder();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isTesting = true;
                camera = AXCamera.getInstance(new File(Recorder.getInstance().strTestFolder));
                boolean isOpened = camera.open(facing);
                if (!isOpened) {
                    saveLog("Open camera failed");
                    CameraTest.isCameraTestPassed = false;
                    stopTest();
                    return;
                }
                saveLog("Camera opened");

                SystemClock.sleep(4000);
                if (!isTesting) {
                    return;
                }
                boolean previewed;
                if (facing == CameraTest.FACING_FRONT) {
//                    previewed = camera.preview(shCameraPreview, 90);
                    previewed = camera.preview(shCameraPreview, 0);
                } else {
                    previewed = camera.preview(shCameraPreview, 0);
                }
                if (!previewed) {
                    stopTest();
                    return;
                }
                saveLog("Camera previewed");
                for (int i = 0; i < times; i++) {
                    SystemClock.sleep(3000);
                    if (!isTesting) {
                        return;
                    }
                    
                    saveLog("Take a picture");
                    camera.shot();
                    SystemClock.sleep(2000);
          
                    if (!isTesting) {
                        return;
                    }

//                  File picdir = new File (BISTApplication.PIC_PATH + "/" + System.currentTimeMillis() + ".png");
//                  ScreenShot.shoot(CameraActivity.this, picdir);
//                  SystemClock.sleep(2000);
//                  new MyThread().start();  
         
                    File file = camera.getFile();
                    if (Validator.isFile(file)) {
                        if (deleteFile)
                            file.delete();
                    } else {
                        saveLog("Take picture failed");
                        CameraTest.isCameraTestPassed = false;
                        stopTest();
                        return;
                    }
                }

                CameraTest.isCameraTestPassed = true;
                stopTest();

            }
        }, 0);
    }

    @Override
    protected void onDestroy() {
        if (isTesting)
            stopTest();
        super.onDestroy();
    }

    private void stopTest() {
        isTesting = false;
        if (camera.isOpened()) {
            saveLog("Close camera");
            camera.close();
            saveLog("Closed");
        }
        finish();
    }

    private void saveLog(String log) {
        log = TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_L_TYPE) + " |" + BISTApplication.ID_NAME.get(BISTApplication.CameraTest_BACK_ID) + "| " + log + "\r\n";
        CameraTest.saveLog(log);
    }
	
	public class MyThread extends Thread {  
		        public void run() {
		        	int width = getWindowManager().getDefaultDisplay().getWidth();
            		int height = getWindowManager().getDefaultDisplay().getHeight();
                	Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
                	canvas = new Canvas(bitmap);
		        	canvas = shCameraPreview.lockCanvas();
		        	canvas.drawBitmap(bitmap, width, height, null);
                	File picfile = new File("/mnt/sdcard/BIST/PIC" + "/" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(picfile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.e("Panel", "FileNotFoundException", e);
                    } catch (IOException e) {
                        Log.e("Panel", "IOEception", e);
                    }
                    shCameraPreview.unlockCanvasAndPost(canvas);     	
		        }  
	    }  

}
