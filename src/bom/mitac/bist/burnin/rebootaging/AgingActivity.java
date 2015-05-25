package bom.mitac.bist.burnin.rebootaging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;

import bom.mitac.bist.burnin.activity.ResultActivity;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.module.TestFactory;
import bom.mitac.bist.burnin.rebootaging.RebootAgingCmn;
import bom.mitac.bist.burnin.rebootaging.SETTING_XML_DATA;

import bom.mitac.bist.burnin.R;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
//import android.graphics.Camera;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;

//+frank
import android.provider.Settings;
import android.content.ContentResolver;
import bom.mitac.bist.burnin.util.Rebooter;

//-frank

public class AgingActivity extends Activity /* implements SurfaceHolder.Callback */{
	private static final String TAG = "frank0512-3:RebootAging4";
	private static final String LOG_FILE_NAME = Environment
			.getExternalStorageDirectory().getPath() + "/RebootAging.log.txt";
	private DebugLog m_DebugLog = new DebugLog(LOG_FILE_NAME);
	private static String ACTION_ALARM_BOOT;
	private static String ACTION_ALARM_REBOOT;
	private static String SETTING_XML_DATA_FILE_NAME;
	private boolean m_FirstFlag = true;
	private int m_Count = 0;
	private boolean m_PartialWakeLockFlag = false;
	private boolean m_FullWakeLockFlag = false;
	private PowerManager.WakeLock m_PartialWakeLock = null;
	private PowerManager.WakeLock m_FullWakeLock = null;
	private AlarmManager mAlarmManager = null; // +frank # 04.29
	private PendingIntent m_AlarmBootPendingIntent = null;
	private PendingIntent m_AlarmRebootPendingIntent = null;
	private int mCameraId = 0; // nakagawa
	private boolean m_delayed = false;
	private boolean mDebug = false;
	
	//+Joshua #0613
	private long endTime;
	private long runTime;
	private long beginTime;
	private int rebootTime;
	private static long LIMIT_TIME;

	// +frank # 0318
	private boolean mErrorOccur = false;
	private boolean mStop = false;
	// -frank # 0318

	private boolean mErrorDialogShowed = false; // +frank # 0423

	// +frank # 0425
	private synchronized void killPreviewDialog() {
		if (cameradialog != null) {
			Log.e(TAG, "killPreviewDialog:cameradialog.dismiss()");
			cameradialog.dismiss();
		}
	}

	// -frank # 0425

	private BroadcastReceiver m_AlarmBootBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Runnable FinishRunnable = new Runnable() {
				@Override
				public void run() {
					if (mErrorOccur)
						return; // +frank # 0318
					findViewById(R.id.button_stop).setEnabled(true);
					if (m_Count > 0 && m_delayed) {
						((AlarmManager) getSystemService(ALARM_SERVICE))
								.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
										SystemClock.elapsedRealtime()
												+ (Integer
														.valueOf(((EditText) findViewById(R.id.editText_on_period))
																.getText()
																.toString()) + 30)
												* 1000,
										m_AlarmRebootPendingIntent);
					} else {
						((AlarmManager) getSystemService(ALARM_SERVICE))
								.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
										SystemClock.elapsedRealtime()
												+ Integer
														.valueOf(((EditText) findViewById(R.id.editText_on_period))
																.getText()
																.toString())
												* 1000,
										m_AlarmRebootPendingIntent);
					}
				}
			};

			if (m_PartialWakeLockFlag == false) {
				m_PartialWakeLockFlag = true;
				m_PartialWakeLock.acquire();
			}
			if (m_FullWakeLockFlag == false) {
				m_FullWakeLockFlag = true;
				m_FullWakeLock.acquire();
			}
			if (m_FirstFlag == false) {
				m_Count++;
			} else {
				m_FirstFlag = false;
			}
			((EditText) findViewById(R.id.editText_count)).setText(String
					.valueOf(m_Count));

			BootRebootProcess(true, new Handler(), FinishRunnable);
		}
	};

	private BroadcastReceiver m_AlarmRebootBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Runnable FinishRunnable = new Runnable() {
				@Override
				public void run() {
					SETTING_XML_DATA SettingXmlData = new SETTING_XML_DATA();
					SettingXmlData.RebootTime = rebootTime;
					SettingXmlData.BeginTime = beginTime;
					SettingXmlData.StartFlag = true;
					SettingXmlData.LogDateTime = m_DebugLog.GetLogDateTime();
					SettingXmlData.OnPeriod = Integer
							.valueOf(((EditText) findViewById(R.id.editText_on_period))
									.getText().toString());
					SettingXmlData.StartTime = ((EditText) findViewById(R.id.editText_start_time))
							.getText().toString();
					SettingXmlData.StopTime = ((EditText) findViewById(R.id.editText_stop_time))
							.getText().toString();
					SettingXmlData.Count = Integer
							.valueOf(((EditText) findViewById(R.id.editText_count))
									.getText().toString());
					SettingXmlData.FreeMemory_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_free_memory_after_boot))
							.isChecked();
					SettingXmlData.FreeMemory_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_free_memory_before_reboot))
							.isChecked();
					SettingXmlData.WwanSignalStrength_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_after_boot))
							.isChecked();
					SettingXmlData.WwanSignalStrength_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_before_reboot))
							.isChecked();
					SettingXmlData.WifiSignalStrength_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_after_boot))
							.isChecked();
					SettingXmlData.WifiSignalStrength_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_before_reboot))
							.isChecked();
					SettingXmlData.Ping_Ip = ((EditText) findViewById(R.id.editText_ping_ip))
							.getText().toString();
					SettingXmlData.Ping_Timeout = Integer
							.valueOf(((EditText) findViewById(R.id.editText_ping_timeout))
									.getText().toString());
					SettingXmlData.Ping_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_ping_after_boot))
							.isChecked();
					SettingXmlData.Ping_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_ping_before_reboot))
							.isChecked();
					SettingXmlData.BluetoothDevice_Timeout = Integer
							.valueOf(((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
									.getText().toString());
					SettingXmlData.BluetoothDevice_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_after_boot))
							.isChecked();
					SettingXmlData.BluetoothDevice_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_before_reboot))
							.isChecked();
					SettingXmlData.Output = ((TextView) findViewById(R.id.textView_output))
							.getText().toString();
					SettingXmlData.Camera = mCameraId;
					RebootAgingCmn.SetSettingXmlData(SettingXmlData,
							SETTING_XML_DATA_FILE_NAME);

					if (m_FullWakeLockFlag) {
						m_FullWakeLockFlag = false;
						m_FullWakeLock.release();
					}

					if (m_PartialWakeLockFlag) {
						m_PartialWakeLockFlag = false;
						m_PartialWakeLock.release();
					}

					// +frank # 0318
					try {
						Settings.System.putString(getContentResolver(),
								"casio.test", "success");
					} catch (Exception e) {
						e.printStackTrace();
					}
					// -frank # 0318
					if (!mStop) {
						killPreviewDialog();
						rebootDevice();
						return;
					}
				}
			};
			findViewById(R.id.button_stop).setEnabled(false);
			BootRebootProcess(false, new Handler(), FinishRunnable);
		}
	};

	private void writeSettingsInfo() {
		SETTING_XML_DATA SettingXmlData = new SETTING_XML_DATA();
		SettingXmlData.StartFlag = true;
		SettingXmlData.LogDateTime = m_DebugLog.GetLogDateTime();
		SettingXmlData.OnPeriod = Integer
				.valueOf(((EditText) findViewById(R.id.editText_on_period))
						.getText().toString());
		SettingXmlData.StartTime = ((EditText) findViewById(R.id.editText_start_time))
				.getText().toString();
		SettingXmlData.StopTime = ((EditText) findViewById(R.id.editText_stop_time))
				.getText().toString();
		SettingXmlData.Count = Integer
				.valueOf(((EditText) findViewById(R.id.editText_count))
						.getText().toString());
		SettingXmlData.FreeMemory_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_free_memory_after_boot))
				.isChecked();
		SettingXmlData.FreeMemory_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_free_memory_before_reboot))
				.isChecked();
		SettingXmlData.WwanSignalStrength_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_after_boot))
				.isChecked();
		SettingXmlData.WwanSignalStrength_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_before_reboot))
				.isChecked();
		SettingXmlData.WifiSignalStrength_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_after_boot))
				.isChecked();
		SettingXmlData.WifiSignalStrength_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_before_reboot))
				.isChecked();
		SettingXmlData.Ping_Ip = ((EditText) findViewById(R.id.editText_ping_ip))
				.getText().toString();
		SettingXmlData.Ping_Timeout = Integer
				.valueOf(((EditText) findViewById(R.id.editText_ping_timeout))
						.getText().toString());
		SettingXmlData.Ping_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_ping_after_boot))
				.isChecked();
		SettingXmlData.Ping_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_ping_before_reboot))
				.isChecked();
		SettingXmlData.BluetoothDevice_Timeout = Integer
				.valueOf(((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
						.getText().toString());
		SettingXmlData.BluetoothDevice_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_after_boot))
				.isChecked();
		SettingXmlData.BluetoothDevice_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_before_reboot))
				.isChecked();
		SettingXmlData.Output = ((TextView) findViewById(R.id.textView_output))
				.getText().toString();
		SettingXmlData.Camera = mCameraId;
		RebootAgingCmn.SetSettingXmlData(SettingXmlData,
				SETTING_XML_DATA_FILE_NAME);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// nakagawa hide ime
		this.getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_rebootaging);
		
		ACTION_ALARM_BOOT = getPackageName() + ".ALARM_BOOT";
		ACTION_ALARM_REBOOT = getPackageName() + ".ALARM_REBOOT";

		SETTING_XML_DATA_FILE_NAME = getFileStreamPath("Setting.xml").getPath();

		m_PartialWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PartialWakeLock");
		m_FullWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.FULL_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.ON_AFTER_RELEASE, "FullWakeLock");
		mAlarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE)); // +frank
																			// #
																			// 04.29
		m_AlarmBootPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(ACTION_ALARM_BOOT), 0);
		m_AlarmRebootPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(ACTION_ALARM_REBOOT), 0);

		// getTotalErrorCount(); // +frank #0425
		registerReceiver(m_AlarmBootBroadcastReceiver, new IntentFilter(
				ACTION_ALARM_BOOT));
		registerReceiver(m_AlarmRebootBroadcastReceiver, new IntentFilter(
				ACTION_ALARM_REBOOT));

		SETTING_XML_DATA SettingXmlData = new SETTING_XML_DATA();
		// +frank # 0318
		// if (RebootAgingCmn.GetSettingXmlData(SettingXmlData,
		// SETTING_XML_DATA_FILE_NAME) == 0 && SettingXmlData.StartFlag) {
		// m_Count = SettingXmlData.Count + 1; // +frank # 03.25.2014
		// }
		// checkSuccessFlag();
		// -frank # 0318

		if (RebootAgingCmn.GetSettingXmlData(SettingXmlData,
				SETTING_XML_DATA_FILE_NAME) == 0 && SettingXmlData.StartFlag) {
			
			//Joshua add for get stop time
			endTime = System.currentTimeMillis();
			runTime = endTime - SettingXmlData.BeginTime;
			
			m_DebugLog.SetLogDateTime(SettingXmlData.LogDateTime);

			m_FirstFlag = false;
			try {
				m_Count = SettingXmlData.Count;
			} catch (Exception e) {
				m_Count = 0;
			}
			mCameraId = SettingXmlData.Camera;

			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
							| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			sendBroadcast(new Intent(ACTION_ALARM_BOOT));

			((EditText) findViewById(R.id.editText_on_period)).setText(String
					.valueOf(SettingXmlData.OnPeriod));
			((EditText) findViewById(R.id.editText_start_time))
					.setText(SettingXmlData.StartTime);
			((EditText) findViewById(R.id.editText_stop_time))
					.setText(SettingXmlData.StopTime);
			((EditText) findViewById(R.id.editText_count)).setText(String
					.valueOf(SettingXmlData.Count));
			((CheckBox) findViewById(R.id.checkBox_free_memory_after_boot))
					.setChecked(SettingXmlData.FreeMemory_AfterBoot);
			((CheckBox) findViewById(R.id.checkBox_free_memory_before_reboot))
					.setChecked(SettingXmlData.FreeMemory_BeforeReboot);
			((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_after_boot))
					.setChecked(SettingXmlData.WwanSignalStrength_AfterBoot);
			((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_before_reboot))
					.setChecked(SettingXmlData.WwanSignalStrength_BeforeReboot);
			((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_after_boot))
					.setChecked(SettingXmlData.WifiSignalStrength_AfterBoot);
			((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_before_reboot))
					.setChecked(SettingXmlData.WifiSignalStrength_BeforeReboot);
			((EditText) findViewById(R.id.editText_ping_ip))
					.setText(SettingXmlData.Ping_Ip);
			((EditText) findViewById(R.id.editText_ping_timeout))
					.setText(String.valueOf(SettingXmlData.Ping_Timeout));
			((CheckBox) findViewById(R.id.checkBox_ping_after_boot))
					.setChecked(SettingXmlData.Ping_AfterBoot);
			((CheckBox) findViewById(R.id.checkBox_ping_before_reboot))
					.setChecked(SettingXmlData.Ping_BeforeReboot);
			((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
					.setText(String
							.valueOf(SettingXmlData.BluetoothDevice_Timeout));
			((CheckBox) findViewById(R.id.checkBox_bluetooth_device_after_boot))
					.setChecked(SettingXmlData.BluetoothDevice_AfterBoot);
			((CheckBox) findViewById(R.id.checkBox_bluetooth_device_before_reboot))
					.setChecked(SettingXmlData.BluetoothDevice_BeforeReboot);
			((TextView) findViewById(R.id.textView_output)).setText(String
					.valueOf(SettingXmlData.Output));
			((Button) findViewById(R.id.button_delete_log_file))
					.setText("Delete Log File (" + LOG_FILE_NAME + ")");

			findViewById(R.id.editText_on_period).setEnabled(false);
			findViewById(R.id.button_delete_log_file).setEnabled(false);
			findViewById(R.id.button_start).setEnabled(false);
			findViewById(R.id.button_start_back).setEnabled(false);
			findViewById(R.id.button_start_front).setEnabled(false);
			findViewById(R.id.button_stop).setEnabled(false);

			findViewById(R.id.checkBox_free_memory_after_boot)
					.setEnabled(false);
			findViewById(R.id.checkBox_free_memory_before_reboot).setEnabled(
					false);
			findViewById(R.id.checkBox_wwan_signal_strength_after_boot)
					.setEnabled(false);
			findViewById(R.id.checkBox_wwan_signal_strength_before_reboot)
					.setEnabled(false);
			findViewById(R.id.checkBox_wifi_signal_strength_after_boot)
					.setEnabled(false);
			findViewById(R.id.checkBox_wifi_signal_strength_before_reboot)
					.setEnabled(false);
			findViewById(R.id.editText_ping_ip).setEnabled(false);
			findViewById(R.id.editText_ping_timeout).setEnabled(false);
			findViewById(R.id.checkBox_ping_after_boot).setEnabled(false);
			findViewById(R.id.checkBox_ping_before_reboot).setEnabled(false);
			findViewById(R.id.editText_bluetooth_device_timeout).setEnabled(
					false);
			findViewById(R.id.checkBox_bluetooth_device_after_boot).setEnabled(
					false);
			findViewById(R.id.checkBox_bluetooth_device_before_reboot)
					.setEnabled(false);
			
			//Joshua add for auto stop
			beginTime = SettingXmlData.BeginTime;
			Log.d("error","runTime:"+runTime);
			rebootTime = SettingXmlData.RebootTime;
			LIMIT_TIME =  rebootTime * 60000;
			Log.d("error","LIMIT_TIME:"+LIMIT_TIME);
			if(runTime > LIMIT_TIME ){
				stopTestProcess();
				Intent intent = new Intent();
		        intent.setClass(AgingActivity.this, ResultActivity.class);
		        startActivity(intent);
		        SystemClock.sleep(1000);
		        AgingActivity.this.finish();
					
//				Intent getFunctionResult = new Intent();
//		        ComponentName comp = new ComponentName("com.mitac.bist.burnin.activity","com.mitac.bist.burnin.activity.ResultActivity");
//		        getFunctionResult.setComponent(comp);
//		        getFunctionResult.setAction("android.intent.action.MAIN");
//		        startActivity(getFunctionResult);
//		        SystemClock.sleep(1000);
//		        AgingActivity.this.finish();
			}
			
		} else {
			((EditText) findViewById(R.id.editText_on_period)).setText(String
					.valueOf(10));
			((EditText) findViewById(R.id.editText_ping_timeout))
					.setText(String.valueOf(10));
			((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
					.setText(String.valueOf(10));
			((Button) findViewById(R.id.button_delete_log_file))
					.setText("Delete Log File (" + LOG_FILE_NAME + ")");

			findViewById(R.id.button_delete_log_file).setEnabled(
					m_DebugLog.Exist());
			findViewById(R.id.button_stop).setEnabled(false);
			
			//Joshua get begin time
			beginTime = System.currentTimeMillis();
			Log.d("error","Set BeginTime:"+beginTime);
			rebootTime = TestFactory.rebootTime;
			Log.d("error","Set RebootTime:"+rebootTime);

			// Joshua add for auto run
			mStop = false;
			mErrorDialogShowed = false;
//			mCameraId = 2; //with front camera
			mCameraId = 0; //without camera

			if (CheckInputItem()) {
				m_FirstFlag = true;
				m_Count = 0;
				if (mAlarmManager == null) {
					mAlarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE)); // +frank
																						// #
																						// 04.29
				}

				getWindow()
						.addFlags(
								WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
										| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				sendBroadcast(new Intent(ACTION_ALARM_BOOT));

				m_DebugLog.ClearLogDateTime();
				OutputViewAppend(null, null);
				OutputViewAppend(m_DebugLog.Output(true,
						"----- RebootAging Start -----"), null);

				Calendar WorkCalendar = Calendar.getInstance();
				((EditText) findViewById(R.id.editText_start_time))
						.setText(String.format(
								"%04d/%02d/%02d %02d:%02d:%02d.%03d",
								WorkCalendar.get(Calendar.YEAR),
								WorkCalendar.get(Calendar.MONTH) + 1,
								WorkCalendar.get(Calendar.DAY_OF_MONTH),
								WorkCalendar.get(Calendar.HOUR_OF_DAY),
								WorkCalendar.get(Calendar.MINUTE),
								WorkCalendar.get(Calendar.SECOND),
								WorkCalendar.get(Calendar.MILLISECOND)));
				((EditText) findViewById(R.id.editText_stop_time))
						.setText(null);

				findViewById(R.id.editText_on_period).setEnabled(false);
				findViewById(R.id.button_delete_log_file).setEnabled(false);
				findViewById(R.id.button_start).setEnabled(false);
				findViewById(R.id.button_start_back).setEnabled(false);
				findViewById(R.id.button_start_front).setEnabled(false);

				findViewById(R.id.checkBox_free_memory_after_boot).setEnabled(
						false);
				findViewById(R.id.checkBox_free_memory_before_reboot)
						.setEnabled(false);
				findViewById(R.id.checkBox_wwan_signal_strength_after_boot)
						.setEnabled(false);
				findViewById(R.id.checkBox_wwan_signal_strength_before_reboot)
						.setEnabled(false);
				findViewById(R.id.checkBox_wifi_signal_strength_after_boot)
						.setEnabled(false);
				findViewById(R.id.checkBox_wifi_signal_strength_before_reboot)
						.setEnabled(false);
				findViewById(R.id.editText_ping_ip).setEnabled(false);
				findViewById(R.id.editText_ping_timeout).setEnabled(false);
				findViewById(R.id.checkBox_ping_after_boot).setEnabled(false);
				findViewById(R.id.checkBox_ping_before_reboot)
						.setEnabled(false);
				findViewById(R.id.editText_bluetooth_device_timeout)
						.setEnabled(false);
				findViewById(R.id.checkBox_bluetooth_device_after_boot)
						.setEnabled(false);
				findViewById(R.id.checkBox_bluetooth_device_before_reboot)
						.setEnabled(false);
			}
		}

		findViewById(R.id.button_delete_log_file).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						new AlertDialog.Builder(arg0.getContext())
								.setTitle("Question")
								.setMessage("Do you delete Log File ?")
								.setCancelable(false)
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												OutputViewAppend(
														m_DebugLog.Delete() ? "Delete Log File : Success\r\n"
																: "Delete Log File : Failure\r\n",
														null);
												findViewById(
														R.id.button_delete_log_file)
														.setEnabled(
																m_DebugLog
																		.Exist());
											}
										}).setNeutralButton("No", null).show();
					}
				});

		findViewById(R.id.button_start).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startButton(v, 0);
					}
				});
		// nakagawa
		findViewById(R.id.button_start_back).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startButton(v, 1);
					}
				});
		// nakagawa
		findViewById(R.id.button_start_front).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startButton(v, 2);
					}
				});

		// +frank # 04.17
		findViewById(R.id.button_stop).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "stop button is pressed");
						stopTestProcess();
					}
				});
		// -frank # 04.17
	}

	// +frank # 04.25
	private void rebootDevice() {
		Log.d(TAG, "rebootDevice is called");
		//((PowerManager) getSystemService(POWER_SERVICE)).reboot(null);
		Rebooter.reboot(this);
	}

	// -frank # 04.25

	// +frank # 03.25
	private synchronized void releaseCamera() {
		Log.d(TAG, "releaseCamera() is called");
		if (mCamera != null) {
			try {
				mCamera.stopPreview();
				Log.d(TAG, "mCamera.stopPreview();");
			} catch (Exception e) {
				handleErrors("releaseCamera: Camera stopPreview API error", e,
						true);
				return;
			}

			try {
				mCamera.setPreviewCallback(null);
				Log.d(TAG, "mCamera.setPreviewCallback(null);");
			} catch (Exception e) {
				handleErrors(
						"releaseCamera: mCamera.setPreviewCallback(null) API error",
						e, true);
				return;
			}

			try {
				mCamera.unlock();
				Log.d(TAG, "mCamera.unlock();");
			} catch (Exception e) {
				handleErrors("releaseCamera: mCamera.unlock() API error", e,
						true);
				return;
			}

			try {
				mCamera.release();
				Log.d(TAG, "mCamera.release();");
			} catch (Exception e) {
				handleErrors("releaseCamera: mCamera.release() API error", e,
						true);
				return;
			}

			mCamera = null;
		}
	}

	// -frank # 03.25

	// +frank # 04.17
	private synchronized void setCameraPara() {
		Log.d(TAG, "setCameraPara() is called");
		// +frank # 04.17
		int width = 640;
		int height = 480;
		int format = ImageFormat.NV21;

		if (mDebug && m_Count == 2) {
			mCamera = null;
		}

		if (mCamera == null) {
			handleErrors("setCameraPara: Camera object is null", true);
			return;
		} else {
			try {
				Camera.Parameters params = mCamera.getParameters();
				params.setPreviewSize(width, height);
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				android.view.ViewGroup.LayoutParams lp = surfaceView
						.getLayoutParams();
				int ch = params.getPreviewSize().height;
				int cw = params.getPreviewSize().width;

				lp.width = cw; // fixed
				lp.height = ch; // fixed
				surfaceView.setLayoutParams(lp);

				Log.d(TAG, "Camera: " + cw + " x " + ch + " View: " + lp.width
						+ " x " + lp.height);
				mCamera.setParameters(params);
			} catch (NullPointerException e) {
				handleErrors("setCameraPara: NullPointerException error:", e,
						false);
			} catch (Exception e) {
				handleErrors("setCameraPara: Unexpected Exception error", e,
						false);
			}

		}
		// -frank # 0417
	}

	// +frank # 04.17

	// +frank # 0325
	private synchronized void startCamera(final SurfaceHolder holder) {
		Log.d(TAG, "startCamera() is called");
		// // TODO Auto-generated method stub
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
				handleErrors("startCamera: Camera setPreviewDisplay API error",
						e, true);
				return;
			}

			try {
				mstartPreview();
			} catch (Exception e) {
				handleErrors("startCamera: Camera startPreview API errors", e,
						true);
				return;
			}
		}
	}

	// -frank # 0325
	private synchronized void mstartPreview() throws Exception {
		Log.d(TAG, "mstartPreview() is called");
		// TODO Auto-generated method stub
		if (mCamera != null && !mStop) {
			try {
				Log.d(TAG, " mCamera.startPreview()");
				mCamera.startPreview();
			} catch (Exception e) {
				handleErrors("mstartPreview: Camera startPreview API errors",
						e, true);
				return;
			}
		}
	}

	// -frank # 03.25.2014

	// +frank # 0318
	private synchronized void showErrorDialog(String msg) {
		Log.e(TAG, "showErrorDialog(String) is called ");
		if (!mErrorDialogShowed)
			AlertDialog_Ok("ERROR OCCUR", msg);
	}

	private synchronized void showErrorDialog() {
		Log.e(TAG, "showErrorDialog() is called ");
		if (!mErrorDialogShowed)
			AlertDialog_Ok("ERROR OCCUR", "please check the log file");
	}

	private synchronized void dumpErrorMsgs(String msg, Exception e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		m_DebugLog.Output(true, msg + errors.toString());
		OutputViewAppend(m_DebugLog.Output(true, msg + errors.toString()), null);
	}

	private synchronized void dumpErrorMsgs(String msg) {
		m_DebugLog.Output(true, msg);
		// OutputViewAppend(m_DebugLog.Output(true, msg), null);
	}

	private synchronized void handleErrors(String msg, Exception e, boolean stop) {
		Log.e(TAG, "handleErrors(String msg:" + msg
				+ ", Exception e) is called ");
		dumpErrorMsgs(msg + "=>", e);
		if (stop)
			standardErrorHandler();
	}

	private synchronized void handleErrors(String msg, boolean stop) {
		Log.e(TAG, "handleErrors(String msg: " + msg + ") is called ");
		dumpErrorMsgs(msg);
		if (stop)
			standardErrorHandler();
	}

	private synchronized void handleErrors(String msg, String dailogMsg,
			boolean stop) {
		Log.e(TAG, "handleErrors(String msg:" + msg + ", String dailogMsg:"
				+ dailogMsg + ") is called ");
		dumpErrorMsgs(msg);
		if (stop)
			standardErrorHandler(dailogMsg);
	}

	private synchronized void handleErrors(String msg, String dailogMsg,
			Exception e, boolean stop) {
		Log.e(TAG, "handleErrors(String msg:" + msg + ", String dailogMsg:"
				+ dailogMsg + ", Exception e) is called ");
		dumpErrorMsgs(msg + "=>", e);
		if (stop)
			standardErrorHandler(dailogMsg);
	}

	private synchronized void standardErrorHandler() {
		mErrorOccur = true;
		showErrorDialog();
		stopTestProcess();
	}

	private synchronized void standardErrorHandler(String dialogMsg) {
		mErrorOccur = true;
		showErrorDialog(dialogMsg);
		stopTestProcess();
	}

	// private int getTotalErrorCount() {
	// int totalErrorCount = Settings.System.getInt(getContentResolver(),
	// TOTAL_ERROR_COUNT_KEY, 0);
	// Log.d(TAG, "getTotalErrorCount(): " + totalErrorCount);
	// return totalErrorCount;
	// }
	//
	// private void setTotalErrorCount(int errorCount) {
	// Log.d(TAG, "setTotalErrorCount(): " + errorCount);
	// Settings.System.putInt(getContentResolver(), TOTAL_ERROR_COUNT_KEY,
	// errorCount);
	// }
	//
	// private void increaseTotalErrorCount() {
	// int totalErrorCount = getTotalErrorCount() + 1;
	// Log.d(TAG, "increaseTotalErrorCount(): " + totalErrorCount);
	// setTotalErrorCount(totalErrorCount);
	// }
	//
	// private void resetTotalErrorCount() {
	// Log.d(TAG, "resetTotalErrorCount()");
	// setTotalErrorCount(0);
	// }

	private void fakeException() throws Exception {
		throw new Exception("fake exception");
	}

	private void checkSuccessFlag() {
		String casio_test = Settings.System.getString(getContentResolver(),
				"casio.test");
		Log.d(TAG, "casio.test=>" + casio_test);
		if (m_Count > 0 && !("success".equals(casio_test))) {
			Log.d(TAG, "casio.test=>" + casio_test);
			Log.d(TAG, "m_Count=>" + m_Count);
			Log.d(TAG, "damn error occur");
			mErrorOccur = true;
		} else {
			Log.d(TAG, "casio.test=>" + casio_test);
			Log.d(TAG, "m_Count=>" + m_Count);
			Log.d(TAG, "NO error occur");
			mErrorOccur = false;
			Settings.System.putString(getContentResolver(), "casio.test",
					"null");
			Log.d(TAG, "reset casio.test=>null");
		}
	}

	private synchronized void stopTestProcess() {
		Log.d(TAG, "stopTestProecess is called");
		mStop = true;
		// resetTotalErrorCount();
		killPreviewDialog();

		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		((AlarmManager) getSystemService(ALARM_SERVICE))
				.cancel(m_AlarmRebootPendingIntent);
		((AlarmManager) getSystemService(ALARM_SERVICE))
				.cancel(m_AlarmBootPendingIntent);

		if (m_FullWakeLockFlag) {
			m_FullWakeLockFlag = false;
			m_FullWakeLock.release();
		}
		if (m_PartialWakeLockFlag) {
			m_PartialWakeLockFlag = false;
			m_PartialWakeLock.release();
		}

		OutputViewAppend(
				m_DebugLog.Output(true, "----- RebootAging End -----"), null);
		Calendar WorkCalendar = Calendar.getInstance();
		((EditText) findViewById(R.id.editText_stop_time)).setText(String
				.format("%04d/%02d/%02d %02d:%02d:%02d.%03d",
						WorkCalendar.get(Calendar.YEAR),
						WorkCalendar.get(Calendar.MONTH) + 1,
						WorkCalendar.get(Calendar.DAY_OF_MONTH),
						WorkCalendar.get(Calendar.HOUR_OF_DAY),
						WorkCalendar.get(Calendar.MINUTE),
						WorkCalendar.get(Calendar.SECOND),
						WorkCalendar.get(Calendar.MILLISECOND)));

		findViewById(R.id.editText_on_period).setEnabled(true);
		findViewById(R.id.button_delete_log_file)
				.setEnabled(m_DebugLog.Exist());
		findViewById(R.id.button_start).setEnabled(true);
		findViewById(R.id.button_start_back).setEnabled(true);
		findViewById(R.id.button_start_front).setEnabled(true);
		findViewById(R.id.button_stop).setEnabled(false);

		findViewById(R.id.checkBox_free_memory_after_boot).setEnabled(true);
		findViewById(R.id.checkBox_free_memory_before_reboot).setEnabled(true);
		findViewById(R.id.checkBox_wwan_signal_strength_after_boot).setEnabled(
				true);
		findViewById(R.id.checkBox_wwan_signal_strength_before_reboot)
				.setEnabled(true);
		findViewById(R.id.checkBox_wifi_signal_strength_after_boot).setEnabled(
				true);
		findViewById(R.id.checkBox_wifi_signal_strength_before_reboot)
				.setEnabled(true);
		findViewById(R.id.editText_ping_ip).setEnabled(true);
		findViewById(R.id.editText_ping_timeout).setEnabled(true);
		findViewById(R.id.checkBox_ping_after_boot).setEnabled(true);
		findViewById(R.id.checkBox_ping_before_reboot).setEnabled(true);
		findViewById(R.id.editText_bluetooth_device_timeout).setEnabled(true);
		findViewById(R.id.checkBox_bluetooth_device_after_boot)
				.setEnabled(true);
		findViewById(R.id.checkBox_bluetooth_device_before_reboot).setEnabled(
				true);

		SETTING_XML_DATA SettingXmlData = new SETTING_XML_DATA();
		SettingXmlData.BeginTime = endTime;
		SettingXmlData.StartFlag = false;
		SettingXmlData.LogDateTime = m_DebugLog.GetLogDateTime();
		SettingXmlData.OnPeriod = Integer
				.valueOf(((EditText) findViewById(R.id.editText_on_period))
						.getText().toString());
		SettingXmlData.StartTime = ((EditText) findViewById(R.id.editText_start_time))
				.getText().toString();
		SettingXmlData.StopTime = ((EditText) findViewById(R.id.editText_stop_time))
				.getText().toString();
		SettingXmlData.Count = Integer
				.valueOf(((EditText) findViewById(R.id.editText_count))
						.getText().toString());
		SettingXmlData.FreeMemory_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_free_memory_after_boot))
				.isChecked();
		SettingXmlData.FreeMemory_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_free_memory_before_reboot))
				.isChecked();
		SettingXmlData.WwanSignalStrength_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_after_boot))
				.isChecked();
		SettingXmlData.WwanSignalStrength_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_wwan_signal_strength_before_reboot))
				.isChecked();
		SettingXmlData.WifiSignalStrength_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_after_boot))
				.isChecked();
		SettingXmlData.WifiSignalStrength_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_wifi_signal_strength_before_reboot))
				.isChecked();
		SettingXmlData.Ping_Ip = ((EditText) findViewById(R.id.editText_ping_ip))
				.getText().toString();
		SettingXmlData.Ping_Timeout = Integer
				.valueOf(((EditText) findViewById(R.id.editText_ping_timeout))
						.getText().toString());
		SettingXmlData.Ping_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_ping_after_boot))
				.isChecked();
		SettingXmlData.Ping_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_ping_before_reboot))
				.isChecked();
		SettingXmlData.BluetoothDevice_Timeout = Integer
				.valueOf(((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
						.getText().toString());
		SettingXmlData.BluetoothDevice_AfterBoot = ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_after_boot))
				.isChecked();
		SettingXmlData.BluetoothDevice_BeforeReboot = ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_before_reboot))
				.isChecked();
		SettingXmlData.Output = ((TextView) findViewById(R.id.textView_output))
				.getText().toString();
		SettingXmlData.Camera = mCameraId;
		RebootAgingCmn.SetSettingXmlData(SettingXmlData,
				SETTING_XML_DATA_FILE_NAME);

		// +frank
		Settings.System.putString(getContentResolver(), "casio.test", "null");
		Log.d(TAG, "stopTestProcess:reset casio.test=>null");
		mErrorOccur = false;
		// -frank
	}

	// -frank # 0318

	// nakagawa check camera
	private void startButton(View v, int cameraid) {
		mStop = false; // +frank # 0318
		mErrorDialogShowed = false; // +frank # 0425
		mCameraId = cameraid;

		// surfaceViewDialog();

		if (CheckInputItem()) {
			m_FirstFlag = true;
			m_Count = 0;
			if (mAlarmManager == null) {
				mAlarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE)); // +frank
																					// #
																					// 04.29
			}

			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
							| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			sendBroadcast(new Intent(ACTION_ALARM_BOOT));

			m_DebugLog.ClearLogDateTime();
			OutputViewAppend(null, null);
			OutputViewAppend(
					m_DebugLog.Output(true, "----- RebootAging Start -----"),
					null);

			Calendar WorkCalendar = Calendar.getInstance();
			((EditText) findViewById(R.id.editText_start_time)).setText(String
					.format("%04d/%02d/%02d %02d:%02d:%02d.%03d",
							WorkCalendar.get(Calendar.YEAR),
							WorkCalendar.get(Calendar.MONTH) + 1,
							WorkCalendar.get(Calendar.DAY_OF_MONTH),
							WorkCalendar.get(Calendar.HOUR_OF_DAY),
							WorkCalendar.get(Calendar.MINUTE),
							WorkCalendar.get(Calendar.SECOND),
							WorkCalendar.get(Calendar.MILLISECOND)));
			((EditText) findViewById(R.id.editText_stop_time)).setText(null);

			findViewById(R.id.editText_on_period).setEnabled(false);
			findViewById(R.id.button_delete_log_file).setEnabled(false);
			findViewById(R.id.button_start).setEnabled(false);
			findViewById(R.id.button_start_back).setEnabled(false);
			findViewById(R.id.button_start_front).setEnabled(false);

			findViewById(R.id.checkBox_free_memory_after_boot)
					.setEnabled(false);
			findViewById(R.id.checkBox_free_memory_before_reboot).setEnabled(
					false);
			findViewById(R.id.checkBox_wwan_signal_strength_after_boot)
					.setEnabled(false);
			findViewById(R.id.checkBox_wwan_signal_strength_before_reboot)
					.setEnabled(false);
			findViewById(R.id.checkBox_wifi_signal_strength_after_boot)
					.setEnabled(false);
			findViewById(R.id.checkBox_wifi_signal_strength_before_reboot)
					.setEnabled(false);
			findViewById(R.id.editText_ping_ip).setEnabled(false);
			findViewById(R.id.editText_ping_timeout).setEnabled(false);
			findViewById(R.id.checkBox_ping_after_boot).setEnabled(false);
			findViewById(R.id.checkBox_ping_before_reboot).setEnabled(false);
			findViewById(R.id.editText_bluetooth_device_timeout).setEnabled(
					false);
			findViewById(R.id.checkBox_bluetooth_device_after_boot).setEnabled(
					false);
			findViewById(R.id.checkBox_bluetooth_device_before_reboot)
					.setEnabled(false);
		}
	}

	private Dialog cameradialog = null;
	private SurfaceView surfaceView = null;

	// +frank # 04.17
	private synchronized void surfaceViewDialog() {
		Log.d(TAG, "surfaceViewDialog() is called");

		if (mCameraId == 0) {
			return;
		}
		if (mErrorOccur || mStop) {
			killPreviewDialog();
			return;
		}

		// +frank # 03.25.2014
		try {
			getCameraInstance(mCameraId - 1);
		} catch (Exception e) {
			handleErrors("Camera open fail", e, true);
			return;
		}
		// -frank # 03.25.2014
		try {
			surfaceView = new SurfaceView(this);
			surfaceView.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			if (cameradialog == null) {
				cameradialog = new Dialog(this);
				// cameradialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				cameradialog
						.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
				cameradialog.setCancelable(true);
				cameradialog.setTitle(mCameraId == 1 ? "Back Camera"
						: "Front Camera");
			}
			cameradialog.setContentView(surfaceView);
			surfaceView.requestFocus();
			LayoutParams lp = new LayoutParams();
			lp.screenBrightness = 1.0f;

			if (mCamera != null) {
				setCameraPara(); // +frank # 04.17
				Camera.Parameters params = mCamera.getParameters();
				lp.width = params.getPreviewSize().width;
				lp.height = params.getPreviewSize().height;
			} else {
				// +frank # 03.25.2014
				lp.width = 640;
				lp.height = 480;
				// -frank # 03.25.2014
			}
			Log.d(TAG, "surfaceViewDialog(): width=" + lp.width + " height="
					+ lp.height);
			cameradialog.getWindow().setAttributes(lp);
			cameradialog.getWindow().setGravity(Gravity.BOTTOM + Gravity.RIGHT);

			SurfaceHolder holder = surfaceView.getHolder();
			holder.addCallback(mSurfaceListener);
			cameradialog.show();
		} catch (NullPointerException e) {
			handleErrors("surfaceViewDialog: Null pointer error ",
					"Null pointer error", e, false);
			return;
		} catch (Exception e) {
			handleErrors("surfaceViewDialog: Unexpected Exception error ",
					"Unexpected Exception error", e, false);
			return;
		}
	}

	// -frank # 04.17

	private synchronized void getCameraInstance(int cameraId) throws Exception {
		Log.d(TAG, "getCameraInstance(" + cameraId + ") is called");
		if (mCamera == null) {
			Log.d(TAG, "Camera.open(" + cameraId + ")");
			mCamera = Camera.open(cameraId);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (m_FullWakeLock != null) {
			if (m_FullWakeLockFlag) {
				m_FullWakeLockFlag = false;
				m_FullWakeLock.release();
			}
		}
		if (m_PartialWakeLock != null) {
			if (m_PartialWakeLockFlag) {
				m_PartialWakeLockFlag = false;
				m_PartialWakeLock.release();
			}
		}
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		stopAlarmIntent(); // +frank #04.29
		unregisterReceiver(m_AlarmRebootBroadcastReceiver);
		unregisterReceiver(m_AlarmBootBroadcastReceiver);
	}

	private void BootRebootProcess(final boolean BootFlag,
			final Handler FinishHandler, final Runnable FinishRunnable) {
		final Handler WorkHandler = new Handler();
		final Runnable BluetoothDeviceFinishRunnable = new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "BootRebootProcess(BootFlag=" + BootFlag + ")");
				if (BootFlag) {
					OutputViewAppend(m_DebugLog.Output(true,
							"-After Boot Process Start | Count:[%d]", m_Count),
							null);
				} else {
					OutputViewAppend(m_DebugLog.Output(true,
							"-Before Reboot Process Start | Count:[%d]",
							m_Count), null);
				}
				FinishHandler.post(FinishRunnable);
			}
		};
		final Runnable PingFinishRunnable = new Runnable() {
			@Override
			public void run() {
				BluetoothDevice(
						WorkHandler,
						BluetoothDeviceFinishRunnable,
						((CheckBox) findViewById(BootFlag ? R.id.checkBox_bluetooth_device_after_boot
								: R.id.checkBox_bluetooth_device_before_reboot))
								.isChecked());
			}
		};
		final Runnable WifiSignalStrengthFinishRunnable = new Runnable() {
			@Override
			public void run() {
				Ping(WorkHandler,
						PingFinishRunnable,
						((CheckBox) findViewById(BootFlag ? R.id.checkBox_ping_after_boot
								: R.id.checkBox_ping_before_reboot))
								.isChecked());
			}
		};
		final Runnable WwanSignalStrengthFinishRunnable = new Runnable() {
			@Override
			public void run() {
				WifiSignalStrength(
						WorkHandler,
						WifiSignalStrengthFinishRunnable,
						((CheckBox) findViewById(BootFlag ? R.id.checkBox_wifi_signal_strength_after_boot
								: R.id.checkBox_wifi_signal_strength_before_reboot))
								.isChecked());
			}
		};
		final Runnable FreeMemoryFinishRunnable = new Runnable() {
			@Override
			public void run() {
				WwanSignalStrength(
						WorkHandler,
						WwanSignalStrengthFinishRunnable,
						((CheckBox) findViewById(BootFlag ? R.id.checkBox_wwan_signal_strength_after_boot
								: R.id.checkBox_wwan_signal_strength_before_reboot))
								.isChecked());
			}
		};
		final Runnable StartRunnable = new Runnable() {
			@Override
			public void run() {
				FreeMemory(
						WorkHandler,
						FreeMemoryFinishRunnable,
						((CheckBox) findViewById(BootFlag ? R.id.checkBox_free_memory_after_boot
								: R.id.checkBox_free_memory_before_reboot))
								.isChecked());
			}
		};
		if (BootFlag == false) {
			OutputViewAppend(null, null);
		}
		if (BootFlag) {
			OutputViewAppend(m_DebugLog.Output(true,
					"+After Boot Process Start | Count:[%d]", m_Count), null);
		} else {
			OutputViewAppend(m_DebugLog.Output(true,
					"+Before Reboot Process Start | Count:[%d]", m_Count), null);
		}
		// WorkHandler.post(StartRunnable);
		CameraCheck(BootFlag, WorkHandler, StartRunnable); 
	}

	//Joshua:Cancel camera check for Aries
	private void CameraCheck(boolean BootFlag, Handler handler,
			Runnable runnable) {
		Log.d(TAG, "CameraCheck(BootFlag=" + BootFlag + ")");
		int nCamera = 0;

		try {
//			nCamera = Camera.getNumberOfCameras();
		} catch (Exception e) {
//			Toast.makeText(this, "Camera Count Exception: " + e,
//					Toast.LENGTH_LONG).show();
//			handleErrors("Camera count exception", e, true); // +frank # 0318
			return;
		}

//		if (nCamera != 2) {
			// +frank # 0318
//			handleErrors("Camera Count Number Error", "Camera count number:"
//					+ nCamera + " is incorrect", true);
//			return;
//		} else {
//			Toast.makeText(this, "Camera Number : " + nCamera,
//					Toast.LENGTH_SHORT).show();
			surfaceViewDialog();
			handler.post(runnable);
//		}
	}

	// +frank # 04.25
	private Camera mCamera = null;
	private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated() is called");
			if (mCameraId == 0) {
				Log.e(TAG, "surfaceCreated: mCameraId == 0");
				handleErrors("surfaceCreated: mCameraId == 0", false);
				return;
			}

			if (mCamera == null) {
				Log.e(TAG, "surfaceCreated: mCamera is null");
				handleErrors("surfaceCreated: mCamera is null", false);
				return;
			}

			startCamera(holder); // +frank # 0325
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed()");
			if (mCamera != null)
				releaseCamera();
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.d(TAG, "surfaceChanged(): format=" + format + " width=" + width
					+ " height=" + height);
			// if (mCamera == null) {
			// Log.e(TAG, "surfaceCreated: mCamera is null");
			// handleErrors("surfaceCreated: mCamera is null", false);
			// return;
			// }
		}
	};

	// -frank # 04.25

	/***** FreeMemory *****/
	private void FreeMemory(Handler FinishHandler, Runnable FinishRunnable,
			boolean ValidFlag) {
		if (ValidFlag) {
			ActivityManager.MemoryInfo WorkMemoryInfo = new ActivityManager.MemoryInfo();
			((ActivityManager) getSystemService(ACTIVITY_SERVICE))
					.getMemoryInfo(WorkMemoryInfo);
			OutputViewAppend(m_DebugLog.Output(true, "    Free Memory:[%d]",
					WorkMemoryInfo.availMem), null);
		}
		FinishHandler.post(FinishRunnable);
	}

	/***** WwanSignalStrength *****/
	private void WwanSignalStrength(final Handler FinishHandler,
			final Runnable FinishRunnable, boolean ValidFlag) {
		if (ValidFlag) {
			PhoneStateListener WorkPhoneStateListener = new PhoneStateListener() {
				@Override
				public void onSignalStrengthsChanged(
						SignalStrength signalStrength) {
					// TODO 閾ｪ�?慕函謌舌＆繧後◆�?｡繧ｽ�?�ラ�?ｻ繧ｹ繧ｿ�?�?
					super.onSignalStrengthsChanged(signalStrength);
					int Work1;
					Work1 = signalStrength.getGsmSignalStrength();
					OutputViewAppend(m_DebugLog.Output(true,
							"    Wwan Signal Strength:%s[%d]",
							Work1 == 99 ? "(Error)" : "", Work1), null);
					((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
							.listen(this, PhoneStateListener.LISTEN_NONE);
					FinishHandler.post(FinishRunnable);
				}
			};
			((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(
					WorkPhoneStateListener,
					PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		} else {
			FinishHandler.post(FinishRunnable);
		}
	}

	/***** WifiSignalStrength *****/
	private void WifiSignalStrength(Handler FinishHandler,
			Runnable FinishRunnable, boolean ValidFlag) {
		if (ValidFlag) {
			WifiInfo WorkWifiInfo = ((WifiManager) getSystemService(WIFI_SERVICE))
					.getConnectionInfo();
			OutputViewAppend(m_DebugLog.Output(true,
					"    Wifi Signal Strength:%s[SSID:%s, RSSI:%d]",
					WorkWifiInfo.getSSID() == null ? "(Error)" : "",
					WorkWifiInfo.getSSID(), WorkWifiInfo.getRssi()), null);
		}
		FinishHandler.post(FinishRunnable);
	}

	/***** Ping *****/
	private void Ping(Handler FinishHandler, Runnable FinishRunnable,
			boolean ValidFlag) {
		if (ValidFlag) {
			OutputViewAppend(
					m_DebugLog.Output(
							true,
							"    Ping( %s ):%s",
							((EditText) findViewById(R.id.editText_ping_ip))
									.getText().toString(),
							ExecShellCommand(String
									.format("ping -c 1 -w %s %s",
											((EditText) findViewById(R.id.editText_ping_timeout))
													.getText().toString(),
											((EditText) findViewById(R.id.editText_ping_ip))
													.getText().toString())) != 0 ? "(Error)[NG]"
									: "[OK]"), null);
		}
		FinishHandler.post(FinishRunnable);
	}

	/***** BluetoothDevice *****/
	private String m_BluetoothDeviceDebugString = null;
	private BroadcastReceiver m_BluetoothDeviceFoundBroadcastReceiver = null;

	private void BluetoothDevice(final Handler FinishHandler,
			final Runnable FinishRunnable, boolean ValidFlag) {
		if (ValidFlag) {
			final BluetoothAdapter WorkBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			final Handler WorkHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// update UI here depending on what message is received.
					super.handleMessage(msg);
					switch (msg.what) {
					case 1:
						break;
					}
				}
			};

			final Runnable WorkRunnable = new Runnable() {
				@Override
				public void run() {
					OutputViewAppend(m_DebugLog.Output(true,
							"    Bluetooth Device:%s[%s]",
							m_BluetoothDeviceDebugString == null ? "(Error)"
									: "", m_BluetoothDeviceDebugString), null);
					if (WorkBluetoothAdapter != null) {
						WorkBluetoothAdapter.cancelDiscovery();
					}
					unregisterReceiver(m_BluetoothDeviceFoundBroadcastReceiver);
					FinishHandler.post(FinishRunnable);
				}
			};
			m_BluetoothDeviceDebugString = null;
			m_BluetoothDeviceFoundBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO 閾ｪ�?慕函謌舌＆繧後◆�?｡繧ｽ�?�ラ�?ｻ繧ｹ繧ｿ�?�?
					m_BluetoothDeviceDebugString = ((BluetoothDevice) intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
							.getName();
					WorkHandler.removeCallbacks(WorkRunnable);
					WorkHandler.post(WorkRunnable);
				}
			};
			registerReceiver(m_BluetoothDeviceFoundBroadcastReceiver,
					new IntentFilter(BluetoothDevice.ACTION_FOUND));
			if (WorkBluetoothAdapter != null) {
				WorkBluetoothAdapter.startDiscovery();
				WorkHandler
						.postDelayed(
								WorkRunnable,
								Integer.valueOf(((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
										.getText().toString()) * 1000);
			} else {
				WorkHandler.post(WorkRunnable);
			}
		} else {
			FinishHandler.post(FinishRunnable);
		}
	}

	private boolean CheckInputItem() {
		boolean Ret = true;
		if (Ret) {
			if (((EditText) findViewById(R.id.editText_on_period)).length() == 0) {
				Ret = false;
				AlertDialog_Ok("Error", "Invalid [On Period]");
				findViewById(R.id.editText_on_period).requestFocus();
			}
		}
		if (Ret) {
			if ((((CheckBox) findViewById(R.id.checkBox_ping_after_boot))
					.isChecked() || ((CheckBox) findViewById(R.id.checkBox_ping_before_reboot))
					.isChecked())
					&& (((EditText) findViewById(R.id.editText_ping_ip))
							.length() == 0 || ((EditText) findViewById(R.id.editText_ping_timeout))
							.length() == 0)) {
				Ret = false;
				if (((EditText) findViewById(R.id.editText_ping_ip)).length() == 0) {
					AlertDialog_Ok("Error", "Invalid [Ping Ip]");
					findViewById(R.id.editText_ping_ip).requestFocus();
				} else {
					AlertDialog_Ok("Error", "Invalid [Ping Timeout]");
					findViewById(R.id.editText_ping_timeout).requestFocus();
				}
			}
		}

		if (Ret) {
			if ((((CheckBox) findViewById(R.id.checkBox_bluetooth_device_after_boot))
					.isChecked() || ((CheckBox) findViewById(R.id.checkBox_bluetooth_device_before_reboot))
					.isChecked())
					&& ((EditText) findViewById(R.id.editText_bluetooth_device_timeout))
							.length() == 0) {
				Ret = false;
				AlertDialog_Ok("Error", "Invalid [Bluetooth Device Timeout]");
				findViewById(R.id.editText_bluetooth_device_timeout)
						.requestFocus();
			}
		}
		return (Ret);
	}

	private void OutputViewAppend(final String OutputString,
			Handler OutputHandler) {
		final ScrollView OutputResultScrollView = (ScrollView) findViewById(R.id.scrollView_output);
		final TextView OutputResultTextView = (TextView) findViewById(R.id.textView_output);

		if (OutputHandler == null) {
			OutputHandler = new Handler();
		}
		OutputHandler.post(new Runnable() {
			@Override
			public void run() {
				if (OutputString != null) {
					OutputResultTextView.append(OutputString);
				} else {
					OutputResultTextView.setText(null);
				}
			}
		});
		OutputHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				OutputResultScrollView.scrollTo(0,
						OutputResultTextView.getBottom());
			}
		}, 200);
	}

	private int ExecShellCommand(String Command) {
		int Ret = -1;

		try {
			Ret = Runtime.getRuntime().exec(Command).waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (Ret);
	}

	private synchronized void AlertDialog_Ok(String Title, String Message) {
		mErrorDialogShowed = true;
		new AlertDialog.Builder(this).setTitle(Title).setMessage(Message)
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mErrorDialogShowed = false;
					}
				}).create().show();

	}

	private void stopAlarmIntent() {
		Log.d(TAG, "stopAlarmIntent is called");
		if (mAlarmManager != null) {
			mAlarmManager.cancel(m_AlarmBootPendingIntent);
			mAlarmManager.cancel(m_AlarmRebootPendingIntent);
		}
	}
}
