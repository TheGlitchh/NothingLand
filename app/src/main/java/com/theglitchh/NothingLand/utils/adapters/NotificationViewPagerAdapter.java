package com.theglitchh.NothingLand.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.theglitchh.NothingLand.R;
import com.theglitchh.NothingLand.plugins.Notification.NotificationMeta;
import com.theglitchh.NothingLand.services.OverlayService;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;

public class NotificationViewPagerAdapter extends PagerAdapter {
    private ArrayList<NotificationMeta> notifications;
    private OverlayService ctx;

    public NotificationViewPagerAdapter(ArrayList<NotificationMeta> metas, OverlayService context) {
        ctx = context;
        notifications = metas;
    }

    public void updateNotifications(ArrayList<NotificationMeta> metas) {
        notifications = metas;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        NotificationMeta meta = notifications.get(position);
        View mView = LayoutInflater.from(container.getContext()).inflate(R.layout.single_notification, null);
        ((TextView) mView.findViewById(R.id.title)).setText(meta.getTitle());
        ((TextView) mView.findViewById(R.id.text_description)).setText(meta.getDescription());
        StringBuilder stringBuilder = new StringBuilder();
        String name = meta.getAll().getString("name");
        if (name != null) stringBuilder.append(name).append(" • ");
        long since = meta.getAll().getLong("time");
        PrettyTime p = new PrettyTime();
        stringBuilder.append(p.format(new Date(since)));
        ((TextView) mView.findViewById(R.id.author)).setText(stringBuilder.toString());
        int value = ctx.getAttr(androidx.appcompat.R.attr.colorPrimary);
        ((TextView) mView.findViewById(R.id.author)).setTextColor(value);
        mView.findViewById(R.id.title).setSelected(true);
        mView.findViewById(R.id.text_description).setSelected(true);
        ((TextView) mView.findViewById(R.id.title)).setTextColor(ctx.textColor);
        ((TextView) mView.findViewById(R.id.text_description)).setTextColor(ctx.textColor);
        container.addView(mView);
        mView.setTag("mv_" + position);
        return mView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
