package com.scannerapp.ahurwitz.scannerapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.scannerapp.ahurwitz.scannerapp.adapters.BarcodeAdapter;
import com.scannerapp.ahurwitz.scannerapp.camera.BarcodeTrackerFactory;
import com.scannerapp.ahurwitz.scannerapp.camera.Camera2Source;
import com.scannerapp.ahurwitz.scannerapp.camera.CameraSourcePreview;
import com.scannerapp.ahurwitz.scannerapp.camera.GraphicOverlay;

import java.io.IOException;
import java.util.ArrayList;

public class BarcodeFragment extends android.support.v4.app.Fragment {
    private static final String TAG = BarcodeActivity.class.getSimpleName();
    private Context context;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private Camera2Source mCamera2Source = null;
    private CameraSourcePreview preview;
    private BarcodeDetector barcodeDetector = null;
    private GraphicOverlay graphicOverlay;
    private ImageView barcodeImg;
    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;
    private boolean wasActivityResumed = false;
    private boolean useCamera2 = false;
    private static final String SCANNER_TYPE = "SCANNER_TYPE";
    private static final String FIRST_LOAD = "FIRST_LOAD";
    private int scannerType;
    private boolean firstLoad = false;
    private ArrayList<String> barcodeNames = new ArrayList<>();
    private BarcodeAdapter barcodeAdapter;
    private RecyclerView recyclerView;
    boolean isFragVisible;

    public BarcodeFragment() {
    }


    public static BarcodeFragment newInstance(int scannerType, boolean firstLoad) {
        BarcodeFragment fragment = new BarcodeFragment();
        Bundle args = new Bundle();
        args.putInt(SCANNER_TYPE, scannerType);
        args.putBoolean(FIRST_LOAD, firstLoad);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs();
    }

    private void initArgs() {
        if (getArguments() != null) {
            scannerType = getArguments().getInt(SCANNER_TYPE);
            firstLoad = getArguments().getBoolean(FIRST_LOAD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context = getActivity().getApplicationContext();

        initViews(view);
        initBarcodeImage();

        //todo: add in for results adapter
        /*barcodeAdapter = new BarcodeAdapter();
        recyclerView.setAdapter(barcodeAdapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);*/

        if (firstLoad) {
            startPreview();
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews(View view) {

        preview = (CameraSourcePreview) view.findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay) view.findViewById(R.id.barOverlay);
        barcodeImg = (ImageView) view.findViewById(R.id.barcode_img);
        //todo: add in for Bottom Sheet
        //bottomSheet = view.findViewById(R.id.bottom_sheet);
        //recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        /*bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(150);*/

    }

    private void initBarcodeImage() {
        int img = 0;
        int width = 250;
        int height = 250;
        switch (scannerType) {
            case Barcode.DATA_MATRIX:
                img = R.drawable.data_matrix;
                break;
            case Barcode.QR_CODE:
                img = R.drawable.qr;
                break;
            case Barcode.PDF417:
                img = R.drawable.pdf417;
                height = 200;
                width = 500;
                break;
        }

        Glide.with(getActivity())
                .load(img)
                .override(width, height)
                .centerCrop()
                .into(barcodeImg);
    }

    private boolean checkGooglePlayAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(getActivity(), resultCode, 2404).show();
            }
        }
        return false;
    }

    private void requestPermissionThenOpenCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            useCamera2 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
            createCameraSourceBack();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }


    private void createCameraSourceBack() {

        barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(scannerType).build();

        if (barcodeDetector.isOperational()) {
            barcodeDetector.setProcessor(new MultiProcessor.Builder<>(
                    new BarcodeTrackerFactory(graphicOverlay, getActivity(), scannerType))
                    .build());
        } else {
            Toast.makeText(context, "BARCODE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        mCamera2Source = new Camera2Source.Builder(context, barcodeDetector)
                .setFacing(Camera2Source.CAMERA_FACING_BACK)
                .setFocusMode(Camera2Source.CAMERA_AF_CONTINUOUS_PICTURE)
                .build();

        startCameraSource();

    }

    public void startCameraSource() {
        if (mCamera2Source != null) {
            try {

                preview.start(mCamera2Source, graphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source 2.", e);
                mCamera2Source.release();
                mCamera2Source = null;
            }
        }
    }

    public void stopCameraSource() {
        preview.stop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(getActivity(), "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            isFragVisible = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( wasActivityResumed && isFragVisible ) {
            startPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wasActivityResumed = true;
        stopCameraSource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCameraSource();
        if (barcodeDetector != null) {
            barcodeDetector.release();
        }
    }

    public void startPreview() {
        if (checkGooglePlayAvailability()) {
            requestPermissionThenOpenCamera();
        }
    }

    public void addToResults(int scannerType, Barcode barcode) {

        if (!barcodeNames.contains(barcode.displayValue)) {
            barcodeNames.add(barcode.displayValue);
            barcodeAdapter.addItems(barcodeNames);
        }

    }
}
