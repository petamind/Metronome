package com.tungnd.android.beautifulmetronome;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by tungs on 6/11/2016.
 * Use sound pool to play beats
 * <link>http://www.101apps.co.za/index.php/articles/using-android-s-soundpool-class-a-tutorial.html</link>
 *
 * http://stackoverflow.com/questions/17069955/play-sound-using-soundpool-example
 * I prefer the background service so this class is replaced by PlayerService
 *
 * @deprecated
 */
public class BeatPlayer implements Runnable{
    private SoundPool mSoundPool;
    private AudioAttributes attributes;
    int soundId;

    public BeatPlayer(Context context) {
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

       soundId = mSoundPool.load(context, R.raw.snd0, 1); // in 2nd param u have to pass your desire ringtone


    }

    public void play(){
        mSoundPool.play(soundId, 1, 1, 0, 0 , 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    synchronized (this) {
                    //mSoundPool.pause(soundId);
                        try {
                            this.wait(500);
                            mSoundPool.play(soundId, 1, 1, 0, 0 , 1);
                            //this.wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void stop(){
        this.mSoundPool.release();
    }

    @Override
    public void run() {

    }
}
