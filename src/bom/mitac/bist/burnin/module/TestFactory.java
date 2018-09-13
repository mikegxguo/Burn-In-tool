package bom.mitac.bist.burnin.module;

import android.app.Activity;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import bom.mitac.bist.burnin.test.*;
import bom.mitac.bist.burnin.util.Recorder;


import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static bom.mitac.bist.burnin.module.BISTApplication.TEST_NAME.*;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-20
 * Time: 下午3:30
 */
public class TestFactory {
    private static TestFactory instance;

    private List<String> configs;
    private Messenger messenger;
    private Activity activity;

    private TestAtTheSameTime beginList;
    private TestAtTheSameTime endList;
    // Reboot test is a special Test
    private StandardTestMethod rebootTest;
    private boolean doRebootTest;
    private int rebootIntervalCycles;
    private int rebootTimesPerCycle;


    private String testTitle;
    //    private static int baseResetTic;
//    private static int tic = -1;
    private int cycleTime;
    private int totalCycles;
    //private int totalTime;
    public static int rebootTime; //get run reboot test time in minutes
   
    private static final int MAX_KEYS = 30;
    List<String> strKeys = new ArrayList<String>(MAX_KEYS);
    List<String> strKeyTest = new ArrayList<String>(MAX_KEYS);
    private boolean containKey;
    private Recorder recorder;

    public static boolean end;
    private Timer timer;
    private BISTTimerTask bistTimerTask;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    private TestFactory(Messenger messenger, Activity activity) {
        this.messenger = messenger;
        this.activity = activity;
        this.recorder = Recorder.getInstance();
        this.beginList = new TestAtTheSameTime();
        this.endList = new TestAtTheSameTime();
    }

    public static TestFactory getInstance(Messenger messenger, Activity activity) {
        if (instance == null)
            instance = new TestFactory(messenger, activity);
        return instance;
    }

    public String readConfig(String filePath) {
        Log.d("feong", "readConfig");
        if (filePath == null || filePath.isEmpty()) {
            return null;
        } else {
            File configFile = new File(filePath);
            if (!configFile.exists())
                return null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(configFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader d = new BufferedReader(isr);
                configs = new ArrayList<String>(200);
                for (String temp = d.readLine(); temp != null; temp = d.readLine()) {
                    if (temp.startsWith("//") || temp.isEmpty())
                        continue;
                    String[] strCommand = temp.split("\\s{1,}");
                    for (String temp2 : strCommand) {
                        configs.add(temp2);
                    }
                }
                // TODO BCR
//                if (configs.contains("BCR")) {
//                    BCRManager.getInstance(activity);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (configs.size() < 4) {
            return null;
        }
        testTitle = configs.get(0);
        cycleTime = Integer.valueOf(configs.get(2));
        totalCycles = Integer.valueOf(configs.get(3));
        rebootTime = Integer.valueOf(configs.get(4));
        return testTitle;
    }

    private void findKey() {
        int index = configs.indexOf("Key");
        if (index != -1) {
            containKey = true;
            for (int i = 0; i < MAX_KEYS; i++) {
                String key = configs.get(++index);
                if (key.equals("Key")) {
                    break;
                } else {
                    strKeys.add(key);
                }
            }
        }
        if (strKeyTest.size() > 0)
            sendMessage(BISTApplication.KEY, "KEY: " + strKeyTest.toString());
    }

    public boolean findCase(int id) {
        int index = -1;
        String name = BISTApplication.ID_NAME.get(id);
        //Log.d("feong", "findCase:" + name);
        index = configs.indexOf(name);
//        if (name.equals(BISTApplication.CellularTest_ID)) {
//            index = configs.indexOf("3G");
//        } else {
//            index = configs.indexOf(name);
//        }
        if (index < 0) {
            return false;
        } else {
//            if (containKey && strKeys.contains(configs.get(index - 1))) {
//                strKeyTest.add(configs.get(index));
//            }
            String firstPara = configs.get(++index);
            String secondPara = configs.get(++index);
            String thirdPara;
            int startTime = Integer.valueOf(firstPara);
            int endTime = -1;
            int cycles = -1;
            int battLevel = 0;
            boolean isContinuous = false;
            StandardTestMethod test = null;
            if (!secondPara.equals("na")) {
                endTime = Integer.valueOf(secondPara);
                // The timer is from 0 to 'cycleTime-1', so the tic will never go to 'cycleTime'
                if (endTime == cycleTime) {
                    endTime--;
                }
            }
            switch (valueOf(name.toUpperCase())) {
                case CAMERA:
                    // TODO Camera
                    thirdPara = configs.get(++index);
                    int facing = CameraTest.FACING_BACK;
                    if (thirdPara.toLowerCase().equals("front")) {
                        facing = CameraTest.FACING_FRONT;
                    }
                    int times = Integer.valueOf(configs.get(++index));
                    boolean delete = Boolean.valueOf(configs.get(++index));
                    test = new CameraTest(messenger, activity, facing, times, delete);
                    // JCamera
//                    int facing = Integer.valueOf(configs.get(++index));
//                    boolean delete = Boolean.valueOf(configs.get(++index));
//                    test = new JCameraTest(messenger, activity, facing, delete);
                    break;

                case NFC:
                    if (endTime == -1) {
                        cycles = Integer.valueOf(configs.get(++index));
                        test = new NFCTest(messenger, activity, cycles);
                    } else {
                        isContinuous = Boolean.valueOf(configs.get(++index));
                        test = new NFCTest(messenger, activity, isContinuous);
                    }
                    break;
                case BCR:
                    if (endTime == -1) {
                        cycles = Integer.valueOf(configs.get(++index));
                        test = new BCRTest(messenger, activity, cycles);
                    } else {
                        isContinuous = Boolean.valueOf(configs.get(++index));
                        test = new BCRTest(messenger, activity, isContinuous);
                    }
                    break;
                case CELLULAR:
                    thirdPara = configs.get(++index);
                    if (thirdPara.toLowerCase().equals("imei")) {
                        cycles = Integer.valueOf(configs.get(++index));
                        test = new IMEITest(messenger, activity, cycles);
                    } else if (thirdPara.toLowerCase().equals("ftp")) {
                        cycles = Integer.valueOf(configs.get(++index));
                        String host = configs.get(++index);
                        String user = configs.get(++index);
                        String password = configs.get(++index);
                        test = new CellularTest(messenger, activity, cycles, host, user, password);
                    }
                    configs.remove("3G");
                    break;
                case WIFI:
                    thirdPara = configs.get(++index);
                    if (thirdPara.toLowerCase().equals("ping")) {
                        cycles = Integer.valueOf(configs.get(++index));
                        String site = configs.get(++index);
                        String ssid1 = configs.get(++index);
                        String password = configs.get(++index);
                        //String ssid3 = configs.get(++index);
                        //test = new PingTest(messenger, activity, cycles, site, ssid1, ssid2, ssid3);
                        test = new PingTest(messenger, activity, cycles, site, ssid1, password);
                    } else if (thirdPara.toLowerCase().equals("ftp")) {


                        if (endTime == -1) {
                            cycles = Integer.valueOf(configs.get(++index));
                            String host = configs.get(++index);
                            String user = configs.get(++index);
                            String password = configs.get(++index);
                            test = new WIFITest(messenger, activity, cycles, host, user, password);
                        } else {
                            isContinuous = Boolean.valueOf(configs.get(++index));
                            String host = configs.get(++index);
                            String user = configs.get(++index);
                            String password = configs.get(++index);
                            test = new WIFITest(messenger, activity, isContinuous, host, user, password);
                        }
                    }
                    break;
                case INAND:
                    if (endTime == -1) {
                        cycles = Integer.valueOf(configs.get(++index));
                        boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                        test = new INANDTest(messenger, activity, cycles, keepTheDiffer);
                    } else {
                        isContinuous = Boolean.valueOf(configs.get(++index));
                        boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                        test = new INANDTest(messenger, activity, isContinuous, keepTheDiffer);
                    }
                    break;
                case SD:
                    if (endTime == -1) {
                        cycles = Integer.valueOf(configs.get(++index));
                        boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                        test = new SDTest(messenger, activity, cycles, keepTheDiffer);
                    } else {
                        isContinuous = Boolean.valueOf(configs.get(++index));
                        boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                        test = new SDTest(messenger, activity, isContinuous, keepTheDiffer);
                    }
                    break;
                case USB:
                    if (endTime == -1) {
                        cycles = Integer.valueOf(configs.get(++index));
                        boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                        test = new USBTest(messenger, activity, cycles, keepTheDiffer);
                    } else {
                        isContinuous = Boolean.valueOf(configs.get(++index));
                        boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                        test = new USBTest(messenger, activity, isContinuous, keepTheDiffer);
                    }
                    break;
                case FLASH:
                    cycles = Integer.valueOf(configs.get(++index));
                    boolean keepTheDiffer = Boolean.valueOf(configs.get(++index));
                    test = new FlashTest(messenger, activity, cycles, keepTheDiffer);
                    break;
                case VIDEO:
                    boolean adjustVolume = Boolean.valueOf(configs.get(++index));
                    String videoPath = configs.get(++index);
                    // reduce 5 seconds for playing
                    // TODO Video
                    test = new VideoTest(messenger, activity, adjustVolume, videoPath, (endTime - startTime - 5));
                    // JVideo
//                    test = new JVideoTest(messenger, activity, adjustVolume, videoPath);
                    break;
                case BT:
                    if (endTime == -1) {
                        cycles = Integer.valueOf(configs.get(++index));
                        test = new BTTest(messenger, activity, cycles);
                    } else {
                        isContinuous = Boolean.valueOf(configs.get(++index));
                        test = new BTTest(messenger, activity, isContinuous);
                    }
                    break;
                case GPS:
                    int checkTime = endTime - startTime;
                    thirdPara = configs.get(++index);
                    if (thirdPara.toLowerCase().contains("hot")) {
                        test = new GPSTest(messenger, activity, GPSTest.TYPE.HOT_START, checkTime);
                    } else if (thirdPara.toLowerCase().contains("warm")) {
                        test = new GPSTest(messenger, activity, GPSTest.TYPE.WARM_START, checkTime);
                    } else if (thirdPara.toLowerCase().contains("cold")) {
                        test = new GPSTest(messenger, activity, GPSTest.TYPE.COLD_START, checkTime);
                    } else {
                        // It is nmea test
                        cycles = Integer.valueOf(configs.get(++index));
                        test = new NMEATest(messenger, activity, cycles);
                    }
                    break;
                case TEMPERATURE:
                    isContinuous = Boolean.valueOf(configs.get(++index));
                    test = new TemperatureTest(messenger, activity, isContinuous);
                    break;
                case VIBRATOR:
                    test = new VibratorTest(messenger, activity);
                    break;
                case BKL:
                    test = new BKLTest(messenger, activity);
                    break;
                case SENSOR:
                    test = new SensorTest(messenger, activity);
                    break;
                case BATTERY:
                    battLevel = Integer.valueOf(configs.get(++index));
                    test = new BatteryTest(messenger, activity, battLevel);
                    break;
                case SUSPEND:
                    // Keep this case to be the last one
                    test = new SuspendTest(messenger, activity, (endTime - startTime));
//                    baseResetTic = startTime;
                    endTime--;
                    break;
                case REBOOT:
                    test = new RebootTest(messenger, activity);
                    doRebootTest = true;
                    rebootIntervalCycles = startTime;
                    rebootTimesPerCycle = endTime;
                    rebootTest = test;
                    startTime = -1;
                    endTime = -1;
                    break;
                default:
                    return false;
            }
            Log.d("feong", "Add to list(begin): "+test.toString()+" time stamp:"+startTime);
            beginList.add(startTime, test);
            if (endTime != -1) {
                Log.d("feong", "Add to list(end): "+test.toString()+" time stamp:"+endTime);
                endList.add(endTime, test);
            }
            // there are many case for Camera, gps, 3g, wifi test, so remove it after it has added.
            configs.remove(name);
            return true;
        }
    }

    /**
     * Should running in a thread
     *
     * @return
     */
    public boolean isReady(Map<String, Boolean> cases) {
        Log.d("feong", "isReady");
        boolean result = true;
        for (StandardTestMethod test : beginList.getAllTests()) {
            boolean ready = test.classSetup();
            cases.put(test.toString(), ready);
            result = result && ready;
        }
        // TODO setTitle
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                activity.setTitle(testTitle);
//            }
//        });
        return result;
    }

    public TestAtTheSameTime getBeginList() {
        return beginList;
    }

    public void removeAllCases() {
        if (beginList != null) {
            beginList.removeAllTests();
        }
    }

    public void startTest() {
        if (timer != null) {
            stopTest();
        }

        if (createFolder(new File(recorder.strTestFolder))) {
            if (createFolder(new File(Environment.getExternalStorageDirectory(), "log_enabled"))) {
                timer = new Timer();
                bistTimerTask = new BISTTimerTask();
                timer.scheduleAtFixedRate(bistTimerTask, 0, 1000);
            }
        }
    }

    public void stopTest() {
        if (timer != null) {
            if (bistTimerTask != null) {
                bistTimerTask.cancel();
                bistTimerTask = null;
            }
            timer.cancel();
            timer = null;
            threadPool.shutdown();
//            sendMessage(BISTApplication.COMMAND, "TEST END");
        }
    }

    private void start(final StandardTestMethod test) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                test.start();
            }
        });
    }

    private void stop(final StandardTestMethod test) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                test.stop();
            }
        });
    }

//    public static void resetTic() {
//        if (tic > baseResetTic)
//            tic = -1;
//    }

    private void sendMessage(int what, String log) {
        if (messenger == null || log == null)
            return;

        Message message = Message.obtain();
        message.what = what;
        message.obj = log;
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean createFolder(File folder) {
        if (folder.exists()) {
            return true;
        }
        return folder.mkdirs();
    }

    private class BISTTimerTask extends TimerTask {
        private int tic = -1;
        private int cycle;
        private long startTime;
        private long last_tic = 0;
        private long new_tic = 0;
//        private boolean end;

        {
            if (recorder != null) {
                if (recorder.startTime == 0) {
                    recorder.startTime = System.currentTimeMillis();
                }
                startTime = recorder.startTime;
                cycle = recorder.cycle;
                if (recorder.rebootMoment == 0) {
                    // The first time reboot
                } else {
                    recorder.rebootMoment = System.currentTimeMillis() - recorder.rebootMoment;
                }
            } else {
                // WRONG!
            }
        }

        @Override
        public void run() {
            if(!BISTApplication.g_bEndSuspendTest) {
                Log.d("feong", "Suspend test is going to exit\n");
                return;
            }
            new_tic = System.currentTimeMillis();
            if(new_tic-last_tic<100){
                Log.d("feong", "resume");
                last_tic = new_tic;
                return;
            }
            last_tic = new_tic;
            tic++;

            if (end) {
                stopTest();
                sendMessage(BISTApplication.COMMAND, "TEST END");
                return;
            }

            tic = (tic + cycleTime) % cycleTime;
            if (tic == 0) {

                if (doRebootTest) {
                    // Special Test: Reboot test
                    if (recorder.cycle % rebootIntervalCycles == 0 && recorder.cycle != 0) {

                        if (recorder.rebootTimes == rebootTimesPerCycle) {
                            // Having already reboot target times, then reset this field
                            recorder.rebootTimes = 0;
                        } else {
                            rebootTest.start();
                            stopTest();
                            return;
                        }
                    }
                }


                cycle++;
                if (cycle>totalCycles) {
                    stopTest();
                    sendMessage(BISTApplication.COMMAND, "TEST END");
                    return;
                }
                sendMessage(BISTApplication.TIME_CYCLE, "Cycle: " + cycle);
                recorder.cycle = cycle;
                recorder.write();
            }
            sendMessage(BISTApplication.TIME_CYCLE, "Time: " + countTime());
            List<StandardTestMethod> list = endList.get(tic);
            if (list != null) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    Log.d("feong", list.get(i).toString()+": stop at the time stamp("+tic+")");
                    stop(list.get(i));
                }
//                if (tic == 0 && cycle == 1) {
//                    // As we have made the end time of suspend test to 0 second, we have to skip it(SuspendTest.Stop()) on Cycle 1 Second 0
//                } else {
//                    for (int i = 0, size = list.size(); i < size; i++) {
//                        stop(list.get(i));
//                    }
//                }
            }
            list = beginList.get(tic);
            if (list != null) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    Log.d("feong", list.get(i).toString()+": start at the time stamp("+tic+")");
                    start(list.get(i));
                }
            }

        }

        private String countTime() {
            long interval = System.currentTimeMillis() - startTime;
            interval = interval / 1000;
            long day = interval / (24 * 3600);
            long hour = interval % (24 * 3600) / 3600;
            long minute = interval % 3600 / 60;
            long second = interval % 60;
//            if (interval / 60 >= totalTime) {
//                end = true;
//            }
            if (day > 0) {
                return day + "D" + hour + "H" + minute + "M" + second + "S";
            } else if (hour > 0) {
                return hour + "H" + minute + "M" + second + "S";
            } else if (minute > 0) {
                return minute + "M" + second + "S";
            } else if (second > 0) {
                return second + "S";
            } else {
                return "";
            }
        }
    }

    public class TestAtTheSameTime {

        List<Integer> timeList = new ArrayList<Integer>();
        List<List<StandardTestMethod>> lists = new ArrayList<List<StandardTestMethod>>();
        List<StandardTestMethod> allTests = new ArrayList<StandardTestMethod>();

        public void add(int time, StandardTestMethod test) {
            if (timeList.contains(time)) {
                lists.get(timeList.indexOf(time)).add(test);
            } else {
                timeList.add(time);
                List<StandardTestMethod> tested = new ArrayList<StandardTestMethod>();
                tested.add(test);
                lists.add(tested);
            }
            allTests.add(test);
        }

        public List<StandardTestMethod> get(int time) {
            if (timeList.contains(time)) {
                return lists.get(timeList.indexOf(time));
            } else {
                return null;
            }
        }

        public List<StandardTestMethod> getAllTests() {
            return allTests;
        }

        public void removeAllTests() {
            if (allTests != null)
                allTests.clear();
        }
    }
    
    public static void SetEndtoTrue(){
    	end = true;
    }

}
