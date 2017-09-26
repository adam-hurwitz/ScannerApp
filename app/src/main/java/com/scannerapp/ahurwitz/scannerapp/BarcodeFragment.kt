package com.scannerapp.ahurwitz.scannerapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.vision.barcode.Barcode
import com.scannerapp.ahurwitz.scannerapp.Adapters.SmartFragmentStatePagerAdapter
import kotlinx.android.synthetic.main.fragment_barcode.*

/**
 * Created by ahurwitz on 9/4/17.
 */
class BarcodeFragment : Fragment() {

    private val TAG = BarcodeFragment::class.java.simpleName
    private lateinit var scannerPagerAdapter: SmartFragmentStatePagerAdapter
    private lateinit var onScannerTypeChange: OnScannerTypeChange
    private var page = 1

    companion object {
        private val NUM_ITEMS = 3
        val BARCODE_TYPE = "BARCODE_TYPE"
        val DATA_MATRIX = "DATA_MATRIX"
        val QR = "QR"
        val PDF417= "PDF417"

        @JvmStatic
        fun newInstance() : BarcodeFragment {
            return BarcodeFragment()
        }
    }

    interface OnScannerTypeChange{
        fun updateScannerType(scannerType: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barcode, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnScannerTypeChange) {
            onScannerTypeChange = context
        } else {
            throw RuntimeException("Hosting activity must implement OnScannerTypeChange")
        }

    }

    private fun initViewPager() {
        scannerPagerAdapter = ScannerPagerAdapter(childFragmentManager)
        viewPager.setAdapter(scannerPagerAdapter)
        viewPager.setCurrentItem(1)
        viewPager.setOffscreenPageLimit(2)
        startPreviews(viewPager)
    }

    class ScannerPagerAdapter(fragmentManager: FragmentManager) : SmartFragmentStatePagerAdapter(fragmentManager) {

        override fun getCount(): Int {
            return NUM_ITEMS
        }

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return BarcodeFragmentOverlay.newInstance(Intent().putExtra(BARCODE_TYPE, DATA_MATRIX))
                1 -> return BarcodeFragmentOverlay.newInstance(Intent().putExtra(BARCODE_TYPE, QR))
                2 -> return BarcodeFragmentOverlay.newInstance(Intent().putExtra(BARCODE_TYPE, PDF417))
                else -> return null
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return "Page " + position
        }

    }

    private fun startPreviews(viewPager: ViewPager) {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                page = position
                when (position) {
                    0 -> onScannerTypeChange.updateScannerType(Barcode.DATA_MATRIX)
                    1 -> onScannerTypeChange.updateScannerType(Barcode.QR_CODE)
                    2 -> onScannerTypeChange.updateScannerType(Barcode.PDF417)
                    else -> return
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    fun addBarcode(barcode: Barcode){
        (scannerPagerAdapter.getRegisteredFragment(page) as BarcodeFragmentOverlay).addBarcode(barcode)
    }

}