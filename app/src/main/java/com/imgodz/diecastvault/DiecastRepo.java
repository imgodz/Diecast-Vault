package com.imgodz.diecastvault;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiecastRepo {

    private DiecastDAO diecastDAO;
    private LiveData<List<Diecast>> allDiecast;
    private final ExecutorService executorService;

    public DiecastRepo(Application application) {
        DiecastDatabase database = DiecastDatabase.getInstance(application);
        this.diecastDAO = database.getDiecastDAO();
        allDiecast = diecastDAO.getAllDiecast();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Diecast diecast) {
        executorService.execute(() -> diecastDAO.insert(diecast));
    }

    public void update(Diecast diecast) {
        executorService.execute(() -> diecastDAO.update(diecast));
    }

    public void delete(Diecast diecast) {
        executorService.execute(() -> diecastDAO.delete(diecast));
    }

    public LiveData<List<Diecast>> getAllDiecast() {
        return allDiecast;
    }

    public LiveData<Integer> getDiecastCount() {
        return diecastDAO.getDiecastCount();
    }

    public LiveData<String> getMostCommonModelMaker() {
        return diecastDAO.getMostCommonModelMaker();
    }

    public LiveData<List<CategoryCount>> getCategoryBreakdown() {
        return diecastDAO.getCategoryBreakdown();
    }

    public LiveData<Integer> getTotalSpent() {
        return diecastDAO.getTotalSpent();
    }

    public LiveData<Diecast> getMostExpensive() {
        return diecastDAO.getMostExpensive();
    }

    public LiveData<Diecast> getDiecastById(int id) { return diecastDAO.getDiecastById(id); }

}
