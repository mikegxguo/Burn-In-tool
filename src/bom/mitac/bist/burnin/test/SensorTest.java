package bom.mitac.bist.burnin.test;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Messenger;
import android.os.SystemClock;
import android.provider.Settings;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


public class SensorTest extends TestClass {

    private SensorManager sensorMgr;
    private SensorEventListener lsn;
    private Sensor lightSensor, oriSensor, GSensor;
    private float x1, y1, z1;
    private float x2, y2, z2;
    private float x3;

    public SensorTest(Messenger messenger, Activity activity) {
        super(messenger, activity);
        this.id = BISTApplication.SensorTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    @Override
    public boolean classSetup() {
        if (sensorMgr != null)
            return true;
        sensorMgr = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        lsn = new SensorEventListener() {
            public void onSensorChanged(SensorEvent e) {
                if (e.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    x1 = e.values[SensorManager.DATA_X];
                    y1 = e.values[SensorManager.DATA_Y];
                    z1 = e.values[SensorManager.DATA_Z];
                } else if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    x2 = e.values[SensorManager.DATA_X];
                    y2 = e.values[SensorManager.DATA_Y];
                    z2 = e.values[SensorManager.DATA_Z];
                } else if (e.sensor.getType() == Sensor.TYPE_LIGHT) {
                    x3 = e.values[SensorManager.DATA_X];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        GSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(lsn, GSensor, SensorManager.SENSOR_DELAY_GAME);
        oriSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorMgr.registerListener(lsn, oriSensor, SensorManager.SENSOR_DELAY_GAME);
        lightSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorMgr.registerListener(lsn, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean testBegin() {

        SystemClock.sleep(2000);
        sendMessage("Seneor Type: Compass\n" +
                "X:" + String.valueOf(x1) + "\n" +
                "Y:" + String.valueOf(y1) + "\n" +
                "Z:" + String.valueOf(z1) + "\n", true);
        sendMessage("Seneor Type: G-Sensor\n" +
                "X:" + String.valueOf(x2) + "\n" +
                "Y:" + String.valueOf(y2) + "\n" +
                "Z:" + String.valueOf(z2) + "\n", true);
        sendMessage("Seneor Type: Light Sensor\n" +
                "X:" + String.valueOf(x3) + "\n", true);
//        setMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//        sendMessage("Sensor test end!", true);
//        sensorMgr.unregisterListener(lsn);
        return true;
    }

    private void setMode(int mode) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }
}
