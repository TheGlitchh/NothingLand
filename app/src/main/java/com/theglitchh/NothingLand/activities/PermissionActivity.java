package com.theglitchh.NothingLand.activities;

import static com.theglitchh.NothingLand.activities.AppearanceActivity.IMAGE_PICK_CODE;
import static com.theglitchh.NothingLand.activities.AppearanceActivity.PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.theglitchh.NothingLand.R;

public class PermissionActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        findViewById(R.id.notification_access).setOnClickListener(l -> {
            if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null && Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
                Toast.makeText(this, "This permission is already enabled", Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            Toast.makeText(this, "Please select NothingLand", Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.record_audio).setOnClickListener(l -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "This permission is already enabled", Toast.LENGTH_LONG).show();
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        });

        findViewById(R.id.storage_access).setOnClickListener(l -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+, request the new permissions
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions already granted", Toast.LENGTH_LONG).show();
                    return;
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, IMAGE_PICK_CODE);
            } else {
                // For Android 12 and below, use the legacy permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions already granted", Toast.LENGTH_LONG).show();
                    return;
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_PICK_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int checks = 0;
        if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null && Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            ((CheckBox) findViewById(R.id.notification_access_checkbox)).setChecked(true);
            checks++;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            ((CheckBox) findViewById(R.id.record_audio_checkbox)).setChecked(true);
            checks++;
        }

        // Check for Storage Permissions (READ_EXTERNAL_STORAGE and ACCESS_MEDIA_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ check for media storage access
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                ((CheckBox) findViewById(R.id.storage_access_checkbox)).setChecked(true);
                checks++;
            }
        } else {
            // Android 12 and below check for legacy storage permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                ((CheckBox) findViewById(R.id.storage_access_checkbox)).setChecked(true);
                checks++;
            }
        }

        if (checks >= 3) {
            finish();
        }
    }
}
