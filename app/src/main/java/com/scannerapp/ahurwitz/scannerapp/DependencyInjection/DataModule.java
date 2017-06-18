package com.scannerapp.ahurwitz.scannerapp.DependencyInjection;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.scannerapp.ahurwitz.scannerapp.Camera.Camera2Source;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {

    Application application;

    public DataModule(Application application) {
        this.application = application;
    }

    //todo: implement method injection

    /* @Provides
    @Singleton
    public Camera2Source provideCamera2Source(Context context, BarcodeDetector barcodeDetector){
        return new Camera2Source.Builder(context, barcodeDetector)
                .setFacing(Camera2Source.CAMERA_FACING_BACK)
                .setFocusMode(Camera2Source.CAMERA_AF_CONTINUOUS_PICTURE)
                .build();
    }*/

    /*@Provides
    @Singleton
    public Camera2Source provideCamera2Source(){

    }*/

}