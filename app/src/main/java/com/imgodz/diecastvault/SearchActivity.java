package com.imgodz.diecastvault;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class SearchActivity extends AppCompatActivity {

    private DiecastviewModel diecastViewModel;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private TextView textResultsCount;
    private List<Diecast> allDiecasts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewSearch);

        searchAdapter = new SearchAdapter(this, new ArrayList<>(), diecast -> {
            Intent intent = new Intent(SearchActivity.this, DiecastDetailActivity.class);
            intent.putExtra("diecast_id", diecast.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchAdapter);

        DiecastViewModelFactory factory = new DiecastViewModelFactory(getApplication());
        diecastViewModel = new ViewModelProvider(this, factory).get(DiecastviewModel.class);

        diecastViewModel.getAllDiecast().observe(this, diecasts -> {
            allDiecasts = diecasts != null ? diecasts : new ArrayList<>();
            showRandomResults();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setQueryHint("Search by name, brand, color...");
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 2) {
                    performSearch(newText);
                } else if (newText.isEmpty()) {
                    showRandomResults();
                }
                return true;
            }
        });

        return true;
    }

    private void showRandomResults() {
        if (allDiecasts.isEmpty()) {
            textResultsCount.setText("No diecasts available");
            searchAdapter.updateList(new ArrayList<>());
            return;
        }

        List<Diecast> randomResults = new ArrayList<>(allDiecasts);
        Collections.shuffle(randomResults);
        int count = Math.min(5, randomResults.size());
        searchAdapter.updateList(randomResults.subList(0, count));
    }

    private void performSearch(String query) {
        if (allDiecasts.isEmpty()) return;

        String lowerQuery = query.toLowerCase();
        List<Diecast> results = new ArrayList<>();

        for (Diecast diecast : allDiecasts) {
            if (matchesSearch(diecast, lowerQuery)) {
                results.add(diecast);
            }
        }

        if (results.size() > 5 && query.length() < 2) {
            Collections.shuffle(results);
            results = results.subList(0, 5);
        }

        searchAdapter.updateList(results);
    }

    private boolean matchesSearch(Diecast diecast, String query) {
        return (diecast.getName() != null && diecast.getName().toLowerCase().contains(query)) ||
                (diecast.getModelMaker() != null && diecast.getModelMaker().toLowerCase().contains(query)) ||
                (diecast.getPrimaryColor() != null && diecast.getPrimaryColor().toLowerCase().contains(query)) ||
                (diecast.getSetSeries() != null && diecast.getSetSeries().toLowerCase().contains(query)) ||
                (diecast.getCategory() != null && diecast.getCategory().toLowerCase().contains(query));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}