package com.theglitchh.NothingLand.plugins.AppShortcut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.theglitchh.NothingLand.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppShortcutPickerActivity extends AppCompatActivity {
    private LinearLayout appListContainer;
    private EditText searchEditText;
    private SharedPreferences sharedPreferences;
    private List<AppShortcutIsland.AppInfo> allApps;
    private List<AppShortcutIsland.AppInfo> filteredApps;
    private Set<String> selectedPackages;
    private static final String PREF_SELECTED_APPS = "app_shortcut_selected_apps";
    private static final int MAX_SHORTCUTS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_shortcut_picker);
        
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        appListContainer = findViewById(R.id.app_list_container);
        searchEditText = findViewById(R.id.search_apps);
        AppCompatButton saveButton = findViewById(R.id.save_button);
        AppCompatButton cancelButton = findViewById(R.id.cancel_button);
        
        allApps = new ArrayList<>();
        filteredApps = new ArrayList<>();
        selectedPackages = new HashSet<>(sharedPreferences.getStringSet(PREF_SELECTED_APPS, new HashSet<>()));
        
        loadAllApps();
        displayApps(allApps);
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        saveButton.setOnClickListener(v -> saveSelection());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadAllApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo packageInfo : packages) {
            // Skip system apps
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    String appName = pm.getApplicationLabel(packageInfo).toString();
                    Drawable icon = pm.getApplicationIcon(packageInfo);
                    allApps.add(new AppShortcutIsland.AppInfo(appName, packageInfo.packageName, icon));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Sort alphabetically
        Collections.sort(allApps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));
    }

    private void filterApps(String query) {
        filteredApps = allApps.stream()
                .filter(app -> app.appName.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayApps(filteredApps);
    }

    private void displayApps(List<AppShortcutIsland.AppInfo> apps) {
        appListContainer.removeAllViews();
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        
        for (AppShortcutIsland.AppInfo app : apps) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(16, 8, 16, 8);
            
            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(selectedPackages.contains(app.packageName));
            checkBox.setEnabled(selectedPackages.contains(app.packageName) || selectedPackages.size() < MAX_SHORTCUTS);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedPackages.size() < MAX_SHORTCUTS) {
                        selectedPackages.add(app.packageName);
                        updateCheckBoxStates();
                    } else {
                        Toast.makeText(AppShortcutPickerActivity.this, 
                                "Maximum " + MAX_SHORTCUTS + " apps allowed", 
                                Toast.LENGTH_SHORT).show();
                        checkBox.setChecked(false);
                    }
                } else {
                    selectedPackages.remove(app.packageName);
                    updateCheckBoxStates();
                }
            });
            
            itemLayout.addView(checkBox);
            
            TextView label = new TextView(this);
            label.setText(app.appName);
            label.setPadding(16, 0, 0, 0);
            label.setTextSize(16);
            itemLayout.addView(label);
            
            container.addView(itemLayout);
        }
        
        scrollView.addView(container);
        appListContainer.addView(scrollView);
    }

    private void updateCheckBoxStates() {
        // Refresh the display to enable/disable checkboxes based on selection count
        displayApps(filteredApps.isEmpty() ? allApps : filteredApps);
    }

    private void saveSelection() {
        sharedPreferences.edit().putStringSet(PREF_SELECTED_APPS, selectedPackages).apply();
        Toast.makeText(this, "Apps saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
