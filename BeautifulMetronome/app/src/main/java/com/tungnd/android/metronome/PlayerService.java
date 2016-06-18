package com.tungnd.android.metronome;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.tungnd.android.beat.metronome;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tungs on 6/10/2016.
 * As described by stackoverflow, it is not good with Media player and thread for metronome but it is a good to try with background and soundpool
 * http://stackoverflow.com/questions/21043059/play-background-sound-in-android-applications
 * <p/>
 * http://stackoverflow.com/questions/5580537/androidsound-pool-and-service
 */
public class PlayerService extends Service implements SoundPool.OnLoadCompleteListener, metronome {
    private final int NUMBER_OF_SOUNDPOOLS = 1;
    private SoundPool[] mSoundPools = new SoundPool[NUMBER_OF_SOUNDPOOLS];
    private int soundPoolIndex;
    private AudioAttributes attributes;
    private int soundId;
    private boolean enabled = false;
    private boolean isResourceReady = false;
    private Timer timer;
    private IBinder mIBinder = new LocalBinder();
    /**
     * tempo of metronome, default 60BPM
     */
    private int tempo = 60;
    /**
     * time in miliseconds between 2 beats
     */
    private long beatInterval;

    @Override
    public void startBeat() {
        setEnabled(true);
    }

    @Override
    public void stopBeat() {
        setEnabled(false);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    private final class BeatTimerTask extends TimerTask {

        @Override
        public void run() {
            if (enabled && isResourceReady) {
                //Toast.makeText(PlayerService.this, "Play now ", Toast.LENGTH_SHORT).show();
                // synchronized (PlayerService.this) {

                soundPoolIndex = ++soundPoolIndex % mSoundPools.length;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mSoundPools[soundPoolIndex].play(soundId, 1, 1, 0, 0, 1.0f);
                    }
                }
                ).start();
//                    PlayerService.this.notify();
                //}
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.toString(), "On bind");
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //soundPool = ... // initialize it here
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            for (int i = 0; i < mSoundPools.length; i++) {
                this.mSoundPools[i] = new SoundPool.Builder()
                        .setAudioAttributes(attributes)
                        .setMaxStreams(5)
                        .build();
            }
        } else {
            for (int i = 0; i < mSoundPools.length; i++) {
                this.mSoundPools[i] = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            }

        }

        mSoundPools[0].setOnLoadCompleteListener(this);//set listener

//        mSoundPools[1].load(this, R.raw.snd0, 1);
//        mSoundPools[2].load(this, R.raw.snd0, 1);
        soundId = mSoundPools[0].load(this, R.raw.snd0, 1); // in 2nd param u have to pass your desire tone
    }

    public void onDestroy() {
        Log.d(this.toString(), "On destroy");
        if (mSoundPools != null) {
            setEnabled(false);
            for (SoundPool sp : mSoundPools) {
                sp.release();
            }
            timer.cancel();
        }
    }

    public void setEnabled(boolean start) {
        this.enabled = start;
    }

    /**
     * set tempo and also the beat interval for the metronome
     *
     * @param tempo integer value from 20-300
     */
    public void setTempo(int tempo) {
        Log.d("tempo", tempo + "");
        this.tempo = tempo;
        if (tempo > 0) {
            setBeatInterval(60000 / tempo);
        }
    }

    public int getTempo() {
        return tempo;
    }

    private void setBeatInterval(long beatInterval) {
        this.beatInterval = beatInterval;
        setTimer();
    }

    /**
     * start timer with beatInterval
     */
    private void setTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new BeatTimerTask(), 0L, beatInterval);
    }

    /**
     * When sound loading complete, start the timer
     *
     * @param soundPool
     * @param sampleId
     * @param status
     */
    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status == 0) {
            Log.d(this.toString(), "Load sounds complete");
            isResourceReady = true;
            setTempo(60);
        } else {
            Toast.makeText(this, "Error loading media files", Toast.LENGTH_SHORT).show();
        }
    }
}
