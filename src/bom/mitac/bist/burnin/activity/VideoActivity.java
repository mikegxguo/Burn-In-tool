package bom.mitac.bist.burnin.activity;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import bom.mitac.bist.burnin.module.BISTApplication;
import bom.mitac.bist.burnin.test.VideoTest;
import bom.mitac.bist.burnin.util.TimeStamp;

import bom.mitac.bist.burnin.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-4-1
 * Time: 上午10:02
 */
public class VideoActivity extends Activity implements SurfaceHolder.Callback {

    private Timer timer;
    private boolean isTesting;
    private boolean adjustVolume;
    private long time;
    private String videoPath;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_video);

        videoPath = getIntent().getStringExtra(BISTApplication.VIDEO_PATH);
        time = getIntent().getLongExtra(BISTApplication.VIDEO_TIME, 60000);
        time = time * 1000;
        adjustVolume = getIntent().getBooleanExtra(BISTApplication.VIDEO_VOLUME, false);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.sv_video);
        if (surfaceView.getVisibility() != View.VISIBLE) {
            saveLog("Failed");
            VideoTest.isVideoTestPassed = false;
            this.finish();
        }
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
    }

    @Override
    protected void onDestroy() {
        if (isTesting) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    private void saveLog(String log) {
        log = TimeStamp.getTimeStamp(TimeStamp.TimeType.FULL_L_TYPE) + " |" + BISTApplication.ID_NAME.get(BISTApplication.VideoTest_ID) + "| " + log + "\r\n";
        VideoTest.saveLog(log);
    }

    private void play(MediaPlayer mediaPlayer, SurfaceHolder surfaceHolder, String videoPath) {
        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopTest() {
        isTesting = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        this.finish();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        if (isTesting) {

        } else {
            isTesting = true;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    play(mediaPlayer, surfaceHolder, videoPath);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                    saveLog("Video is ERROR! MediaPlayer="+mediaPlayer+" what="+i+" Extra="+i2);
                    mediaPlayer.release();
                    VideoTest.isVideoTestPassed = false;
                    stopTest();
                    return true;
                }
            });

            saveLog("Playing the video");
            play(mediaPlayer, surfaceHolder, videoPath);
            timer = new Timer();
            if (adjustVolume) {
                timer.schedule(new TimerTask() {
                    boolean rise = true;
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int volumeMAX = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    @Override
                    public void run() {
                        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if (volume == 0) {
                            rise = true;
                        } else if (volume == volumeMAX) {
                            rise = false;
                        }

                        if (rise) {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        } else {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        }
                    }
                }, 0, 1000);
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        saveLog("Video is playing");
                        VideoTest.isVideoTestPassed = true;
                        mediaPlayer.stop();
                    } else {
                        saveLog("Video is not playing");
                        VideoTest.isVideoTestPassed = false;
                    }
                    mediaPlayer.release();
                    stopTest();
                }
            }, time);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }
}
