package com.imgodz.diecastvault;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DiecastViewModelFactory implements ViewModelProvider.Factory {
    private  final Application mApplication;

    public DiecastViewModelFactory(Application application) {
        this.mApplication = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DiecastviewModel.class)) {
            return (T) new DiecastviewModel(mApplication);
            }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
