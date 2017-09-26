package com.scannerapp.ahurwitz.scannerapp.DependencyInjection;

import android.app.Application;

import dagger.Module;

@Module
public class DataModule {

    Application application;

    public DataModule(Application application) {
        this.application = application;
    }

}