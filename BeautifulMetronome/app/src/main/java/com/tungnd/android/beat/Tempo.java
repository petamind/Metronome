package com.tungnd.android.beat;

/**
 * Created by tunguyen on 14/06/2016.
 * It will be harder for translation tempos
 * TODO:
 */
public enum  Tempo {
    GRAVE("GRAVE", new int[]{20, 35});
    private String name;
    private int[] range;
    Tempo(String name, int[] range){
        this.name = name;
        this.range = range;
    }
}
