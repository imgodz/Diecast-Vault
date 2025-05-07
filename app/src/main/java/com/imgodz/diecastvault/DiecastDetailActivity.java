package com.imgodz.diecastvault;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiecastDetailActivity extends AppCompatActivity {
    private Diecast diecast;
    private EditText editTextName, editTextMaker, editTextReleaseInfo, editTextPrice,
            editTextPurchaseDate, editTextPrimaryColor, editTextSecondaryColor, editTextLivery;

    Spinner spinnerSetSeries, spinnerCategory;
    private ImageView imageView;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Uri imageUri;
    private DiecastviewModel diecastViewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_diecast_detail);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextMaker = findViewById(R.id.editTextMaker);
        EditText editTextReleaseInfo = findViewById(R.id.editTextReleaseInfo);
        EditText editTextPrice = findViewById(R.id.editTextPrice);
        EditText editTextPurchaseDate = findViewById(R.id.editTextPurchaseDate);
        EditText editTextPrimaryColor = findViewById(R.id.editTextPrimaryColor);
        EditText editTextSecondaryColor = findViewById(R.id.editTextSecondaryColor);
        EditText editTextLivery = findViewById(R.id.editTextLivery);
        Spinner spinnerSet = findViewById(R.id.spinnerSet);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);

        Button saveButton = findViewById(R.id.buttonSaveChanges);
        Button deleteButton = findViewById(R.id.buttonDelete);

        imageView = findViewById(R.id.imageViewDetail);

        imageView.setOnClickListener(v -> showImagePickerDialog());

        int diecastId = getIntent().getIntExtra("DIECAST_ID", -1);

        diecastViewModel = new ViewModelProvider(this).get(DiecastviewModel.class);



        if (diecastId != -1) {
            diecastViewModel.getDiecastById(diecastId).observe(this, diecast -> {
                if (diecast != null) {
                    this.diecast = diecast;
                    populateDiecastDetails(diecast);
                }
            });
        }

        // Set/Series
        ArrayAdapter<CharSequence> setAdapter = ArrayAdapter.createFromResource(
                this, R.array.set_series_options, android.R.layout.simple_spinner_item);
        setAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSet.setAdapter(setAdapter);

// Category
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);


        diecastId = getIntent().getIntExtra("diecast_id", -1);
        if (diecastId == -1) {
            Toast.makeText(this, "No diecast ID provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DiecastviewModel viewModel = new ViewModelProvider(this).get(DiecastviewModel.class);

        saveButton.setEnabled(false); // disable until loaded

        int finalDiecastId = diecastId;
        viewModel.getAllDiecast().observe(this, diecastList -> {
            for (Diecast d : diecastList) {
                if (d.getId() == finalDiecastId) {
                    diecast = d; // ✅ Assign it here

                    // Populate fields
                    editTextName.setText(d.getName());
                    editTextMaker.setText(d.getModelMaker());
                    editTextReleaseInfo.setText(d.getReleaseInfo());
                    editTextPrice.setText(String.valueOf(d.getPrice()));
                    editTextPurchaseDate.setText(d.getPurchaseDate());
                    editTextPrimaryColor.setText(d.getPrimaryColor());
                    editTextSecondaryColor.setText(d.getSecondaryColor());
                    editTextLivery.setText(d.getLivery());
                    spinnerSet.setSelection(setAdapter.getPosition(d.getSetSeries()));
                    spinnerCategory.setSelection(categoryAdapter.getPosition(d.getCategory()));

                    // Load image
                    String imagePath = diecast.getImagePath();

                    if (diecast.getImagePath() != null) {
                        File imgFile = new File(diecast.getImagePath());
                        if (imgFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.placeholder); // fallback image
                        }
                    } else {
                        imageView.setImageResource(R.drawable.placeholder); // fallback image
                    }

                    saveButton.setEnabled(true); // ✅ enable once loaded
                    break;
                }
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (diecast == null) {
                Toast.makeText(this, "Diecast not loaded!", Toast.LENGTH_SHORT).show();
                return;
            }
            showInfoDialog();
        });


        saveButton.setOnClickListener(v -> {
            if (diecast == null) {
                Toast.makeText(this, "Diecast not loaded!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                diecast.setName(editTextName.getText().toString().trim());
                diecast.setModelMaker(editTextMaker.getText().toString().trim());
                diecast.setReleaseInfo(editTextReleaseInfo.getText().toString().trim());
                diecast.setPrice(Integer.parseInt(editTextPrice.getText().toString().trim()));
                diecast.setPurchaseDate(editTextPurchaseDate.getText().toString().trim());
                diecast.setPrimaryColor(editTextPrimaryColor.getText().toString().trim());
                diecast.setSecondaryColor(editTextSecondaryColor.getText().toString().trim());
                diecast.setLivery(editTextLivery.getText().toString().trim());
                diecast.setSetSeries(spinnerSet.getSelectedItem().toString());
                diecast.setCategory(spinnerCategory.getSelectedItem().toString());

                viewModel.update(diecast);
                Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            } catch (Exception e) {
                Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void populateDiecastDetails(Diecast d) {
        // Initialize views if not already done
        editTextName = findViewById(R.id.editTextName);
        editTextMaker = findViewById(R.id.editTextMaker);
        editTextReleaseInfo = findViewById(R.id.editTextReleaseInfo);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextPurchaseDate = findViewById(R.id.editTextPurchaseDate);
        editTextPrimaryColor = findViewById(R.id.editTextPrimaryColor);
        editTextSecondaryColor = findViewById(R.id.editTextSecondaryColor);
        editTextLivery = findViewById(R.id.editTextLivery);
        spinnerSetSeries = findViewById(R.id.spinnerSet);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // Set up adapters if not already done
        ArrayAdapter<CharSequence> setAdapter = ArrayAdapter.createFromResource(
                this, R.array.set_series_options, android.R.layout.simple_spinner_item);
        setAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSetSeries.setAdapter(setAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Populate fields
        editTextName.setText(d.getName());
        editTextMaker.setText(d.getModelMaker());
        editTextReleaseInfo.setText(d.getReleaseInfo());
        editTextPrice.setText(String.valueOf(d.getPrice()));
        editTextPurchaseDate.setText(d.getPurchaseDate());
        editTextPrimaryColor.setText(d.getPrimaryColor());
        editTextSecondaryColor.setText(d.getSecondaryColor());
        editTextLivery.setText(d.getLivery());
        spinnerSetSeries.setSelection(setAdapter.getPosition(d.getSetSeries()));
        spinnerCategory.setSelection(categoryAdapter.getPosition(d.getCategory()));

        // Load image
        if (d.getImagePath() != null) {
            File imageFile = new File(d.getImagePath());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_subsub, menu);
        return true;
    }
    private void showImagePickerDialog() {
        String[] options = {"Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Replace Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    }
                })
                .show();
    }

    private void showInfoDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal);
        builder.setTitle("Confirm?")
                .setMessage("Are you sure you want to delete this diecast?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Positive button action - delete the diecast
                    DiecastviewModel viewModel = new ViewModelProvider(this).get(DiecastviewModel.class);
                    viewModel.delete(diecast);
                    Toast.makeText(this, "Diecast deleted", Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Negative button action - just dismiss and do nothing
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                updateImageFromUri(imageUri);
            } else if (requestCode == REQUEST_GALLERY) {
                imageUri = data.getData();
                updateImageFromUri(imageUri);
            }
        }
    }

    private void updateImageFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // Optional: Resize before saving
            int width = 1024;
            int height = (int) ((double) bitmap.getHeight() / bitmap.getWidth() * 1024);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, true);

            // Save to file
            File dir = new File(getFilesDir(), "DiecastImages");
            if (!dir.exists()) dir.mkdirs();

            String filename = "diecast_" + System.currentTimeMillis() + ".jpg";
            File file = new File(dir, filename);
            FileOutputStream out = new FileOutputStream(file);
            resized.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();

            // Update preview
            imageView.setImageBitmap(resized);

            // Set path in diecast
            if (diecast != null) {
                diecast.setImagePath(file.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button
            return true;
        }
        if (item.getItemId() == R.id.action_card) {
            if (diecast != null) {
                showDiecastCard(diecast);
            } else {
                Toast.makeText(this, "Diecast not loaded yet", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        if (item.getItemId() == R.id.action_info) {
            showInforDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void showInforDialog() {
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
            new android.app.AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal)
                    .setTitle("App Info")
                    .setMessage(infoText)
                    .setPositiveButton("Got it", null)
                    .show();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Fallback if version info fails
            new android.app.AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal)
                    .setTitle("App Info")
                    .setMessage(R.string.info_text)
                    .setPositiveButton("Got it", null)
                    .show();
        }
    }
    @SuppressLint("SetTextI18n")
    private void showDiecastCard(Diecast diecast) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.view_diecast_card, null);

        ImageView cardImage = cardView.findViewById(R.id.cardImage);
        TextView cardName = cardView.findViewById(R.id.cardName);
        TextView cardMaker = cardView.findViewById(R.id.cardMaker);
        TextView cardRelease = cardView.findViewById(R.id.cardRelease);
        TextView cardCategory = cardView.findViewById(R.id.cardCategory);
        TextView cardColor = cardView.findViewById(R.id.cardColor);
        TextView cardLivery = cardView.findViewById(R.id.cardLivery);
        TextView cardPrice = cardView.findViewById(R.id.cardPrice);

        cardName.setText(diecast.getName());
        cardMaker.setText("Maker: " + diecast.getModelMaker());
        cardRelease.setText("Release: " + diecast.getReleaseInfo());
        cardCategory.setText("Category: " + diecast.getCategory());
        cardColor.setText("Color: " + diecast.getPrimaryColor() +
                (diecast.getSecondaryColor() != null ? " / " + diecast.getSecondaryColor() : ""));
        cardLivery.setText("Livery: " + (diecast.getLivery() != null ? diecast.getLivery() : "N/A"));
        cardPrice.setText("₹" + diecast.getPrice() + " | " + diecast.getPurchaseDate());

        if (diecast.getImagePath() != null) {
            File imgFile = new File(diecast.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                cardImage.setImageBitmap(bitmap);
            }
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal);
        builder.setView(cardView)
                .setPositiveButton("Save as Image", (dialog, which) -> saveCardAsImage(cardView))
                .setNegativeButton("Close", null)
                .show();
    }

    private void saveCardAsImage(View view) {
        // 1. Create bitmap from view
        Bitmap originalBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(originalBitmap);
        view.draw(canvas);

        // 2. Draw watermark text on a copy
        Bitmap watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas watermarkCanvas = new Canvas(watermarkedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#66d3d3d3")); // semi-transparent white
        paint.setTextSize(36);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        watermarkCanvas.drawText("Created from Diecast Vault", watermarkedBitmap.getWidth() - 30, watermarkedBitmap.getHeight() - 20, paint);

        // 3. Save using MediaStore (public gallery)
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "DiecastCard_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Diecast Vault");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
            watermarkedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "Card saved to gallery", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}
