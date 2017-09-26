package com.scannerapp.ahurwitz.scannerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.vision.barcode.Barcode
import com.scannerapp.ahurwitz.scannerapp.Adapters.BarcodeAdapter
import com.scannerapp.ahurwitz.scannerapp.Utils.Utils
import kotlinx.android.synthetic.main.fragment_barcode_overlay.*
import rx.subscriptions.CompositeSubscription

/**
 * Created by ahurwitz on 9/4/17.
 */
class BarcodeFragmentOverlay : Fragment() {

    private val TAG = BarcodeFragmentOverlay::class.java.simpleName

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var adapter: BarcodeAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var compositeSubscription = CompositeSubscription()
    private var PEEKING_HEIGHT = 116
    private var barcodes: ArrayList<String> = arrayListOf("")

    companion object {
        private val NORMAL = "NORMAL"
        private val SMALL = "SMALL"
        private val IMG_WIDTH = 250
        private val IMG_HEIGHT = 250
        private val IMG_WIDTH_SMALL = 40
        private val IMG_HEIGHT_SMALL = 40
        private val PDF417_IMG_WIDTH = 500
        private val PDF417_IMG_HEIGHT = 200
        private val PDF417_IMG_WIDTH_SMALL = 80
        private val PDF417_IMG_HEIGHT_SMALL = 32

        @JvmStatic
        fun newInstance(intent: Intent): BarcodeFragmentOverlay {
            return BarcodeFragmentOverlay().apply {
                arguments = intent.extras
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barcode_overlay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBarcodeOverlays()
        initBottomSheet()
        initRecyclerView()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
    }

    private fun initBarcodeOverlays() {
        var type = arguments.getString(BarcodeFragment.BARCODE_TYPE)
        Glide.with(activity)
                .load(getBarcodeImageAndSize(type, NORMAL).first)
                .override(getBarcodeImageAndSize(type, NORMAL).second, getBarcodeImageAndSize(type, NORMAL).third)
                .centerCrop()
                .into(barcodeImg)
        Glide.with(activity)
                .load(getBarcodeImageAndSize(type, SMALL).first)
                .override(getBarcodeImageAndSize(type, SMALL).second, getBarcodeImageAndSize(type, SMALL).third)
                .centerCrop()
                .into(handle)
    }

    private fun getBarcodeImageAndSize(type: String, size: String): Triple<Int, Int, Int> {
        when (type) {
            BarcodeFragment.DATA_MATRIX -> return Triple(R.drawable.data_matrix, getImgDimen(type, size).first, getImgDimen(type, size).second)
            BarcodeFragment.QR -> return Triple(R.drawable.qr, getImgDimen(type, size).first, getImgDimen(type, size).second)
            BarcodeFragment.PDF417 -> return Triple(R.drawable.pdf417, getImgDimen(type, size).first, getImgDimen(type, size).second)
            else -> return Triple(0, 0, 0)
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from<View>(bottomSheet)
        bottomSheetBehavior.peekHeight = PEEKING_HEIGHT

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                Log.d(TAG, "barcode: " + newState + " " + (adapter.itemCount > 0).toString())

                if (adapter.itemCount > 0) {
                    when (newState) {
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING, BottomSheetBehavior.STATE_EXPANDED -> {
                            handle.visibility = View.GONE
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            handle.visibility = View.VISIBLE
                            bottomSheetBehavior.peekHeight = PEEKING_HEIGHT
                        }
                    }
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
    }

    private fun initRecyclerView() {
        adapter = BarcodeAdapter()
        recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        layoutManager.reverseLayout = true
        recyclerView.setHasFixedSize(true)
        onCellSelected()
    }

    fun addBarcode(barcode: Barcode) {
        if (!barcodes.contains(barcode.displayValue)) {
            barcodes.add(barcode.displayValue)
            adapter.swapItems(barcodes)

            layoutManager.scrollToPosition(adapter.itemCount - 1)
            adapter.notifyItemChanged(adapter.itemCount - 1)

            if (adapter.itemCount > 0 && adapter.itemCount < 4) {
                bottomSheetBehavior.peekHeight = adapter.itemCount * 168
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            handle.visibility = View.GONE
        }
    }

    private fun onCellSelected() {
        compositeSubscription.add(
                adapter.onCellSelectedEvent()
                        .subscribe({ barcode ->
                            if (Utils.isValidURL(barcode)) {
                                val webpage = Uri.parse(barcode)
                                val intent = Intent(Intent.ACTION_VIEW, webpage)
                                if (intent.resolveActivity(activity.packageManager) != null) {
                                    startActivity(intent)
                                }
                            }
                        }, { throwable -> Log.e(BarcodeFragment::class.java.simpleName, throwable.toString()) })
        )
    }

    fun getImgDimen(type: String, size: String): Pair<Int,Int>{
        when(type){
            BarcodeFragment.DATA_MATRIX, BarcodeFragment.QR -> {
                if (size == NORMAL) {
                    return Pair(IMG_WIDTH, IMG_HEIGHT)
                } else {
                    return Pair(IMG_WIDTH_SMALL, IMG_HEIGHT_SMALL)
                }
            }
            BarcodeFragment.PDF417 -> {
                if (size == NORMAL) {
                    return Pair(PDF417_IMG_WIDTH, PDF417_IMG_HEIGHT)
                } else {
                    return Pair(PDF417_IMG_WIDTH_SMALL, PDF417_IMG_HEIGHT_SMALL)
                }
            }
            else -> return Pair(0,0)
        }
    }
}