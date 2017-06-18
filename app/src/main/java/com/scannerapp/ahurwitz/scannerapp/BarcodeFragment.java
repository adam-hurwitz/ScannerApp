package com.scannerapp.ahurwitz.scannerapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.scannerapp.ahurwitz.scannerapp.Adapters.BarcodeAdapter;
import com.scannerapp.ahurwitz.scannerapp.Camera.BarcodeTrackerFactory;
import com.scannerapp.ahurwitz.scannerapp.Camera.Camera2Source;
import com.scannerapp.ahurwitz.scannerapp.Camera.CameraSourcePreview;
import com.scannerapp.ahurwitz.scannerapp.Camera.GraphicOverlay;
import com.scannerapp.ahurwitz.scannerapp.Utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

import rx.subscriptions.CompositeSubscription;

public class BarcodeFragment extends android.support.v4.app.Fragment {

    //todo: implement method injection

    /*@Inject
    private void buildCamera2Source (Camera2Source camera2Source) {

    }*/

    private static final String TAG = BarcodeActivity.class.getSimpleName();
    private Context context;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private Camera2Source mCamera2Source = null;
    private CameraSourcePreview preview;
    private BarcodeDetector barcodeDetector = null;
    private GraphicOverlay graphicOverlay;
    private ImageView barcodeImg;
    private View bottomSheet;
    private boolean wasActivityResumed = false;
    private boolean useCamera2 = false;
    private static final String SCANNER_TYPE = "SCANNER_TYPE";
    private static final String FIRST_LOAD = "FIRST_LOAD";
    private int scannerType;
    private boolean firstLoad = false;
    private RecyclerView recyclerView;
    boolean isFragVisible;
    private BottomSheetBehavior bottomSheetBehavior;
    private BarcodeAdapter barcodeAdapter;
    private ImageView handle;
    private int peekingHeight = 116;
    private ArrayList<String> dataMatrixNames = new ArrayList<>();
    private ArrayList<String> qrNames = new ArrayList<>();
    private ArrayList<String> upcNames = new ArrayList<>();

    private CompositeSubscription compositeSubscription;

    public BarcodeFragment() {
    }


    public static BarcodeFragment newInstance(int scannerType) {
        BarcodeFragment fragment = new BarcodeFragment();
        Bundle args = new Bundle();
        args.putInt(SCANNER_TYPE, scannerType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarcodeApplication.getApp().getDataComponent().inject(this);
        initArgs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        compositeSubscription = new CompositeSubscription();

        context = getActivity().getApplicationContext();

        initViews(view);
        initBarcodeImage();
        initBottomSheet(view);
        initRecyclerView();
        startPreview();

        super.onViewCreated(view, savedInstanceState);
    }

    private ArrayList<String> getListType() {
        switch (scannerType) {
            case Barcode.DATA_MATRIX:
                return dataMatrixNames;
            case Barcode.QR_CODE:
                return qrNames;
            case Barcode.PDF417:
                return upcNames;
            default:
                return new ArrayList<>();
        }
    }

    private void initBarcodeImage() {
        Glide.with(getActivity())
                .load(getBarcodeImage().first)
                .override(getBarcodeImage().second[0], getBarcodeImage().second[1])
                .centerCrop()
                .into(barcodeImg);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wasActivityResumed && isFragVisible) {
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
        compositeSubscription.clear();
        stopCameraSource();
        if (barcodeDetector != null) {
            barcodeDetector.release();
        }
    }

    private void initArgs() {
        if (getArguments() != null) {
            scannerType = getArguments().getInt(SCANNER_TYPE);
            firstLoad = getArguments().getBoolean(FIRST_LOAD);
        }
    }

    private void initViews(View view) {
        preview = (CameraSourcePreview) view.findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay) view.findViewById(R.id.barOverlay);
        barcodeImg = (ImageView) view.findViewById(R.id.barcode_img);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        handle = (ImageView) view.findViewById(R.id.handle);
    }

    private void initBottomSheet(View view) {
        bottomSheet = view.findViewById(R.id.recycler_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(peekingHeight);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        handle.setImageDrawable(getResources().getDrawable(getBarcodeImage().first));

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                Log.v(BarcodeFragment.class.getSimpleName(), "STATE: " + newState);

                if (getListType().isEmpty()) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else if (newState == BottomSheetBehavior.STATE_DRAGGING
                        || newState == BottomSheetBehavior.STATE_EXPANDED
                        || newState == BottomSheetBehavior.STATE_SETTLING) {
                    handle.setVisibility(View.GONE);
                } else {
                    handle.setVisibility(View.VISIBLE);
                }

                if (!getListType().isEmpty() && newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setPeekHeight(peekingHeight);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initRecyclerView() {
        barcodeAdapter = new BarcodeAdapter();
        recyclerView.setAdapter(barcodeAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        onCellSelected();
    }

    private Pair<Integer, int[]> getBarcodeImage() {
        int[] size = {250, 250};
        switch (scannerType) {
            case Barcode.DATA_MATRIX:
                return new Pair<>(R.drawable.data_matrix, size);
            case Barcode.QR_CODE:
                return new Pair<>(R.drawable.qr, size);
            case Barcode.PDF417:
                size[0] = 500;
                size[1] = 200;
                return new Pair<>(R.drawable.pdf417, size);
            default:
                return new Pair<>(0, size);
        }
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

    public void startPreview() {
        if (checkGooglePlayAvailability()) {
            requestPermissionThenOpenCamera();
        }
    }

    public void addToResults(int scannerType, Barcode barcode) {

        if (!getListType().contains(barcode.displayValue)) {
            getListType().add(barcode.displayValue);
            barcodeAdapter.addItems(getListType());

            if (!getListType().isEmpty() && getListType().size() < 4) {
                bottomSheetBehavior.setPeekHeight(getListType().size() * 168);
                handle.setVisibility(View.GONE);
            }

        }
    }

    private void onCellSelected() {
        compositeSubscription.add(barcodeAdapter.onCellSelectedEvent()
                .subscribe(barcode -> {
                    Log.v(BarcodeFragment.class.getSimpleName(), "ON_CELL_SELECTED: " + barcode);
                    if (Utils.isValidURL(barcode)) {
                        Uri webpage = Uri.parse(barcode);
                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }, throwable -> {
                    Log.e(BarcodeFragment.class.getSimpleName(), throwable.toString());
                }));
    }

}
