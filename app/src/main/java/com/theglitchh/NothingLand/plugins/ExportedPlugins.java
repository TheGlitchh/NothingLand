package com.theglitchh.NothingLand.plugins;

import com.theglitchh.NothingLand.plugins.AppShortcut.AppShortcutIsland;

import java.util.ArrayList;

public class ExportedPlugins {
    public static ArrayList<BasePlugin> getPlugins() {
        ArrayList<BasePlugin> plugins = new ArrayList<>();
        plugins.add(new AppShortcutIsland());
        return plugins;
    }
}
