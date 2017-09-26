package com.scannerapp.ahurwitz.scannerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.scannerapp.ahurwitz.scannerapp.Camera.*
import java.io.IOException



class BarcodeUpdateActivity : AppCompatActivity(), BarcodeGraphicTracker.BarcodeUpdateListener, BarcodeFragment.OnScannerTypeChange {

    private val TAG = BarcodeUpdateActivity::class.java.simpleName
    private val REQUEST_CAMERA_PERMISSION = 200

    private lateinit var preview: CameraSourcePreview
    private lateinit var graphicOverlay: GraphicOverlay<BarcodeGraphic>
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var camera2Source: Camera2Source

    private var scannerType = Barcode.QR_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        initViews()
    }

    private fun initViews() {
        preview = findViewById(R.id.preview) as CameraSourcePreview
        graphicOverlay = findViewById(R.id.barOverlay) as GraphicOverlay<BarcodeGraphic>
        startBarcodeOverlay()
        startPreview()
    }

    fun startPreview() {
        if (checkGooglePlayAvailability()) {
            requestPermissionThenOpenCamera()
        }
    }

    private fun startBarcodeOverlay() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.content, BarcodeFragment.newInstance())
                .commit()
    }

    private fun checkGooglePlayAvailability(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode == ConnectionResult.SUCCESS) {
            return true
        } else {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 2404).show()
            }
        }
        return false
    }

    private fun requestPermissionThenOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createCameraSourceBack()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun createCameraSourceBack() {

        createBarCodeDetector(scannerType)

        createCameraSource()

        startCameraSource()

    }

    private fun createBarCodeDetector(scannerType: Int) {

        barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(scannerType).build()

        if (barcodeDetector.isOperational()) {
            barcodeDetector.setProcessor(MultiProcessor.Builder(
                    BarcodeTrackerFactory(graphicOverlay, this))
                    .build())
        } else {
            Toast.makeText(this, "BARCODE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show()
        }

    }

    private fun createCameraSource() {
        camera2Source = Camera2Source.Builder(this, barcodeDetector)
                .setFacing(Camera2Source.CAMERA_FACING_BACK)
                .setFocusMode(Camera2Source.CAMERA_AF_CONTINUOUS_PICTURE)
                .build()
    }

    fun startCameraSource() {
        if (camera2Source != null) {
            try {
                preview.start(camera2Source, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source 2.", e)
                camera2Source.release()
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera()
            } else {
                Toast.makeText(this, "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG).show()
                this.finish()
            }
        }
    }


    override fun onBarcodeDetected(barcode: Barcode) {
        runOnUiThread {
            (supportFragmentManager.findFragmentById(R.id.content) as BarcodeFragment).addBarcode(barcode)
        }
    }

    override fun updateScannerType(scannerType: Int) {
        barcodeDetector.release()
        createBarCodeDetector(scannerType)
        camera2Source.refreshDetector(barcodeDetector)
    }

}
