package com.scannerapp.ahurwitz.scannerapp.DependencyInjection;


import com.scannerapp.ahurwitz.scannerapp.BarcodeApplication;
import com.scannerapp.ahurwitz.scannerapp.BarcodeFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ahurwitz on 1/24/17.
 */

@Singleton
@Component(modules={DataModule.class})

public interface DataComponent {

    void inject(BarcodeApplication barcodeApplication);

    void inject(BarcodeFragment barcodeFragment);

}
