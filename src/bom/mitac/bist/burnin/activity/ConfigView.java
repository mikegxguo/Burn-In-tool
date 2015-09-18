package bom.mitac.bist.burnin.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.StandardTestMethod;
import bom.mitac.bist.burnin.module.TestFactory;
import bom.mitac.bist.burnin.util.*;

import bom.mitac.bist.burnin.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: xiaofeng.liu Date: 14-5-8 Time: 下午2:24
 */
public class ConfigView extends LinearLayout implements Button.OnClickListener {

	private Context context;
	private Recorder recorder;
	private TestView testView;
	private Map<String, String> cases = new HashMap<String, String>(24);;

	private TextView tvEnvironment;
	private TextView tvCaseName;
	private TextView tvCaseReady;

	private Button btnRecheck;
	private Button btnOK;
	private Button btnScan;

	private View configView;

	private Activity activity;
	private TestFactory testFactory;

	private String configVersion;
	private boolean beginNewTest;

	public ConfigView(Context context, Activity activity) {
		super(context);
		this.context = context;
		this.activity = activity;
		LayoutInflater layoutInflater = LayoutInflater.from(this.activity);
		configView = layoutInflater.inflate(R.layout.view_config, null);
		addView(configView);
		findViews();
		initUI();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_recheck:
			// reboot if BCR check Fail
			//Log.d("error", "BCR Result: " + cases.get("BCR").equals("false"));
			if (cases != null && cases.containsKey("BCR")
					&& cases.get("BCR").equals("false")) {
				recorder = Recorder.getInstance();
				recorder.BCRCheck = false;
				recorder.isTesting = true;
				recorder.write();
				if (Rebooter.isRebooterInstalled(context)) {
					// Ensure the environment is clean, so we reboot at the
					// first time.
					new Thread() {
						@Override
						public void run() {
							SystemClock.sleep(3000);
							Rebooter.reboot(context);
						}
					}.start();
					return;
				}
			}

			btnRecheck.setEnabled(false);
			checkTestCondition();
			break;
		case R.id.btn_ok:
			// set test date and time!
			// Log.d("error", "set time!");
			//setDate(context, 2000, 1, 1);
			//setTime(context, 0, 0);
			// SystemClock.setCurrentTimeMillis(1000);
			// Log.d("error","set time OK? "+SystemClock.setCurrentTimeMillis(1000));
			if (beginNewTest) {
				Recorder.getInstance().init();
				Recorder.getInstance().strTestFolder = BISTApplication.BASE_PATH
						+ File.separator
						+ TimeStamp
								.getTimeStamp(TimeStamp.TimeType.FULL_S_TYPE);
			}
			MainActivity.switchView();
			break;
		case R.id.btn_scan:
			BCRManager.getInstance(activity).stopScan();
			BCRManager.getInstance(activity).startScan();
			break;
		}
	}

	private void findViews() {
		if (configView == null)
			return;
		tvEnvironment = (TextView) configView.findViewById(R.id.tv_environment);
		tvCaseName = (TextView) configView.findViewById(R.id.tv_case_name);
		tvCaseReady = (TextView) configView.findViewById(R.id.tv_case_ready);
		btnRecheck = (Button) configView.findViewById(R.id.btn_recheck);
		btnOK = (Button) configView.findViewById(R.id.btn_ok);
		btnScan = (Button) configView.findViewById(R.id.btn_scan);

		btnRecheck.setOnClickListener(this);
		btnOK.setOnClickListener(this);
		btnScan.setOnClickListener(this);
	}

	private void initUI() {
		// btnRecheck.setVisibility(INVISIBLE);
		btnRecheck.setEnabled(false);
		btnOK.setEnabled(false);
		btnOK.setEnabled(false);
		btnScan.setVisibility(GONE);
	}

	public void checkTestCondition(TestFactory testFactory) {
		this.testFactory = testFactory;
		if (checkConfig()) {
			testFactory.removeAllCases();
			loadTestCases();
			checkTestCondition();
		}
	}

	private boolean checkConfig() {
		if (testFactory == null)
			return false;
		configVersion = testFactory.readConfig(BISTApplication.CONFIG_PATH);
		if (configVersion == null) {
			return false;
		} else {
			activity.setTitle(configVersion);
			return true;
		}
	}

	private boolean findCase(int id) {
		if (testFactory == null)
			return false;
		return testFactory.findCase(id);
	}

	private void loadTestCases() {
		SKU sku = SKU.read();
		if (sku != null) {
			SKU.Filter filter = sku.getFilter();
			if (filter != null) {
				if (filter.Camera_back
						&& findCase(BISTApplication.CameraTest_BACK_ID)) { // back

				}
				if (filter.Camera_front
						&& findCase(BISTApplication.CameraTest_FRONT_ID)) { // front

				}
				if (filter.NFC && findCase(BISTApplication.NFCTest_ID)) {

				}
				if (filter.BCR && findCase(BISTApplication.BCRTest_ID)) {

				}
				if (filter.Cellular
						&& findCase(BISTApplication.CellularTest_ID)) { // imei

				}
				if (filter.Cellular
						&& findCase(BISTApplication.CellularTest_ID)) { // ftp

				}
				if (filter.Wifi && findCase(BISTApplication.WIFITest_ID)) { // ping

				}
				if (filter.Wifi && findCase(BISTApplication.WIFITest_ID)) { // ftp

				}
				if (filter.iNAND && findCase(BISTApplication.INANDTest_ID)) {

				}
				if (filter.SD && findCase(BISTApplication.SDTest_ID)) {

				}
				if (filter.Flash && findCase(BISTApplication.FlashTest_ID)) {

				}
				if (filter.Video && findCase(BISTApplication.VideoTest_ID)) {

				}
				if (filter.BT && findCase(BISTApplication.BTTest_ID)) {

				}
				if (filter.GPS && findCase(BISTApplication.GPSTest_ID)) { // hot

				}
				if (filter.GPS && findCase(BISTApplication.GPSTest_ID)) { // warm

				}
				if (filter.GPS && findCase(BISTApplication.GPSTest_ID)) { // cold

				}
				if (filter.GPS && findCase(BISTApplication.GPSTest_ID)) { // nmea

				}
				if (findCase(BISTApplication.TemperatureTest_ID)) {

				}
				if (filter.Vibrator
						&& findCase(BISTApplication.VibratorTest_ID)) {

				}
				if (filter.BKL && findCase(BISTApplication.BKLTest_ID)) {

				}
				if (filter.Sensor && findCase(BISTApplication.SensorTest_ID)) {

				}
				if (filter.Battery && findCase(BISTApplication.BatteryTest_ID)) {

				}
				if (filter.Suspend && findCase(BISTApplication.SuspendTest_ID)) {

				}
				if (filter.USB && findCase(BISTApplication.USBTest_ID)) {

				}
				if (findCase(BISTApplication.RebootTest_ID)) {

				}
			}

		} else {
			for (int id : BISTApplication.IDS) {
				findCase(id);
			}
		}

	}

	private void checkTestCondition() {
		if (testFactory == null)
			return;

		new AsyncTask<Void, Void, Boolean>() {

			private StringBuilder caseName;
			private StringBuilder caseReady;
			private int ready;

			private void updateStatus() {
				caseName = new StringBuilder("CASE:").append("\r\n");
				caseReady = new StringBuilder("READY?").append("\r\n");
				if (cases.size() == 0)
					return;
				for (Map.Entry<String, String> entry : cases.entrySet()) {
					caseName.append(entry.getKey()).append("\r\n");
					caseReady.append(entry.getValue()).append("\r\n");
				}
			}

			@Override
			protected void onPreExecute() {
				Log.d("feong", "onPreExecute");
				tvEnvironment.setText("IMAGE: " + SystemInformation.getIMAGE());
				tvEnvironment.append("\r\nSKU ID: "
						+ SystemInformation.getSKUID());
				tvEnvironment.append("\r\nTool Version: "
						+ SystemInformation.getToolVersion(activity));
				tvEnvironment.append("\r\nConfig Version: " + configVersion);
				tvEnvironment.append("\r\nSystem Running Time: "
						+ SystemInformation.getRunnintTime());
				Recorder.getInstance().strTestCondition = tvEnvironment
						.getText().toString();
			}

			@Override
			protected Boolean doInBackground(Void... values) {
				Log.d("feong", "doInBackground");
				if (testFactory == null)
					return false;

				ready = -1;
				boolean result = true;
				for (StandardTestMethod test : testFactory.getBeginList()
						.getAllTests()) {
					cases.put(test.toString(), "...");
					updateStatus();
					publishProgress();
					boolean ready = test.classSetup();
					cases.put(test.toString(), String.valueOf(ready));
					updateStatus();
					publishProgress();
					result = result && ready;
				}

				for (int i = 3; i > 0; i--) {
					ready = i;
					publishProgress();
					SystemClock.sleep(1000);
				}
				return result;
			}

			@Override
			protected void onProgressUpdate(Void... values) {
				if (ready > 0) {
					btnOK.setText("OK(" + ready + ")");
				}
				// Turn "false" and "true" color
				Spannable span = null;
				String ready = caseReady.toString();
				List<Integer> listFalse = StringUtil.indexOf(ready, "false");
				List<Integer> listTrue = StringUtil.indexOf(ready, "true");
				span = new SpannableString(ready);
				for (int i : listFalse) {
					span.setSpan(new ForegroundColorSpan(Color.RED), i, i + 5,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					span.setSpan(new BackgroundColorSpan(Color.YELLOW), i,
							i + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				for (int i : listTrue) {
					span.setSpan(new ForegroundColorSpan(Color.GREEN), i,
							i + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				tvCaseName.setText(caseName);
				tvCaseReady.setText(span);
			}

			@Override
			protected void onPostExecute(Boolean ok) {
				Log.d("feong", "onPostExecute");
				if (ok) {
					if (!Recorder.getInstance().BCRCheck) {
						Log.d("error", "check1");
						recorder = Recorder.getInstance();
						recorder.BCRCheck = true;
						recorder.write();
						Log.d("error", "check2");
						btnOK.setText("OK");
						btnOK.setEnabled(true);
						Log.d("error", "check3");
						if (cases != null && cases.containsKey("BCR")) {
							Log.d("error", "check4");
							btnScan.setVisibility(VISIBLE);
						}
					} else {
						if (Recorder.getInstance().isTesting
								&& Recorder.getInstance().strTestFolder != null) {
							MainActivity.switchView();
							// ConfigView.this.setVisibility(View.GONE);
						} else {
							if (Recorder.getInstance().isTesting
									&& Recorder.getInstance().strTestFolder == null) {
								tvCaseReady
										.setText("Error! Read test recorder.xml failed!\n");
								tvCaseReady
										.append("If you wanna begin a new test, please click \"OK\" button");
								tvCaseReady.setTextColor(Color.RED);
								beginNewTest = true;
							}

							// activeManage();
							btnOK.setText("OK");
							btnOK.setEnabled(true);
							if (cases != null && cases.containsKey("BCR")) {
								btnScan.setVisibility(VISIBLE);
							}
						}
					}
				} else {
					btnRecheck.setEnabled(true);
					// btnOK.setText("Test anyway");
					// btnOK.setEnabled(true);
				}
			}
		}.execute();

	}

	private void activeManage() {
		// Launch permission panel
		ComponentName componentName = new ComponentName(activity,
				LockScreenAdmin.class);
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) activity
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (!devicePolicyManager.isAdminActive(componentName)) {
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			// List of permission
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					componentName);

			// Explanation
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					"Lock Screen");

			activity.startActivity(intent);
		}
	}

	static void setDate(Context context, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		long when = c.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
					.setTime(when);
		}
	}

	static void setTime(Context context, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long when = c.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
					.setTime(when);
		}
	}

}
