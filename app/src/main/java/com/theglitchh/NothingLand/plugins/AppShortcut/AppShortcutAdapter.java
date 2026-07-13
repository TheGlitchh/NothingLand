package com.theglitchh.NothingLand.plugins.AppShortcut;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theglitchh.NothingLand.R;

import java.util.List;

public class AppShortcutAdapter extends BaseAdapter {
    private Context context;
    private List<AppShortcutIsland.AppInfo> apps;
    private OnAppClickListener onAppClickListener;

    public interface OnAppClickListener {
        void onAppClick(AppShortcutIsland.AppInfo app);
    }

    public AppShortcutAdapter(Context context, List<AppShortcutIsland.AppInfo> apps, 
                              OnAppClickListener onAppClickListener) {
        this.context = context;
        this.apps = apps;
        this.onAppClickListener = onAppClickListener;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.app_shortcut_grid_item, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.app_icon);
            holder.label = convertView.findViewById(R.id.app_label);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        AppShortcutIsland.AppInfo app = apps.get(position);
        holder.icon.setImageDrawable(app.icon);
        holder.label.setText(app.appName);
        
        convertView.setOnClickListener(v -> onAppClickListener.onAppClick(app));
        
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
    }
}
