package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-19
 * Time: 下午6:27
 */
public class TemperatureTest extends TestClass {
    private float temperature;
    private SensorManager sensorMgr;
    private Sensor temSensor;

    private SensorEventListener temListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() == Sensor.TYPE_TEMPERATURE) {
                temperature = e.values[SensorManager.DATA_X];
            }
        }

        public void onAccuracyChanged(Sensor s, int accuracy) {
        }
    };

    public TemperatureTest(Messenger messenger, Activity activity, boolean isContinuous) {
        super(messenger, activity, isContinuous);
        this.id = BISTApplication.TemperatureTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
    }

    @Override
    public boolean classSetup() {
        sensorMgr = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        if (sensorMgr == null)
            return false;
        temSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        if (temSensor == null)
            return false;
        return true;
    }

    @Override
    public boolean testSetup() {
        if (sensorMgr == null || temListener == null || temSensor == null)
            return false;
        sensorMgr.registerListener(temListener, temSensor, SensorManager.SENSOR_DELAY_GAME);
        return true;
    }

    @Override
    public boolean testBegin() {
        sendMessage("Temperature: " + temperature + "'C", true);
        return true;
    }

    @Override
    public boolean testCleanup() {
        if (sensorMgr == null || temListener == null)
            return false;
        sensorMgr.unregisterListener(temListener);
        return true;
    }
}
