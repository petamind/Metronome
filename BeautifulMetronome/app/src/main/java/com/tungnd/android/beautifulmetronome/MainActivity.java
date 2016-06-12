package com.tungnd.android.beautifulmetronome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Main activity to start app
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TempoView tempoView;
    private boolean start;
    private BeatPlayer beatPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tempoView = (TempoView) findViewById(R.id.visual);
        tempoView.setOnClickListener(this);
        this.beatPlayer = new BeatPlayer(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.visual:
            {
                start = !start;
                if(start)
                {
                    Intent svc=new Intent(this, PlayerService.class);
                    svc.setAction(PlayerService.ACTION_PLAY);
                    startService(svc);
                    Toast.makeText(this, "start svc", Toast.LENGTH_SHORT).show();
//                    tempoView.startBeat();
//                    this.beatPlayer.play();
                } else {
//                    tempoView.stopBeat();
//                    if(this.beatPlayer!=null){
//                        beatPlayer.stop();
//                    }
                }
            }
        }
    }
}
