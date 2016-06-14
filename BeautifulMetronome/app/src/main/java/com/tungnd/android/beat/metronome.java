package com.tungnd.android.beat;

/**
 * Created by tungs on 6/10/2016.
 */
public interface metronome {
    int[] tempos = {20, 30, 45, 60,
            75, 90, 100, 120,
            150, 170, 200, 240};

    /**
     * Start the beat with tempo
     */
    void startBeat();
    /**
     * Stop the beat with tempo
     */
    void stopBeat();
}
