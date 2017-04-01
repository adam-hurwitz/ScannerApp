package com.scannerapp.ahurwitz.scannerapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scannerapp.ahurwitz.scannerapp.R;

/**
 * Created by ahurwitz on 3/31/17.
 */

public class BarcodeViewHolder extends RecyclerView.ViewHolder {

    private FrameLayout barcodeView;
    private TextView barcodeText;

    private String barcodeValue;

    public BarcodeViewHolder(View view) {

        super(view);

        barcodeView = (FrameLayout) view.findViewById(R.id.cell);

        barcodeText = (TextView) view.findViewById(R.id.cell_text);

    }

    public void onBind(String barcodeValue){

        Log.v(BarcodeViewHolder.class.getSimpleName(), "RESULTS: " + barcodeValue );

        this.barcodeValue = barcodeValue;

        barcodeText.setText(barcodeValue);

    }

}
