package com.imgodz.diecastvault;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

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

        tileRandom.setOnClickListener(v -> diecastViewModel.getAllDiecast().observe(this, diecasts -> {
            if (diecasts != null && !diecasts.isEmpty()) {
                Random random = new Random();
                Diecast randomDiecast = diecasts.get(random.nextInt(diecasts.size()));

                Intent intent = new Intent(MainActivity.this, DiecastDetailActivity.class);
                intent.putExtra("diecast_id", randomDiecast.getId());
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "No diecasts available", Toast.LENGTH_SHORT).show();
            }
        }));

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }
}