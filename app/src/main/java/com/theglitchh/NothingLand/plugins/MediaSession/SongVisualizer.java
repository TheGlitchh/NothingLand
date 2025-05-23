package com.theglitchh.NothingLand.plugins.MediaSession;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.theglitchh.NothingLand.services.OverlayService;

public class SongVisualizer extends View {
    Visualizer visualizer;

    public SongVisualizer(Context context) {
        super(context);
        this.ctx = (OverlayService) context;
        init();
    }

    public SongVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = (OverlayService) context;
        init();
    }

    public SongVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.ctx = (OverlayService) context;
        init();
    }

    private OverlayService ctx;
    private int currentColor = Color.BLUE;
    public boolean paused;
    private byte[] bytes;

    public void setPlayerId(int sessionID) {
        try {
            if (visualizer != null) {
                release();
                visualizer = null;
            }
            visualizer = new Visualizer(sessionID);
            visualizer.setEnabled(false);
            visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                  int samplingRate) {
                    SongVisualizer.this.bytes = bytes;
                    if (!paused) invalidate();
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                             int samplingRate) {
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);


            visualizer.setEnabled(true);
        } catch (Exception e) {
            // do nothing lol
        }
    }

    public void release() {
        //will be null if setPlayer hasn't yet been called
        if (visualizer == null)
            return;

        visualizer.release();
        bytes = null;
        invalidate();
    }
    private void init() {
        IntentFilter filter = new IntentFilter(ctx.getPackageName() + ".COLOR_CHANGED");
        ctx.registerReceiver(receiver, filter);

        if (paint.getColor() != currentColor) {
            setColor(currentColor);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setColor(int color) {
        if (paint.getColor() != color) {
            paint.setColor(color);
            currentColor = color;
        }
    }
    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ctx.getPackageName() + ".COLOR_CHANGED")) {
                    int newColor = intent.getIntExtra("Allaccent_color", Color.RED);

                    // Only update if the new color is different from the current one
                    if (newColor != currentColor) {
                        currentColor = newColor;
                        paint.setColor(newColor);
                    }
                }
            }
        }
    };

    private final Paint paint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        float density = 3;

        if (bytes != null) {
            float barWidth = getWidth() / density;
            float div = bytes.length / density;
            paint.setStrokeWidth(barWidth - 4);

            for (int i = 0; i < density; i++) {
                int bytePosition = (int) Math.ceil(i * div);
                float barX = (i * barWidth) + (barWidth / 2);
                if (bytes[bytePosition] == 0 || bytes[bytePosition] + 128 == 0) {
                    canvas.drawLine(barX, (getHeight() / 2f), barX, (getHeight() / 2f), paint);
                } else {
                    int top = (getHeight() - 20) +
                            ((byte) (Math.abs(bytes[bytePosition]) + 128)) * (getHeight() - 20) / 128;
                    canvas.drawLine(barX, ((getHeight() + 20) - top) / 2f, barX, top, paint);
                }
            }
            super.onDraw(canvas);
        }
    }
}

