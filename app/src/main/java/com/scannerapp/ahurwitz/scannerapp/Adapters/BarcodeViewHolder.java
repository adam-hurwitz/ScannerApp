package com.scannerapp.ahurwitz.scannerapp.Adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scannerapp.ahurwitz.scannerapp.R;

/**
 * Created by ahurwitz on 3/31/17.
 */

public class BarcodeViewHolder extends RecyclerView.ViewHolder {

    public FrameLayout barcodeView;

    private TextView barcodeText;
    private String barcodeValue;

    public BarcodeViewHolder(View view, View.OnClickListener onClickListener) {

        super(view);

        barcodeView = (FrameLayout) view.findViewById(R.id.cell);

        barcodeText = (TextView) view.findViewById(R.id.cell_text);

        barcodeView.setOnClickListener(onClickListener);

    }

    public void bind(String barcodeValue){

        this.barcodeValue = barcodeValue;

        barcodeText.setText(barcodeValue);

        barcodeView.setTag(barcodeView.getId(), barcodeValue);

    }

}
