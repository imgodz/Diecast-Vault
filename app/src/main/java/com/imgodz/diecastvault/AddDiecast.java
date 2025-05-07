package com.imgodz.diecastvault;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

public class AddDiecast extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Uri imageUri;
    private ImageView imagePreview;

    private DiecastviewModel diecastViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_diecast);
        DiecastViewModelFactory factory = new DiecastViewModelFactory(getApplication());
        diecastViewModel = new ViewModelProvider(this, factory).get(DiecastviewModel.class);


        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextReleaseInfo = findViewById(R.id.editTextReleaseInfo);
        EditText editTextModelMaker = findViewById(R.id.editTextModelMaker);
        EditText editTextPrice = findViewById(R.id.editTextPrice);
        EditText editTextPurchaseDate = findViewById(R.id.editTextPurchaseDate);
        EditText editTextPrimaryColor = findViewById(R.id.editTextPrimaryColor);
        EditText editTextSecondaryColor = findViewById(R.id.editTextSecondaryColor);
        EditText editTextLivery = findViewById(R.id.editTextLivery);

        Spinner spinnerSetSeries = findViewById(R.id.spinnerSetSeries);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonSave = findViewById(R.id.buttonSave);

        imagePreview = findViewById(R.id.imagePreview);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);

        buttonSelectImage.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        String[] setSeriesOptions = {"Mainline", "Premium", "Semi-Premium", "Themed", "Team Transport", "Car Culture", "Exclusives", "Part of 5 Pack", "Others"};
        ArrayAdapter<String> setSeriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, setSeriesOptions);
        spinnerSetSeries.setAdapter(setSeriesAdapter);

        String[] categoryOptions = {"Car", "Super Car", "Sports Car", "Race Car", "Rally Car", "Offroad", "Truck", "Vintage", "Electric", "Aviation", "Transports", "Bus/Vans", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions);
        spinnerCategory.setAdapter(categoryAdapter);

        diecastViewModel = new ViewModelProvider(this).get(DiecastviewModel.class);

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String modelMaker = editTextModelMaker.getText().toString().trim();
            String releaseInfo = editTextReleaseInfo.getText().toString().trim();
            String price = editTextPrice.getText().toString().trim();
            String primaryColor = editTextPrimaryColor.getText().toString().trim();
            String secondaryColor = editTextSecondaryColor.getText().toString().trim();
            String livery = editTextLivery.getText().toString().trim();
            String purchaseDate = editTextPurchaseDate.getText().toString().trim();
            String imagePath = saveImageToFile(imageUri);

            if (imagePath == null) {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                return;
            }



            String setSeries = spinnerSetSeries.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            if (name.isEmpty() || modelMaker.isEmpty() || price.isEmpty() || purchaseDate.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Diecast diecast = new Diecast();
                    diecast.setName(name);
                    diecast.setModelMaker(modelMaker);
                    diecast.setReleaseInfo(releaseInfo);
                    diecast.setPrice(Integer.parseInt(price));
                    diecast.setSetSeries(setSeries);
                    diecast.setPurchaseDate(purchaseDate);
                    diecast.setPrimaryColor(primaryColor);
                    diecast.setSecondaryColor(secondaryColor);
                    diecast.setLivery(livery);
                    diecast.setCategory(category);
                    diecast.setImagePath(imagePath);


            diecastViewModel.insert(diecast);
            Toast.makeText(this, "Diecast added successfully", Toast.LENGTH_SHORT).show();

            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });




    }

    private String saveImageToFile(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // Resize image to ~1024px width
            int width = 1024;
            int height = (int) ((double) bitmap.getHeight() / bitmap.getWidth() * 1024);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, true);

            File dir = new File(getFilesDir(), "DiecastImages");
            if (!dir.exists()) dir.mkdirs();

            String filename = "diecast_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(dir, filename);

            FileOutputStream out = new FileOutputStream(imageFile);
            resized.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                imagePreview.setImageURI(imageUri);
            } else if (requestCode == REQUEST_GALLERY) {
                imageUri = data.getData();
                imagePreview.setImageURI(imageUri);
            }
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_GALLERY);
            }

        });
        builder.show();
    }

    private byte[] imageToByteArray(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_sub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button
            return true;
        }

        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void showInfoDialog() {
        try {
            // Get version info
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;

            int versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionCode;

            // Format the string with placeholders
            String infoText = getString(
                    R.string.info_text,
                    versionName,
                    versionCode
            );

            // Show dialog
            new AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal)
                    .setTitle("App Info")
                    .setMessage(infoText)
                    .setPositiveButton("Got it", null)
                    .show();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Fallback if version info fails
            new AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal)
                    .setTitle("App Info")
                    .setMessage(R.string.info_text)
                    .setPositiveButton("Got it", null)
                    .show();
        }
    }
}