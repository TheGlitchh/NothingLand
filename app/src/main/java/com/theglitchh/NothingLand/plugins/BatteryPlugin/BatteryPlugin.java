package com.theglitchh.NothingLand.plugins.BatteryPlugin;
import android.content.SharedPreferences;
import android.os.Handler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theglitchh.NothingLand.R;
import com.theglitchh.NothingLand.plugins.BasePlugin;
import com.theglitchh.NothingLand.services.OverlayService;
import com.theglitchh.NothingLand.utils.SettingStruct;
import com.theglitchh.NothingLand.views.BatteryImageView;

import java.util.ArrayList;

public class BatteryPlugin extends BasePlugin {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ctx.getPackageName() + ".COLOR_CHANGED")) {
                    // Handle color change, update UI or settings accordingly
                    int newColor = intent.getIntExtra("Allaccent_color", Color.RED);
                    // Example: update the visualizer's color
                    tv.setTextColor(newColor);
                }
            }
        }
    };
    @Override
    public String getID() {
        return "BatteryPlugin";
    }

    @Override
    public String getName() {
        return "Battery Plugin";
    }

    private OverlayService ctx;

    @Override
    public void onCreate(OverlayService context) {
        ctx = context;
        ctx.registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private View mView;

    @Override
    public View onBind() {
        mView = LayoutInflater.from(ctx).inflate(R.layout.battery_layout, null);
        init();
        return mView;
    }

    private void init() {
        tv = mView.findViewById(R.id.text_percent);
        batteryImageView = mView.findViewById(R.id.cover);
        updateView();
    }

    float batteryPercent;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getExtras().getInt(BatteryManager.EXTRA_STATUS);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
            if (isCharging) {
                ctx.enqueue(BatteryPlugin.this);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPercent = level * 100 / (float) scale;
                updateView();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Dequeue after 3 seconds
                        ctx.dequeue(BatteryPlugin.this);
                    }
                }, 3000); // 3000 milliseconds (3 seconds)
            } else {
                ctx.dequeue(BatteryPlugin.this);
                if (tv != null && batteryImageView != null) {
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, ctx.dpToInt(0));
                    valueAnimator.setDuration(300);
                    valueAnimator.addUpdateListener(valueAnimator1 -> {
                        ViewGroup.LayoutParams p = batteryImageView.getLayoutParams();
                        p.width = (int) valueAnimator1.getAnimatedValue();
                        p.height = (int) valueAnimator1.getAnimatedValue();
                        batteryImageView.setLayoutParams(p);
                    });
                    valueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationEnd(animation);
                            tv.setVisibility(View.INVISIBLE);
                        }
                    });
                    valueAnimator.start();
                }
            }
        }
    };
    private TextView tv;
    private BatteryImageView batteryImageView;

    private void updateView() {
        if (mView != null) {
            tv.setText((int) batteryPercent + "%");
            batteryImageView.updateBatteryPercent(batteryPercent);
            SharedPreferences prefs = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
            IntentFilter filter = new IntentFilter(ctx.getPackageName() + ".COLOR_CHANGED");
            ctx.registerReceiver(receiver, filter);
            if (batteryPercent > 80) {
                batteryImageView.setStrokeColor(prefs.getInt("Allaccent_color", Color.RED));
                tv.setTextColor(prefs.getInt("Allaccent_color", Color.RED));
            } else if (batteryPercent < 80 && batteryPercent > 20) {
                batteryImageView.setStrokeColor(prefs.getInt("Allaccent_color", Color.RED));
                tv.setTextColor(prefs.getInt("Allaccent_color", Color.RED));
            } else {
                batteryImageView.setStrokeColor(prefs.getInt("Allaccent_color", Color.RED));
                tv.setTextColor(prefs.getInt("Allaccent_color", Color.RED));
            }
        }
    }

    @Override
    public void onUnbind() {

        mView = null;
    }

    @Override
    public void onBindComplete() {
        if (mView == null) return;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, ctx.dpToInt(ctx.minHeight / 4));
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            ViewGroup.LayoutParams p = batteryImageView.getLayoutParams();
            p.width = (int) valueAnimator1.getAnimatedValue();
            p.height = (int) valueAnimator1.getAnimatedValue();
            batteryImageView.setLayoutParams(p);
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                tv.setVisibility(View.VISIBLE);
                batteryImageView.requestLayout();
                batteryImageView.updateBatteryPercent(batteryPercent);
            }
        });
        valueAnimator.start();
    }

    @Override
    public void onDestroy() {
        ctx.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onExpand() {

    }

    @Override
    public void onCollapse() {

    }

    @Override
    public void onClick() {

    }

    @Override
    public String[] permissionsRequired() {
        return null;
    }

    @Override
    public ArrayList<SettingStruct> getSettings() {
        return null;
    }
}
