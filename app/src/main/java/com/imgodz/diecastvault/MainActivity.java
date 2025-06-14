package com.imgodz.diecastvault;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 10;
    private static final int REQUEST_CODE_MANAGE_STORAGE = 1002;
    private static final int REQUEST_CODE_IMPORT_FILE = 1003;
    private DiecastviewModel diecastViewModel;
    private TextView textDiecasts, textMostExpensive, textMostCommonMaker, textTotalSpent;
    private Integer diecastCount = null;
    private String mostCommonMaker = null;
    private Integer totalSpent = null;
    private Diecast mostExpensive = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.buttonAddDiecast).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDiecast.class);
            startActivity(intent);
        });

        LinearLayout tileRandom = findViewById(R.id.tileRandom);
        LinearLayout tileFilter = findViewById(R.id.tileFilter);

        textDiecasts = findViewById(R.id.textDiecast);
        textMostExpensive = findViewById(R.id.textMostExpensive);
        textMostCommonMaker = findViewById(R.id.textMostCommon);
        textTotalSpent = findViewById(R.id.textSpent);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        DiecastViewModelFactory factory = new DiecastViewModelFactory(getApplication());
        diecastViewModel = new ViewModelProvider(this, factory).get(DiecastviewModel.class);

        tileRandom.setOnClickListener(v ->
                observeOnce(diecastViewModel.getAllDiecast(), this, diecasts -> {
                    if (diecasts != null && !diecasts.isEmpty()) {
                        Random random = new Random();
                        Diecast randomDiecast = diecasts.get(random.nextInt(diecasts.size()));

                        Intent intent = new Intent(MainActivity.this, DiecastDetailActivity.class);
                        intent.putExtra("diecast_id", randomDiecast.getId());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "No diecasts available", Toast.LENGTH_SHORT).show();
                    }
                })
        );
        tileFilter.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        diecastViewModel.getDiecastCount().observe(this, count -> {
            diecastCount = count;
            updateStatsDisplay();
        });

        diecastViewModel.getMostCommonModelMaker().observe(this, maker -> {
            mostCommonMaker = maker;
            updateStatsDisplay();
        });

        diecastViewModel.getTotalSpent().observe(this, spent -> {
            totalSpent = spent;
            updateStatsDisplay();
        });

        diecastViewModel.getMostExpensive().observe(this, expensive -> {
            mostExpensive = expensive;
            updateStatsDisplay();
        });
    }

    public static <T> void observeOnce(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);  // removes itself after first run
            }
        });
    }

    private void updateStatsDisplay() {
        if (diecastCount == null || mostCommonMaker == null || totalSpent == null || mostExpensive == null) {
            return;
        }

        String mostExpensiveName = mostExpensive.getName().length() > 15 ? mostExpensive.getName().substring(0, 15) + "..." : mostExpensive.getName();
        String maxInfo = mostExpensiveName + " (₹ " + mostExpensive.getPrice() + ")";

        String statsText = "Total Diecasts Collected: " + diecastCount + "\n" +
                "Most Common Model Maker: " + mostCommonMaker + "\n" +
                "Total Spent: ₹" + totalSpent + "\n" +
                "Most Expensive: " + maxInfo + "\n";

        String totalString = diecastCount > 1 ? " Diecasts" : " Diecast";
        textDiecasts.setText(diecastCount.toString() + totalString);
        textMostCommonMaker.setText(mostCommonMaker);
        textTotalSpent.setText("₹ " + totalSpent);
        textMostExpensive.setText(maxInfo);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_imAndExport) {
            showImportExportDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImportExportDialog() {
        new AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal)
                .setTitle("Import/Export")
                .setItems(new String[]{"Import Collection", "Export Collection"}, (dialog, which) -> {
                    switch (which) {
                        case 0: startImportProcess(); break;
                        case 1: startExportProcess(); break;
                    }
                })
                .show();
    }

    private void startExportProcess() {
        // Check storage permission first for Android < 10
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            performExport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performExport();
            } else {
                Toast.makeText(this, "Permission required to export", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performExport() {
        new ExportTask(this, new ExportTask.ExportListener() {
            private ProgressDialog progressDialog;

            @Override
            public void onExportStarted() {
                progressDialog = ProgressDialog.show(MainActivity.this,
                        "Exporting",
                        "Preparing your collection...",
                        true);
            }

            @Override
            public void onExportComplete(File exportedFile) {
                progressDialog.dismiss();
                if (exportedFile != null) {
                    shareExportedFile(exportedFile);
                    Toast.makeText(MainActivity.this,
                            "Export successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Export failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute();
    }

    private void shareExportedFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share Collection Backup"));
    }

    private void startImportProcess() {
        // Check storage permissions first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                launchFilePicker();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
                requestManageStoragePermission();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                launchFilePicker();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            }
        }
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");  // Accept all file types but filter in onActivityResult
        startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted, proceed with file operation
                    launchFilePicker();
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == REQUEST_CODE_IMPORT_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                confirmImport(uri);
            }
        }
    }

    private void requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // Try the new intent for Android 11+
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
            } catch (Exception e) {
                // Fallback for devices that don't support the specific intent
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }

    private void confirmImport(Uri uri) {
        new AlertDialog.Builder(this, R.style.DialogTheme_BlackTeal)
                .setTitle("Confirm Import")
                .setMessage("This will replace your current collection. Continue?")
                .setPositiveButton("Import", (dialog, which) -> performImport(uri))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performImport(Uri uri) {
        new ImportTask(this, new ImportTask.ImportListener() {
            private ProgressDialog progressDialog;

            @Override
            public void onImportStarted() {
                progressDialog = ProgressDialog.show(MainActivity.this,
                        "Importing",
                        "Restoring your collection...",
                        true);
            }

            @Override
            public void onImportComplete(boolean success) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(MainActivity.this,
                            "Import successful! Please restart the app.",
                            Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme_BlackTeal)
                            .setTitle("Import Successful")
                            .setMessage("App will now restart to complete the import.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> restartApp())
                            .show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Import failed - invalid file format",
                            Toast.LENGTH_LONG).show();
                }
            }
        }).execute(uri);
    }

    private void restartApp() {
        // Clear any cached data if needed
        DiecastDatabase.getInstance(this).close();

        // Create restart intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Start new activity
        startActivity(intent);

        // Kill current process
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }
}