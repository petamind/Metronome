package com.tungnd.android.beat;

/**
 * Created by tunguyen on 13/06/2016.
 */
public enum TimeSignature {
    t4_4(2, 0, 1, 0, 1, 0, 1, 0),
    t3_4(2, 0, 1, 0, 1, 0),
    t2_4(2, 0, 1, 0),
    t6_8(2, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0);

    private int[] beatSequence;

    TimeSignature(int... beatSequence) {
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

    public String getName(int index) {
        switch (index) {
            case 0:
                return "4/4";
            case 1:
                return "3/4";
            case 2:
                return "2/4";
            case 3:
                return "6/8";
            default:
                break;
        }
        return "X/Y";
    }
}
