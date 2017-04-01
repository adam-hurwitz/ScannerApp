package com.scannerapp.ahurwitz.scannerapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.vision.barcode.Barcode;
import com.scannerapp.ahurwitz.scannerapp.camera.BarcodeGraphicTracker;
import com.scannerapp.ahurwitz.scannerapp.adapters.SmartFragmentStatePagerAdapter;

public class BarcodeActivity extends AppCompatActivity implements BarcodeGraphicTracker.UpdateBarcodeListener {

    SmartFragmentStatePagerAdapter scannerPagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initViewPager();
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        scannerPagerAdapter = new ScannerPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(scannerPagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(2);
        startPreviews(viewPager);
    }

    public static class ScannerPagerAdapter extends SmartFragmentStatePagerAdapter {
        private static int NUM_ITEMS = 3;

        public ScannerPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BarcodeFragment.newInstance(Barcode.DATA_MATRIX, false);
                case 1:
                    return BarcodeFragment.newInstance(Barcode.QR_CODE, true);
                case 2:
                    return BarcodeFragment.newInstance(Barcode.PDF417, false);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

    private void startPreviews(final ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        BarcodeFragment barFrag = (BarcodeFragment) scannerPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                        barFrag.startPreview();
                    case 1:
                        BarcodeFragment barFrag2 = (BarcodeFragment) scannerPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                        barFrag2.startPreview();
                    case 2:
                        BarcodeFragment barFrag3 = (BarcodeFragment) scannerPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                        barFrag3.startPreview();
                    default:
                        return;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void getBarcode(Barcode barcode, int scannerType) {

        final Barcode barcodeVar = barcode;
        final int scannerTypeVar = scannerType;

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {

                BarcodeFragment barcodeFragment = (BarcodeFragment) scannerPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                barcodeFragment.addToResults(scannerTypeVar, barcodeVar);

            }
        });*/


    }

}
