package com.scannerapp.ahurwitz.scannerapp;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.scannerapp.ahurwitz.scannerapp.camera.BarcodeTrackerFactory;
import com.scannerapp.ahurwitz.scannerapp.camera.Camera2Source;
import com.scannerapp.ahurwitz.scannerapp.camera.CameraSourcePreview;
import com.scannerapp.ahurwitz.scannerapp.camera.GraphicOverlay;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BarcodeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BarcodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeFragment extends android.support.v4.app.Fragment {
    private static final String TAG = BarcodeActivity.class.getSimpleName();
    private Context context;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private Camera2Source mCamera2Source = null;
    private CameraSourcePreview mPreview;
    private BarcodeDetector barcodeDetector = null;
    private GraphicOverlay mGraphicOverlay;
    private boolean wasActivityResumed = false;
    private boolean useCamera2 = false;
    private static final String SCANNER_TYPE = "SCANNER_TYPE";
    private static final String FIRST_LOAD = "FIRST_LOAD";
    private int scannerType;
    private boolean firstLoad = false;

    TextView textView;

    public BarcodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param scannerType Parameter 1.
     * @return A new instance of fragment EmptyFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        mPreview = (CameraSourcePreview) view.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) view.findViewById(R.id.barOverlay);

        if (firstLoad) {
            startPreview();
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private boolean checkGooglePlayAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if(resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            if(googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(getActivity(), resultCode, 2404).show();
            }
        }
        return false;
    }

    private void requestPermissionThenOpenCamera() {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            useCamera2 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
            createCameraSourceBack();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }


    private void createCameraSourceBack() {

        Log.v(BarcodeFragment.class.getSimpleName(), "BARCODE_TYPE: " + scannerType);

        barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(scannerType).build();

        if(barcodeDetector.isOperational()) {
            //todo: update to pass in type of tracker to know data to pass into Adapter later
            barcodeDetector.setProcessor(new MultiProcessor.Builder<>(new BarcodeTrackerFactory(mGraphicOverlay, getActivity()))
                    .build());
        } else {
            Toast.makeText(context, "BARCODE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        if(useCamera2) {
            mCamera2Source = new Camera2Source.Builder(context, barcodeDetector)
                    .setFacing(Camera2Source.CAMERA_FACING_BACK)
                    .setFocusMode(Camera2Source.CAMERA_AF_CONTINUOUS_PICTURE)
                    .build();

            if(mCamera2Source.isCamera2Native()) {
                startCameraSource();
            } else {
                useCamera2 = false;
                createCameraSourceBack();
            }
        }
    }

    public void startCameraSource() {
        if(useCamera2) {
            if(mCamera2Source != null) {
                //cameraVersion.setText("Camera 2");
                try {mPreview.start(mCamera2Source, mGraphicOverlay);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source 2.", e);
                    mCamera2Source.release();
                    mCamera2Source = null;
                }
            }
        }
    }

    public void stopCameraSource() {
        mPreview.stop();
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
        if(requestCode == REQUEST_STORAGE_PERMISSION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(getActivity(), "STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startPreview() {
        if(checkGooglePlayAvailability()) {
            requestPermissionThenOpenCamera();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(wasActivityResumed) {
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
        if(barcodeDetector != null) {
            barcodeDetector.release();
        }
    }

    public void startPreivew(){
        startPreview();
    }



}
