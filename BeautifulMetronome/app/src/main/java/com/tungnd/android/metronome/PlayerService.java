package com.tungnd.android.metronome;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
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
 * As described by stackoverflow, it is not good with Media player and thread for metronome
 * but it is a good to try with background and soundpool
 * http://stackoverflow.com/questions/21043059/play-background-sound-in-android-applications
 * <p/>
 * http://stackoverflow.com/questions/5580537/androidsound-pool-and-service
 *
 * improve sound pool performance
 * http://www.thiagorosa.com.br/en/how-to/improve-soundpool-performance
 * The lagging is solved by adding dummy soulpool to play muted sound. It does help the beat play in the accurate frequency.
 */
public class PlayerService extends Service implements SoundPool.OnLoadCompleteListener, metronome {
    /**
     * One for play and one for play dummy muted sound
     */
    private final int NUMBER_OF_SOUNDPOOLS = 2;
    /**
     * Contain one main player and one dummy player
     */
    private SoundPool[] mSoundPools = new SoundPool[NUMBER_OF_SOUNDPOOLS];
    /**
     * List of metronome sound IDs: currently supports different 8 sounds
     */
    private int[] streamIds = new int[8];
    /**
     * Control the play/stop states
     */
    private boolean enabled = false;
    private boolean isResourceReady = false;
    private float volume = 1.0f;
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
    /**
     * select sound
     */
    private int soundIndex;

    @Override
    public void startBeat() {
        setEnabled(true);
        //Start the dummy muted sound so that it can remove the lagging issue
        //but the issue is only noticeable in under 170 BPM
        if(this.tempo < 170)
        {
           playDummySound();
        }
    }

    @Override
    public void stopBeat() {
        setEnabled(false);
        //stop dummy sound when stop
        mSoundPools[1].stop(streamIds[0]);
    }

    @Override
    public void changeSound() {
        soundIndex = ++soundIndex % streamIds.length;
        Log.d(this.toString(), "Sound index: " + soundIndex);
    }

    /**
     * a convenience method to play a sound
     */
    public void playCurrentSound(){
        mSoundPools[0].play(streamIds[soundIndex], volume, volume, 10, 0, 1.0f);
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
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
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build();
            for (int i = 0; i < mSoundPools.length; i++) {
                this.mSoundPools[i] = new SoundPool.Builder()
                        .setAudioAttributes(attributes)
                        .setMaxStreams(3)
                        .build();
            }
        } else {
            for (int i = 0; i < mSoundPools.length; i++) {
                this.mSoundPools[i] = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
            }

        }

        mSoundPools[0].setOnLoadCompleteListener(this);//set listener

        int soundId;
        for (int i = 0; i < streamIds.length; i++) {
            soundId = getResources().getIdentifier("snd" + i, "raw", getApplicationContext().getPackageName());
            for (SoundPool sp : mSoundPools) {
                streamIds[i] = sp.load(this, soundId, 1); // in 2nd param u have to pass your desire tone
            }
        }

    }

    public void onDestroy() {
        Log.d(this.toString(), "On destroy");
        if (mSoundPools != null) {
            timer.cancel();
            setEnabled(false);
            for (SoundPool sp : mSoundPools) {
                sp.release();
            }
        }
    }

    private void setEnabled(boolean start) {
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
            if(tempo <170){
                playDummySound();
            }
        }
    }

    private void playDummySound() {
        mSoundPools[1].stop(streamIds[0]);
        mSoundPools[1].play(streamIds[0], 0, 0, 0,-1, 1f);
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
        timer = new Timer("Metronome", true);
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
            isResourceReady = false;
        }
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

    /**
     * inner class TimerTask that plays the sound
     */
    private final class BeatTimerTask extends TimerTask {
        @Override
        public void run() {
            if (enabled && isResourceReady) {

                synchronized (PlayerService.this) {
                    //the first soundpool will be used to play the beat with highest priority
                    //but it does not help much for the lagging issue of soundpool
                    //so it is good to use a dummy muted sound by the other soundpool
                    mSoundPools[0].play(streamIds[soundIndex], volume, volume, 10, 0, 1.0f);

                    PlayerService.this.notify();//notify to draw the time signature on each beat
                }
            }
        }
    }
}
