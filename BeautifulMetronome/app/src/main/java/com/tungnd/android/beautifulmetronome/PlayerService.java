package com.tungnd.android.beautifulmetronome;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tungs on 6/10/2016.
 * As described by stackoverflow, it is not good with Media player and thread for metronome but it is a good to try with background and soundpool
 * http://stackoverflow.com/questions/21043059/play-background-sound-in-android-applications
 * <p/>
 * http://stackoverflow.com/questions/5580537/androidsound-pool-and-service
 */
public class PlayerService extends Service implements SoundPool.OnLoadCompleteListener {

    public static final String ACTION_PLAY = "com.tungnd.android.action.PLAY";
    public static final String ACTION_STOP = "com.tungnd.android.action.STOP";
    private SoundPool mSoundPool;
    private AudioAttributes attributes;
    private int soundId;
    private boolean enabled = true;
    private Timer timer;
    private final TimerTask beatTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (enabled && mSoundPool != null) {
                //Toast.makeText(PlayerService.this, "Play now ", Toast.LENGTH_SHORT).show();
                mSoundPool.play(soundId, 1, 1, 0, 0, 1);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.toString(), "On bind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service " + intent.getAction(), Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals(ACTION_PLAY)) {
            timer = new Timer();
            //soundPool = ... // initialize it here
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                this.mSoundPool = new SoundPool.Builder()
                        .setAudioAttributes(attributes)
                        .build();
            } else {
                mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            }

            mSoundPool.setOnLoadCompleteListener(this);//set listener

            soundId = mSoundPool.load(this, R.raw.snd0, 1); // in 2nd param u have to pass your desire ringtone

        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d(this.toString(), "On destroy");
        if (mSoundPool != null) {
            setEnabled(false);
            mSoundPool.release();
            timer.cancel();
        }
    }

    public void setEnabled(boolean start) {
        this.enabled = start;
    }

    /**
     * When sound loading complete
     *
     * @param soundPool
     * @param sampleId
     * @param status
     */
    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status == 0) {
            Log.d(this.toString(), "Load complete");
            timer.scheduleAtFixedRate(beatTimerTask, 0L, 1000);

        } else {
            Toast.makeText(this, "Error loading media file", Toast.LENGTH_SHORT).show();
        }
    }
}
