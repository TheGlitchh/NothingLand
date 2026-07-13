package com.theglitchh.NothingLand.plugins.AppShortcut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.theglitchh.NothingLand.R;
import com.theglitchh.NothingLand.plugins.BasePlugin;
import com.theglitchh.NothingLand.services.OverlayService;
import com.theglitchh.NothingLand.utils.SettingStruct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppShortcutIsland extends BasePlugin {
    private OverlayService overlayService;
    private SharedPreferences sharedPreferences;
    private GridView appGridView;
    private AppShortcutAdapter adapter;
    private List<AppInfo> selectedApps;
    private ConstraintLayout containerView;
    private static final String PREF_SELECTED_APPS = "app_shortcut_selected_apps";
    private static final int MAX_SHORTCUTS = 6;

    @Override
    public String getID() {
        return "app_shortcut_island";
    }

    @Override
    public String getName() {
        return "App Shortcuts";
    }

    @Override
    public void onCreate(OverlayService context) {
        this.overlayService = context;
        this.sharedPreferences = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        selectedApps = new ArrayList<>();
        loadSelectedApps();
    }

    @Override
    public View onBind() {
        LayoutInflater inflater = LayoutInflater.from(overlayService);
        containerView = (ConstraintLayout) inflater.inflate(
                R.layout.app_shortcut_island_layout, null);
        containerView.setId(R.id.binded);
        
        appGridView = containerView.findViewById(R.id.app_grid);
        
        if (selectedApps.isEmpty()) {
            selectedApps = getDefaultApps();
        }
        
        adapter = new AppShortcutAdapter(overlayService, selectedApps, app -> {
            launchApp(app.packageName);
            overlayService.dequeue(this);
        });
        appGridView.setAdapter(adapter);
        
        return containerView;
    }

    @Override
    public void onUnbind() {
        if (appGridView != null) {
            appGridView.setAdapter(null);
        }
    }

    @Override
    public void onDestroy() {
        // Cleanup
    }

    @Override
    public void onExpand() {
        // Not used
    }

    @Override
    public void onCollapse() {
        overlayService.dequeue(this);
    }

    @Override
    public void onClick() {
        // Single click opens app picker
        openAppPicker();
    }

    @Override
    public String[] permissionsRequired() {
        return new String[]{
                "android.permission.QUERY_ALL_PACKAGES"
        };
    }

    @Override
    public ArrayList<SettingStruct> getSettings() {
        ArrayList<SettingStruct> settings = new ArrayList<>();
        settings.add(new SettingStruct("Configure App Shortcuts", "App Shortcuts", SettingStruct.TYPE_CUSTOM) {
            @Override
            public void onClick(Context c) {
                openAppPicker();
            }
        });
        return settings;
    }

    private void openAppPicker() {
        Intent intent = new Intent(overlayService, AppShortcutPickerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        overlayService.startActivity(intent);
    }

    private void launchApp(String packageName) {
        try {
            Intent intent = overlayService.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                overlayService.startActivity(intent);
            } else {
                Toast.makeText(overlayService, "Cannot launch app", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(overlayService, "Error launching app", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadSelectedApps() {
        Set<String> savedApps = sharedPreferences.getStringSet(PREF_SELECTED_APPS, new HashSet<>());
        PackageManager pm = overlayService.getPackageManager();
        
        selectedApps.clear();
        for (String packageName : savedApps) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable icon = pm.getApplicationIcon(appInfo);
                selectedApps.add(new AppInfo(appName, packageName, icon));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private List<AppInfo> getDefaultApps() {
        List<AppInfo> defaultApps = new ArrayList<>();
        PackageManager pm = overlayService.getPackageManager();
        
        // Add some common default apps if available
        String[] defaultPackages = {
                "com.android.chrome",
                "com.whatsapp",
                "com.google.android.apps.messaging",
                "com.spotify.music",
                "com.google.android.gms",
                "com.google.android.apps.maps"
        };
        
        for (String packageName : defaultPackages) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable icon = pm.getApplicationIcon(appInfo);
                defaultApps.add(new AppInfo(appName, packageName, icon));
                if (defaultApps.size() >= MAX_SHORTCUTS) break;
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed
            }
        }
        
        return defaultApps;
    }

    public static class AppInfo {
        public String appName;
        public String packageName;
        public Drawable icon;

        public AppInfo(String appName, String packageName, Drawable icon) {
            this.appName = appName;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
}
