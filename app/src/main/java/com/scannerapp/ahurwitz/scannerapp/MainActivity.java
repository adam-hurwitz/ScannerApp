package com.scannerapp.ahurwitz.scannerapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private final static String QR = "QR";
    private final static String DATA_MATRIX = "DATA_MATRIX";
    private final static String PDF_417 = "PDF_417";

    ScannerPagerAdapter scannerPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        scannerPagerAdapter = new ScannerPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(scannerPagerAdapter);
        viewPager.setCurrentItem(1);

    }

    public static class ScannerPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public ScannerPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return TabFragment.newInstance(DATA_MATRIX);
                    //return TabFragment.newInstance(DATA_MATRIX);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return TabFragment.newInstance(QR);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return TabFragment.newInstance(PDF_417);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }
}
