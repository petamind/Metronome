package com.tungnd.android.metronome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tungnd.android.beat.BeatView;
import com.tungnd.android.beat.TimeSignature;
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
    private FloatingActionButton fab;
    private int maxVolumeMusicStream;
    private boolean mBound;
    private AudioManager audioManager;
    private int curVolume;
    private int timeSignatureIndex;
    private TextView tempoTextView;
    private TextView tempoNameTextView;
    private TextView timeSignatureTextView;
    private int isTapped6times;
    private long[] tappedTimes;
    private ImageButton tapTempoButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isPlaying = savedInstanceState.getBoolean("PLAYING");
        }
        setContentView(R.layout.activity_metronome_paralax);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //---detail
        this.tempoTextView = (TextView) findViewById(R.id.tempo_textview);
        this.tempoNameTextView = (TextView) findViewById(R.id.tempo_name_textview);
        this.timeSignatureTextView = (TextView) findViewById(R.id.time_signature_textview);

        //init tempo
        tempoSeekBar = (DiscreteSeekBar) findViewById(R.id.tempo_slider);
        tempoSeekBar.setOnProgressChangeListener(this);


        //init float button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        this.beatView = (BeatView) findViewById(R.id.visual);

        setupTableLayout();

        //init volume control
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        volumeSeekBar = (DiscreteSeekBar) findViewById(R.id.volumn_slider);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolumeMusicStream = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        volumeSeekBar.setOnProgressChangeListener(this);
        //----------------spinner
//        HorizontalSpinner horizontalSpinner = (HorizontalSpinner) findViewById(R.id.horizontal_spinner);
//        horizontalSpinner.setAdapter(new ArrayAdapter<String>(
//        this, android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.tempo_name)));
        ImageButton timeSignature = (ImageButton) findViewById(R.id.change_time_signature_btn);
        timeSignature.setOnClickListener(this);
        ImageButton changeSoundButton = (ImageButton) findViewById(R.id.change_sound_btn);
        changeSoundButton.setOnClickListener(this);
        tapTempoButton = (ImageButton) findViewById(R.id.tap_tempo_btn);
        tapTempoButton.setOnClickListener(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean("PLAYING", isPlaying);
        super.onSaveInstanceState(outState, outPersistentState);
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
                              final int ii;
                              final int jj;
                              if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                  ii = 2;
                              } else {
                                  ii = 3;
                              }
                              jj = metronome.tempos.length / ii;

                              for (int i = 0; i < ii; i++) {
                                  TableRow r = (TableRow) layoutInflater.inflate(R.layout.table_row, null);
                                  for (int j = 0; j < jj; j++) {
                                      Button b = (Button) layoutInflater.inflate(R.layout.button, null);
                                      b.setOnClickListener(MainActivity.this);
                                      b.setText("" + metronome.tempos[i * jj + j]);
                                      r.addView(b);
                                  }
                                  tableLayout.addView(r);
                              }
                          }
                      }

        );
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

    /**
     * 5 taps to set the tempo and start the metronome
     */
    private void tapToTempo() {
        Log.d("tap", isTapped6times +"");
        if (isTapped6times == 5) {
            this.tapTempoButton.setEnabled(false);
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(300);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isTapped6times = 0;
                    tapTempoButton.setEnabled(true);
                }
            }, 4000);
            //TODO set tempo
            long l = (this.tappedTimes[tappedTimes.length-2] - this.tappedTimes[0])/(tappedTimes.length-2);
            int tempo = (int) (60000 / l);
            Log.d("tap", l +":" +tempo);
            this.tempoSeekBar.setProgress(tempo);
            isPlaying = true;
            this.startBeat();
            updateUI();
            Toast.makeText(this, "New tempo is set!", Toast.LENGTH_LONG).show();
            return;
        }

        if (tappedTimes == null) {
            this.tappedTimes = new long[6];
        }
        this.stopBeat();
        playerService.playCurrentSound();
        this.tappedTimes[isTapped6times] = System.currentTimeMillis();
        Log.d("tap", this.tappedTimes[isTapped6times] +"");
        this.isTapped6times = ++isTapped6times % tappedTimes.length;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isTapped6times = 0;
            }
        }, 10000);//reset if tap is longer than 2.5 secs
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
            case R.id.fab:
                isPlaying = !isPlaying;
                startBeat();
                updateUI();
                break;
            case R.id.change_sound_btn: {
                changeSound();
                Snackbar.make(v, "Sound is changed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            }

            case R.id.tap_tempo_btn: {
                tapToTempo();
                if(isTapped6times==0 )
                Snackbar.make(v, "Tap 5 times to set tempo and start", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            }

            case R.id.change_time_signature_btn: {
                timeSignatureIndex = ++timeSignatureIndex % TimeSignature.values().length;
                beatView.setBeatSequence(TimeSignature.values()[timeSignatureIndex].getBeatSequence());
                this.timeSignatureTextView.setText(TimeSignature.values()[timeSignatureIndex].getName(timeSignatureIndex));
                Snackbar.make(v, "Time signature visualization is changed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            }

            case R.id.button: {
                String temp = ((Button) v).getText() + "";
                this.tempoSeekBar.setProgress(Integer.parseInt(temp));
                this.tempoTextView.setText(temp);
            }

            default:
                break;
        }

    }

    private void updateUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isPlaying) {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause, this.getTheme()));
            } else {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play, this.getTheme()));
            }
        } else {
            if (isPlaying) {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
            } else {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
            }
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
        volumeSeekBar.setProgress(100 * curVolume / maxVolumeMusicStream);
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
                this.tempoTextView.setText(tempoSeekBar.getProgress() + "");
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
                if (isPlaying) {
                    playerService.startBeat();
                }
                //change tempo
                break;
        }
    }
}
