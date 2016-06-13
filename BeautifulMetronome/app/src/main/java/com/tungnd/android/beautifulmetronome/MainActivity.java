package com.tungnd.android.beautifulmetronome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Main activity to start app
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, metronome {

    private BeatView beatView;
    private boolean start;
    private PlayerService playerService;
    private Intent svc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.beatView = (BeatView) findViewById(R.id.visual);
        beatView.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startBeat();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.visual: {
                start = !start;
                startBeat();
            }
        }
    }

    @Override
    public void startBeat() {
        if (start) {
            stopBeat();
            svc = new Intent(this, PlayerService.class);
            svc.setAction(PlayerService.ACTION_PLAY);
            startService(svc);
            beatView.startBeat();
        } else {
            stopBeat();
        }
    }

    @Override
    public void stopBeat() {
        if(svc != null)
        {
            stopService(svc);
            beatView.stopBeat();
        }
    }
}
