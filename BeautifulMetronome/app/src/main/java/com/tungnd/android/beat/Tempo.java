package com.tungnd.android.beat;

import android.content.Context;

import com.tungnd.android.metronome.R;

/**
 * Created by tunguyen on 14/06/2016.
 */
public class Tempo {
    public final static int DEFAULT_TEMPO = 100;
    static String[] names;
    static int[] tempos;

    public static String getTempoName(Context c, int tempo){
        if(names==null){
            names = c.getResources().getStringArray(R.array.tempo_name);
            tempos = c.getResources().getIntArray(R.array.tempo_vals);
        }
        for (int i = 0; i < names.length; i++) {
            if(tempo<= tempos[i]){
                return names[i];
            }
        }
        return null;
    }
}
