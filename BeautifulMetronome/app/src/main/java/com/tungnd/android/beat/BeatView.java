package com.tungnd.android.beat;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tungnd.android.metronome.R;

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
    private boolean isBeating = false;

    private int[] beatSequence = TimeSignalPatern.t4_4.getBeatSequence();
    private int beatIndex = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };
    private Runnable mTick = new Runnable() {
        public void run() {
            synchronized (beatLock) {
                while (isBeating) {
                    try {
                        beatLock.wait();
                        mHandler.sendEmptyMessage(0);
                        Thread.sleep(mAnimStartTime / 4);
                        mHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    ;
    /**
     * To synchronize the visual with sound
     */
    private Object beatLock;

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

    public void setBeatLock(Object beatLock) {
        this.beatLock = beatLock;
    }

    /**
     * Draw circles based on beat pattern (time signature)
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(getResources().getColor(R.color.colorAccent));
        }

        Log.d(this.toString(), x + ": " + y + ": " + beatIndex);

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
        setBeating(true);
        new Thread(mTick).start();
    }

    private void setBeating(boolean beating) {
        this.isBeating = beating;
    }

    @Override
    public void stopBeat() {
        setBeating(false);
    }
}
