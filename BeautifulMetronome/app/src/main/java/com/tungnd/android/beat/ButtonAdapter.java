package com.tungnd.android.beat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.tungnd.android.metronome.R;

/**
 * Created by tungs on 6/14/2016.
 * create 3x4 buttons: 20->240 for quick select tempo
 * 20/30/45/60/
 * 75/90/100/120
 * 150/170/200/240
 * @deprecated as the gridview does not work well in CoordinatorLayout
 */
public class ButtonAdapter extends BaseAdapter {
    private final Context context;
    private LayoutInflater layoutInflater;
    private static final int[] tempos = {20, 30, 45, 60,
            75, 90, 100, 120,
            150, 170, 200, 240};

    public ButtonAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
    }

    @Override
    public int getCount() {
        return tempos.length;
    }

    @Override
    public Object getItem(int position) {
        return tempos[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button button ;

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            button = (Button) layoutInflater.inflate(R.layout.button, null);
            button.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        } else {
            button = (Button) convertView;
        }

        button.setText(tempos[position] + "");
        return button;
    }
}
