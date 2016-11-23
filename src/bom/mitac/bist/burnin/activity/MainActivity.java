package bom.mitac.bist.burnin.activity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestFactory;
import bom.mitac.bist.burnin.test.NFCTest;
import bom.mitac.bist.burnin.util.*;
import bom.mitac.bist.burnin.R;

import android.view.ViewGroup.LayoutParams;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

public class MainActivity extends Activity {

	private TestFactory testFactory;
	private Recorder recorder;

	private static ViewFlipper viewFlipper;
	private ConfigView configView;
	private TestView testView;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		saveLog("onCreate");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }

		setContentView(R.layout.activity_main);
		viewFlipper = (ViewFlipper) findViewById(R.id.vf_config_test);

		testView = new TestView(this, this);
		configView = new ConfigView(this, this);

		recorder = Recorder.read();
		if (recorder == null) {
			recorder = Recorder.getInstance();
		} else if (!recorder.isTesting) {
			// First run
			recorder.delete();
			recorder.init();
			recorder.strTestFolder = BISTApplication.BASE_PATH + File.separator
					+ TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_S_TYPE);
		} else {
			// Continual run
			recorder.resetTimes++;
			recorder.write();
		}

		testFactory = TestFactory.getInstance(testView.messenger, this);

		Animation rightIn = AnimationUtils.loadAnimation(this,
				R.xml.push_right_in);
		Animation leftOut = AnimationUtils.loadAnimation(this,
				R.xml.push_left_out);
		viewFlipper.addView(configView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		viewFlipper.addView(testView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		viewFlipper.setInAnimation(rightIn);
		viewFlipper.setOutAnimation(leftOut);

		// createShortcut();
		// activeManage();

		new Thread(new Runnable() {
			@Override
			public void run() {
				
				while (configView.getVisibility() != View.VISIBLE) {
					SystemClock.sleep(1000);
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						configView.checkTestCondition(testFactory);
					}
				});
				while (testView.getVisibility() != View.VISIBLE
						&& configView.getVisibility() == View.VISIBLE) {
					SystemClock.sleep(100);
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						testView.autoRun(testFactory, recorder);
					}
				});
			}
		}).start();

		// new AsyncTask<Void, Void, Void>() {
		//
		// @Override
		// protected Void doInBackground(Void... values) {
		// while (configView.getVisibility() != View.VISIBLE) {
		// SystemClock.sleep(1000);
		// }
		// SystemClock.sleep(5000);
		// publishProgress();
		// while (testView.getVisibility() != View.VISIBLE) {
		// SystemClock.sleep(100);
		// }
		// return null;
		// }
		//
		// @Override
		// protected void onProgressUpdate(Void... values) {
		// configView.checkTestCondition(testFactory);
		// }
		//
		// @Override
		// protected void onPostExecute(Void values) {
		// testView.autoRun(testFactory, recorder);
		// }
		// }.execute();

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		saveLog("onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		saveLog("onResume");
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		saveLog("onDestroy");
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onStop() {
		super.onStop();
		saveLog("onStop");
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveLog("onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		saveLog("onStart");
	}

	public static void switchView() {
		if (viewFlipper != null)
			viewFlipper.showNext();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		saveLog("onNewIntent");
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			if (testFactory == null)
				return;

			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if (tagFromIntent == null)
				return;
			// UID
			byte[] uid = tagFromIntent.getId();
			// Type
			String strCardType = "";
			for (String tech : tagFromIntent.getTechList()) {
				strCardType += tech.substring(tech.lastIndexOf(".") + 1);
				strCardType += ", ";
			}
			NFCTest.setScanned(uid, strCardType);
		}
	}

	private void activeManage() {
		// Launch permission panel
		ComponentName componentName = new ComponentName(this,
				LockScreenAdmin.class);
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (!devicePolicyManager.isAdminActive(componentName)) {
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			// List of permission
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					componentName);

			// Explanation
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					"Lock Screen");

			startActivity(intent);
		}
	}

	private void createShortcut() {

		Intent target = new Intent();
		target.setClassName(this, this.getClass().getName());

		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "shortcutName");
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this,
						R.drawable.ic_launcher));
		shortcutIntent.putExtra("duplicate", false);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, target);

		sendBroadcast(shortcutIntent);

	}

	private void saveLog(String log) {
		Log.d("feong", log);
		File file = new File(BISTApplication.BASE_PATH, "ActivityLife.txt");
		if (log == null || log.isEmpty()) {
			return;
		} else if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs())
				return;
		}
		log = TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_L_TYPE) + " "
				+ log + "\r\n";
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file, true);
			fileWriter.append(log);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
