package com.theglitchh.NothingLand.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.theglitchh.NothingLand.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppearanceActivity extends AppCompatActivity {
    private String intToHex(int i) {
        return Integer.toHexString(i);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.appearence_layout);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDefaultDisplayHomeAsUpEnabled(true);
        selectedImageView = findViewById(R.id.selected_image_view);
        Button selectImageBtn = findViewById(R.id.select_image_btn);


        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String imagePath = sharedPreferences.getString("background_image_uri", null);
        if (imagePath != null) {
            selectedImageUri = Uri.parse(imagePath);
            selectedImageView.setImageURI(selectedImageUri);
            selectedImageView.setVisibility(View.VISIBLE);
        }else {
            selectedImageUri = null; // ← add this to be explicit
            selectedImageView.setVisibility(View.GONE);
        }
        selectImageBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED) {
                    // Permission already granted, proceed to image selection
                    openImagePicker();
                } else {
                    // Request permission
                    ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_MEDIA_IMAGES"}, IMAGE_PICK_CODE);
                }
            } else {
                // For Android versions below 13, use legacy permission
                openImagePicker();
            }
        });
        findViewById(R.id.resetbtn2).setOnClickListener(v -> {
            if (selectedImageUri != null) {
                getContentResolver().releasePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            // Clear the selected image view
            selectedImageUri = null;
            selectedImageView.setVisibility(View.GONE);
            selectedImageView.setImageURI(null);

            // Remove the background image URI from SharedPreferences
            sharedPreferences.edit().remove("background_image_uri").apply();

            // Broadcast the reset event
            Intent intent = new Intent(getPackageName() + ".COLOR_CHANGED");
            intent.putExtra("color", sharedPreferences.getInt("color", Color.BLACK));
            intent.putExtra("Allaccent_color", sharedPreferences.getInt("Allaccent_color", getColor(R.color.md_theme_dark_inversePrimary)));
            intent.putExtra("background_image_uri", (String) null);  // Indicate that the image was cleared

            sendBroadcast(intent);

            // Optionally, you can show a Snackbar for feedback
            Snackbar.make(findViewById(R.id.resetbtn2), "Background image reset", Snackbar.LENGTH_SHORT).show();
        });



        TextInputLayout t = findViewById(R.id.textField);
        TextInputLayout t2 = findViewById(R.id.textField2);

        Objects.requireNonNull(t.getEditText()).setText(intToHex(sharedPreferences.getInt("color", getColor(R.color.black))));
        Objects.requireNonNull(t2.getEditText()).setText(intToHex(sharedPreferences.getInt("Allaccent_color", getColor(R.color.md_theme_dark_inversePrimary))));
        String uriString = sharedPreferences.getString("background_image_uri", null);
        if (uriString != null) {
            selectedImageUri = Uri.parse(uriString);
        } else {
            selectedImageUri = null;
        }
        findViewById(R.id.apply_btn).setOnClickListener(l -> {
            String value = null;
            String AllaccentColorValue = null;
            if (Objects.requireNonNull(t.getEditText()).getText() != null && Objects.requireNonNull(t2.getEditText()).getText() != null)
                value = "#" + t.getEditText().getText().toString();AllaccentColorValue = "#" + t2.getEditText().getText().toString();
            if (value != null) {
                if (isValidColor(value)) {

                    t.setError(null);
                    t.setErrorEnabled(false);
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(findViewById(R.id.textField).getWindowToken(), 0);
                    } catch (Exception ignored) {
                    }
                    try {
                        int color = Color.parseColor(value);
                        int AllaccentColor = Color.parseColor(AllaccentColorValue);
                        sharedPreferences.edit().putInt("color", color).putInt("Allaccent_color", AllaccentColor).apply();
                        Snackbar.make(this, findViewById(R.id.textField), "Successfully updated color", Snackbar.LENGTH_SHORT).show();
                        Intent intent = new Intent(getPackageName() + ".COLOR_CHANGED");
                        intent.putExtra("color", color);
                        intent.putExtra("Allaccent_color", AllaccentColor);

                        if (selectedImageUri != null) {
                            intent.putExtra("background_image_uri", selectedImageUri.toString());

                        }
                        sendBroadcast(intent);
                    } catch (Exception e) {
                        t.setErrorEnabled(true);
                        t.setError("Invalid hexadecimal value");
                    }
                } else {
                    t.setErrorEnabled(true);
                    t.setError("Please provide a valid hexadecimal value");
                }

            } else {
                t.setErrorEnabled(true);
                t.setError("Please provide a hexadecimal value");
            }
        });

    }
    public static final int IMAGE_PICK_CODE = 1001;
    public static final String READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
    public static final int PERMISSION_REQUEST_CODE = 101; // Define the request code
    private Uri selectedImageUri;
    private ImageView selectedImageView;
    private boolean isValidColor(String value) {
        // Source : https://stackoverflow.com/a/23155867/14200419
        Pattern colorPattern = Pattern.compile("#([0-9a-f]{6}|[0-9a-f]{8})");
        Matcher m = colorPattern.matcher(value);
        return m.matches();
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Filter for image files
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Makes it openable with the file manager
        startActivityForResult(intent, IMAGE_PICK_CODE); // Start activity for result
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Log.d("AppearanceActivity", "Image selected: " + selectedImageUri);
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION );
            try {
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); // Persist URI permission
            } catch (SecurityException e) {
                e.printStackTrace(); // Handle exception
            }

            selectedImageView.setVisibility(View.VISIBLE);
            selectedImageView.setImageURI(selectedImageUri);

            // Store the URI string
            getSharedPreferences(getPackageName(), MODE_PRIVATE)
                    .edit()
                    .putString("background_image_uri", selectedImageUri.toString())
                    .apply();

            // Broadcast the image update
            Intent intent = new Intent(getPackageName() + ".COLOR_CHANGED"); // Or use a dedicated IMAGE_CHANGED if preferred
            intent.putExtra("background_image_uri", selectedImageUri.toString());

// Optional: You can also include current color values if you want full state broadcast
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            intent.putExtra("color", sharedPreferences.getInt("color", Color.BLACK));
            intent.putExtra("Allaccent_color", sharedPreferences.getInt("Allaccent_color", getColor(R.color.md_theme_dark_inversePrimary)));

            sendBroadcast(intent);


        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_PICK_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open image picker
                openImagePicker();
            } else {
                Snackbar.make(findViewById(R.id.select_image_btn), "Permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
