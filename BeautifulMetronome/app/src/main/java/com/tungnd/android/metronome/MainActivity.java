package com.tungnd.android.metronome;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.tungnd.android.beat.BeatView;
import com.tungnd.android.beat.Tempo;
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
    private int timeSignatureIndex;
    private TextView tempoTextView;
    private TextView tempoNameTextView;
    private TextView timeSignatureTextView;
    private int isTapped6times;
    private long[] tappedTimes;
    private ImageButton tapTempoButton;
    private AlertDialog alertDialog;
    private AdView adView;
    private NotificationManager mNotificationManager;
    /**
     * TODO: setup in app purchase
     */
    private boolean isPremium;
    private InterstitialAd mInterstitialAd;
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
            playerService.setTempo(tempoSeekBar.getProgress());
            mBound = true;
            setVolume(0);//sync current volume
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("Service disconnected", this.toString());
            mBound = false;
        }
    };
    private int mId = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_metronome_paralax);

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
        ImageButton timeSignature = (ImageButton) findViewById(R.id.change_time_signature_btn);
        timeSignature.setOnClickListener(this);
        ImageButton changeSoundButton = (ImageButton) findViewById(R.id.change_sound_btn);
        changeSoundButton.setOnClickListener(this);
        tapTempoButton = (ImageButton) findViewById(R.id.tap_tempo_btn);
        tapTempoButton.setOnClickListener(this);
        //-------ads
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interestial));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
        if (!isPremium) {
            adView = (AdView) findViewById(R.id.adview);
            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice("60551B196128C8441C4EBC953EE4CB48")
                    .build();
            adView.loadAd(adRequest);
        }

        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        if (savedInstanceState != null) {
            //isPlaying = savedInstanceState.getBoolean("PLAYING");
            tempoSeekBar.setProgress(savedInstanceState.getInt("TEMPO"));
            timeSignatureIndex = savedInstanceState.getInt("TIMESIGN");
            beatView.setBeatSequence(TimeSignature.values()[timeSignatureIndex].getBeatSequence());
            Log.d("onCreate", "play state: " + isPlaying +": "+tempoSeekBar.getProgress());
        } else {
            tempoSeekBar.setProgress(Tempo.DEFAULT_TEMPO);
        }

    }


    private void notification() {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, mId,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_metronome)
                        .setContentTitle(getString(R.string.app_name))
                        //.addAction(android.R.drawable.ic_media_pause, getString(R.string.pause), pendingIntent)
                        //.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(android.R.string.cancel), pendingIntent)
                        .setOngoing(true)
                        .setContentText(tempoSeekBar.getProgress() + " BPM (" + tempoNameTextView.getText() + ")")
                        .setAutoCancel(false);
        mBuilder.setContentIntent(pendingIntent);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("PLAYING", isPlaying);
        outState.putInt("TEMPO", tempoSeekBar.getProgress());
        outState.putInt("TIMESIGN", timeSignatureIndex);
        Log.d("onSave", "playing " + isPlaying);
        super.onSaveInstanceState(outState);
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

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        if (svc == null) {
            svc = new Intent(this, PlayerService.class);
        }
        bindService(svc, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "resume");
        mNotificationManager.cancelAll();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "pause");
        if (isPlaying) {
            notification();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBeat();
        // Unbind from the service
        if (mBound) {
            Log.d("stop beat", "unbind the service connection " + mConnection);
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * 5 taps to set the tempo and start the metronome
     */
    private void tapToTempo() {
        Log.d("tap", isTapped6times + "");
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
            long l = (this.tappedTimes[tappedTimes.length - 2] - this.tappedTimes[0]) / (tappedTimes.length - 2);
            int tempo = (int) (60000 / l);
            Log.d("tap", l + ":" + tempo);
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
        Log.d("tap", this.tappedTimes[isTapped6times] + "");
        this.isTapped6times = ++isTapped6times % tappedTimes.length;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isTapped6times = 0;
            }
        }, 10000);//reset if tap is longer than 2.5 secs
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            stopBeat();
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            isPlaying = false;
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
            case KeyEvent.KEYCODE_BACK:
                this.onBackPressed();
                return true;
            default:
                return false;
        }
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

                Snackbar snackbar = Snackbar.make(v, R.string.sound_changed, Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                snackbar.show();
                break;
            }

            case R.id.tap_tempo_btn: {
                tapToTempo();
                if (isTapped6times == 0)
                    Snackbar.make(v, R.string.tap2temp, Snackbar.LENGTH_LONG)
                            .show();
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
        beatView.setBeatLock(playerService);//sync with sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isPlaying) {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause, this.getTheme()));
                beatView.startBeat();
            } else {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play, this.getTheme()));
            }
        } else {
            if (isPlaying) {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                beatView.startBeat();
            } else {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
            }
        }
    }

    @Override
    public void startBeat() {
        if (isPlaying) {
            Log.d("start beat", "start");

            startService(svc);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            //beatView.setBeatLock(playerService);//sync with sound
            playerService.startBeat();
            beatView.startBeat();
            if (adView != null) {
                adView.setVisibility(View.INVISIBLE);
            }
        } else {
            stopBeat();

            if (adView != null && !isPremium) {
                adView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void stopBeat() {
        if (svc != null) {
            playerService.stopBeat();
            beatView.stopBeat();
            stopService(svc);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }



    @Override
    public void changeSound() {
        playerService.changeSound();
    }

    @Override
    public void setVolume(float volume) {
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
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
                this.tempoNameTextView.setText(Tempo.getTempoName(this, tempoSeekBar.getProgress()));
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

    /**
     * Share to social media
     *
     * @param view
     */
    public void share(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }

    /**
     * Rate this best app
     *
     * @param view
     */
    public void rate(View view) {
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        Toast.makeText(this, R.string.rate_text, Toast.LENGTH_LONG).show();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void help(View view) {
        if (alertDialog == null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            // ...Irrelevant code for customizing the buttons and title
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.help_layout, null);
            dialogBuilder.setView(dialogView);
            alertDialog = dialogBuilder.create();
            dialogView.findViewById(R.id.linearlayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
        }

        alertDialog.show();
    }

    public void decreaseTempo(View view) {
        int tempo = this.tempoSeekBar.getProgress();
        if (tempo > 20) {
            this.tempoSeekBar.setProgress(--tempo);
        }
    }

    public void decreaseVolume(View view) {
        int vol = this.volumeSeekBar.getProgress();
        if (vol > 0) {
            this.volumeSeekBar.setProgress(--vol);
        }
    }
}
