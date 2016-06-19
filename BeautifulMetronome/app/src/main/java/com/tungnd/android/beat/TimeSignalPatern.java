package com.tungnd.android.beat;

/**
 * Created by tunguyen on 13/06/2016.
 */
public enum TimeSignalPatern {
    t4_4(2, 0, 1, 0, 1, 0, 1, 0),
    t3_4(2, 0, 1, 0, 1, 0),
    t2_4(2, 0, 1, 0),
    t6_8(2, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0);

    private int[] beatSequence;

    TimeSignalPatern(int... beatSequence) {
        this.beatSequence = beatSequence;
    }

    /**
     * @return Time signature as an integer array
     * <p/>
     * 2 for main beat
     * <p/>
     * 1 for sub-beats
     * <p/>
     * 0 for nothing
     */

    public int[] getBeatSequence() {
        return beatSequence;
    }
}
