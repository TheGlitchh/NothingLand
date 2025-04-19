package com.theglitchh.NothingLand.plugins;

import com.theglitchh.NothingLand.plugins.BatteryPlugin.BatteryPlugin;
import com.theglitchh.NothingLand.plugins.MediaSession.MediaSessionPlugin;
import com.theglitchh.NothingLand.plugins.Notification.NotificationPlugin;

import java.util.ArrayList;

public class ExportedPlugins {
    public static ArrayList<BasePlugin> getPlugins() {
        ArrayList<BasePlugin> plugins = new ArrayList<>();
        plugins.add(new MediaSessionPlugin());
        plugins.add(new NotificationPlugin());
        plugins.add(new BatteryPlugin());
        return plugins;
    }
}
