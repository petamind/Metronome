package com.tungnd.android.metronome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.tungnd.android.beat.BeatView;
import com.tungnd.android.beat.metronome;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Main activity to isPlaying app
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, metronome, DiscreteSeekBar.OnProgressChangeListener {

    private static PlayerService playerService;
    private BeatView beatView;
    private boolean isPlaying;
    private Intent svc;
    private boolean doubleBackToExitPressedOnce;
    private DiscreteSeekBar tempoSeekBar;
    private DiscreteSeekBar volumeSeekBar;
    private int maxVolumeMusicStream;
    private boolean mBound;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d("Service connected", this.toString());
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            playerService = binder.getService();
            mBound = true;
            setVolume(0);//sync current volume
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("Service disconnected", this.toString());
            mBound = false;
        }
    };
    private AudioManager audioManager;
    private int curVolume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome_paralax);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button changeSoundButton = (Button) findViewById(R.id.change_sound_btn);
        changeSoundButton.setOnClickListener(this);

        //init tempo
        tempoSeekBar = (DiscreteSeekBar) findViewById(R.id.tempo_slider);
        tempoSeekBar.setOnProgressChangeListener(this);


        //init float button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        this.beatView = (BeatView) findViewById(R.id.visual);
        beatView.setOnClickListener(this);

        setupTableLayout();

        //init volume control
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        volumeSeekBar = (DiscreteSeekBar) findViewById(R.id.volumn_slider);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolumeMusicStream = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setOnProgressChangeListener(this);

    }

    /**
     * Create table of tempos
     */
    private void setupTableLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
                LayoutInflater layoutInflater = getLayoutInflater();
                for (int i = 0; i < 3; i++) {
                    TableRow r = (TableRow) layoutInflater.inflate(R.layout.table_row, null);
                    for (int j = 0; j < 4; j++) {
                        Button b = (Button) layoutInflater.inflate(R.layout.button, null);
                        b.setOnClickListener(MainActivity.this);
                        b.setText("" + metronome.tempos[i * 4 + j]);
                        r.addView(b);
                    }
                    tableLayout.addView(r);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
//        svc = new Intent(this, PlayerService.class);
//        bindService(svc, mConnection, Context.BIND_AUTO_CREATE);
//        startService(svc);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        svc = new Intent(this, PlayerService.class);
        bindService(svc, mConnection, Context.BIND_AUTO_CREATE);
        startService(svc);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            Log.d("onPause", "unbind the service connection " + mConnection);
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                setVolume(0);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                setVolume(0);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBeat();
    }

    /**
     * In general, click on the Beat View will isPlaying the beat
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.visual: {
                isPlaying = !isPlaying;
                startBeat();
                break;
            }
            case R.id.change_sound_btn: {
                changeSound();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void startBeat() {
        if (isPlaying) {
            beatView.setBeatLock(playerService);//sync with sound
            playerService.startBeat();
            beatView.startBeat();
        } else {
            stopBeat();
        }
    }

    @Override
    public void stopBeat() {
        if (svc != null) {
            playerService.stopBeat();
            beatView.stopBeat();
        }
    }

    @Override
    public void changeSound() {
        playerService.changeSound();
    }

    @Override
    public void setVolume(float volume) {
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setProgress(100 * curVolume / maxVolumeMusicStream );
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.volumn_slider:
                //change volume
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeSeekBar.getProgress() * maxVolumeMusicStream / 100, 0);
                }
                playerService.setVolume(((float) volumeSeekBar.getProgress()) / 100);
                break;
            case R.id.tempo_slider:
                playerService.setTempo(tempoSeekBar.getProgress());
                //change tempo
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.tempo_slider:
                playerService.stopBeat();
                //change tempo
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.tempo_slider:
                //playerService.startBeat();
                //change tempo
                break;
        }
    }
}
