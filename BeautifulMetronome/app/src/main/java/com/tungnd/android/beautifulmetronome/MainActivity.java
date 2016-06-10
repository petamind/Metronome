package com.tungnd.android.beautifulmetronome;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Main activity to start app
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TempoView tempoView;
    private boolean start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tempoView = (TempoView) findViewById(R.id.visual);
        tempoView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.visual:
            {
                start = !start;
                if(start)
                {
                    tempoView.startBeat();
                } else {
                    tempoView.stopBeat();
                }
            }
        }
    }
}
