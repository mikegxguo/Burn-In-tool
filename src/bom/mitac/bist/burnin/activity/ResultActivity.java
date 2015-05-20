package bom.mitac.bist.burnin.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.util.GetFinalResult;
import bom.mitac.bist.burnin.util.Recorder;
import bom.mitac.bist.burnin.util.TimeStamp;

import bom.mitac.bist.burnin.R;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-6-13
 * Time: 上午10:52
 */
public class ResultActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.activity_result);
        TextView tvResult = (TextView)findViewById(R.id.tv_result);
        RelativeLayout rlBackground = (RelativeLayout)findViewById(R.id.rl_background);

        Recorder recorder = Recorder.read();
        if (recorder == null) {
            recorder = Recorder.getInstance();
        } else if (recorder.cycle==0) {
            // recorder.xml file is wrong
            tvResult.setText("Sorry, recorder file is broken, please go to refer the test log");
        } else {
            // recorder.xml file is OK
            if (GetFinalResult.GetFinalResult()) {
                rlBackground.setBackgroundColor(Color.GREEN);
                tvResult.setText(GetFinalResult.span);
                tvResult.append("\r\n" + "Reset Times: " + recorder.resetTimes + "\r\nTest Result: PASS\r\n");
            } else {
                rlBackground.setBackgroundColor(Color.RED);
                tvResult.setText(GetFinalResult.span);
                tvResult.append("\r\n" + "Reset Times: " + recorder.resetTimes + "\r\nTest Result: FAIL\r\n");
            }
        }
    }
}
