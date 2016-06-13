package com.tungnd.android.beautifulmetronome;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by tungs on 6/10/2016.
 * Visualize beats with simple circles
 */
public class BeatView extends View implements metronome {
    private int tempo = 60;
    /**
     * cirle diameter
     */
    private int diameter;
    private int x;
    private int y;
    private Paint paint;
    private long mAnimStartTime;

    private int[] beatSequence = TimeSignalPatern.t4_4.getBeatSequence();
    private int beatIndex = 0;
    private Handler mHandler = new Handler();
    private Runnable mTick;


    public BeatView(Context context) {
        super(context);
    }

    public BeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BeatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        this.beatIndex = 0;
        this.x = getWidth() / 2;
        this.y = getHeight() / 2;
        this.setDiameter(Math.min(x, y) / 2);
        setTempo(tempo);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    /**
     * Set tempo and time in milliseconds
     *
     * @param tempo 30-300
     */
    public void setTempo(int tempo) {
        this.tempo = tempo;
        this.mAnimStartTime = 60000 / tempo / 2;
    }

    /**
     * Draw circles based on beat pattern (time signature)
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        paint.setColor(getResources().getColor(R.color.colorAccent));

        System.out.println(x + ": " + y + ": " + beatIndex);

        switch (beatSequence[beatIndex]) {
            case 2:
                canvas.drawCircle(x, y, diameter, paint);
                break;
            case 1:
                canvas.drawCircle(x, y, diameter / 2, paint);
                break;
            default:
                canvas.drawCircle(x, y, 0, paint);
        }

        beatIndex = ++beatIndex % beatSequence.length;
    }

    @Override
    public void startBeat() {
        if (mTick == null) {
            mTick = new Runnable() {
                public void run() {
                    invalidate();
                    mHandler.postDelayed(this, mAnimStartTime);
                }
            };
        }
        Toast.makeText(getContext(), "start beat", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(mTick);
        mHandler.postDelayed(mTick, mAnimStartTime);
    }

    @Override
    public void stopBeat() {
        mHandler.removeCallbacks(mTick);
        mTick = null;
        Toast.makeText(getContext(), "Stop beat", Toast.LENGTH_SHORT).show();
    }
}
