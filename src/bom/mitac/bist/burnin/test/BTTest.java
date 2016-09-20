package bom.mitac.bist.burnin.test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Messenger;
import android.os.SystemClock;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestClass;


import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-19
 * Time: 上午9:57
 */
public class BTTest extends TestClass {

    private int btDevices;
    private BluetoothAdapter btAdapter;
    private BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                btDevices++;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                sendMessage(device.getName() + "  " + device.getAddress(), true);
            }
        }
    };

    public BTTest(Messenger messenger, Activity activity, Boolean isContinuous) {
        super(messenger, activity, isContinuous);
        this.id = BISTApplication.BTTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    public BTTest(Messenger messenger, Activity activity, int cycles) {
        super(messenger, activity, cycles);
        this.id = BISTApplication.BTTest_ID;
//        this.logFile = new File(BISTApplication.LOG_PATH, BISTApplication.ID_NAME.get(id) + ".txt");
//        this.logFile = new File(BISTApplication.LOG_PATH, "BIST_Testlog.txt");
    }

    private boolean enableBT() {
        if (btAdapter == null) {
            sendMessage("Enable the bluetooth failed", true);
            return false;
        }

        for (int i = 0; i < 5; i++) {
            if (!btAdapter.isEnabled()) {
                sendMessage("The bluetooth is not enabled, enabling the bluetooth", true);
                btAdapter.enable();
                SystemClock.sleep(5000);
            } else {
                sendMessage("The bluetooth is enabled", true);
                return true;
            }
        }
        sendMessage("Enable the bluetooth failed", true);
        return false;

//        while(true)    {
//            if (!btAdapter.isEnabled()) {
//                sendMessage("The bluetooth is not enabled, enabling the bluetooth", true);
//                btAdapter.enable();
//                SystemClock.sleep(5000);
//                if (isStopped) {
//                    sendMessage("Enable the bluetooth failed", true);
//                    return false;
//                }
//            } else {
//                sendMessage("The bluetooth is enabled", true);
//                break;
//            }
//        }
//
//        return true;
    }

    private void makeDeviceDiscoverable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        activity.startActivity(intent);
    }

    private boolean discoveryDevices() {
        if (btAdapter == null) {
            sendMessage("Open searching process failed", true);
            return false;
        }
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
            SystemClock.sleep(5000);
            if (isStopped) {
                return true;
            }
        }

        sendMessage("Searching the bluetooth devices nearby", true);
        btDevices = 0;
        btAdapter.startDiscovery();
        SystemClock.sleep(15000);

//        if (btAdapter.isDiscovering()) {
        if (btDevices == 0) {
            sendMessage("No bluetooth devices found", true);
            return false;
        } else {
            sendMessage(String.valueOf(btDevices) + " bluetooth devices found", true);
            return true;
        }
//        } else {
//            sendMessage("Open searching process failed", true);
//            return false;
//        }
    }

    private boolean closeDevice() {
        if (btAdapter == null) {
            sendMessage("Close the bluetooth failed", true);
            return false;
        }

        while (true) {
            if (btAdapter.isEnabled()) {
                sendMessage("Closing the bluetooth", true);
                btAdapter.disable();
                SystemClock.sleep(5000);
                if (isStopped) {
                    sendMessage("Close the bluetooth failed", true);
                    return false;
                }
            } else {
                sendMessage("The bluetooth is closed", true);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean classSetup() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
            return false;
        btAdapter.enable();
        SystemClock.sleep(2000);
        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            makeDeviceDiscoverable();
        }
        for (int i = 0; i < 100; i++) {
            SystemClock.sleep(100);
            if (btAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean testSetup() {
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(btReceiver, intentFilter);
        return true;
    }

    @Override
    public boolean testBegin() {
//        if (enableBT()) {
//            for (int i = 0; i < 3; i++) {
//                if (discoveryDevices())
//                    return true;
//            }
//            return false;
//        } else {
//            return false;
//        }
        if (enableBT()) {
//            if (discoveryDevices()) {
//                return closeDevice();
//            } else {
//                closeDevice();
//                return false;
//            }
            return closeDevice();
        } else {
            return false;
        }
    }

    @Override
    public boolean testCleanup() {
        activity.unregisterReceiver(btReceiver);
        return true;
    }
}
