package com.scannerapp.ahurwitz.scannerapp;

import android.app.Application;

import com.scannerapp.ahurwitz.scannerapp.DependencyInjection.DaggerDataComponent;
import com.scannerapp.ahurwitz.scannerapp.DependencyInjection.DataComponent;
import com.scannerapp.ahurwitz.scannerapp.DependencyInjection.DataModule;

public class BarcodeApplication extends Application {

    private static BarcodeApplication app;
    DataComponent dataComponent;

    public static BarcodeApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        initDataComponent();

        dataComponent.inject(this);
    }

    private void initDataComponent(){
        dataComponent = DaggerDataComponent.builder()
                .dataModule(new DataModule(this))
                .build();
    }

    public DataComponent getDataComponent() {
        return dataComponent;
    }
}
