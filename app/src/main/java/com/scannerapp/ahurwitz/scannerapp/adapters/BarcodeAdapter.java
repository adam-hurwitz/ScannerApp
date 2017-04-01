package com.scannerapp.ahurwitz.scannerapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scannerapp.ahurwitz.scannerapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahurwitz on 3/31/17.
 */

public class BarcodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> barcodeValues = new ArrayList<>();

    public BarcodeAdapter() {

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.barcode_adapter_cell, parent, false);

        return new BarcodeViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {

        ((BarcodeViewHolder) holder).onBind(barcodeValues.get(pos));


    }

    @Override
    public int getItemCount() {
        return barcodeValues.size();
    }

    public void addItems(List<String> barcodeValues) {

        this.barcodeValues.addAll(barcodeValues);
        notifyDataSetChanged();
        Log.v(BarcodeAdapter.class.getSimpleName(), "RESULTS: " + barcodeValues.size());


    }

    public void swapItems(List<String> qReplies) {
        barcodeValues.clear();
        barcodeValues.addAll(qReplies);
        notifyDataSetChanged();
    }

}
