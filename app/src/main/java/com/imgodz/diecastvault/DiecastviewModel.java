package com.imgodz.diecastvault;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DiecastviewModel extends AndroidViewModel {


    private final DiecastRepo repository;
    private final LiveData<List<Diecast>> allDiecast;

    public DiecastviewModel(@NonNull Application application) {
        super(application);
        DiecastDatabase db = DiecastDatabase.getInstance(application);
        repository = new DiecastRepo(application);
        allDiecast = repository.getAllDiecast();
    }



    public void insert(Diecast diecast) {
        repository.insert(diecast);
    }

    public void update(Diecast diecast) {
        repository.update(diecast);
    }

    public void delete(Diecast diecast) {
        repository.delete(diecast);
    }

    public LiveData<List<Diecast>> getAllDiecast() {
        return allDiecast;
    }

    public LiveData<Integer> getDiecastCount() { return repository.getDiecastCount(); }

    public LiveData<String> getMostCommonModelMaker() { return repository.getMostCommonModelMaker(); }

    public LiveData<List<CategoryCount>> getCategoryBreakdown() { return repository.getCategoryBreakdown(); }

    public LiveData<Integer> getTotalSpent() { return repository.getTotalSpent(); }

    public LiveData<Diecast> getMostExpensive() { return repository.getMostExpensive(); }

    public LiveData<Diecast> getDiecastById(int id) { return repository.getDiecastById(id); }

}
