package com.scannerapp.ahurwitz.scannerapp.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scannerapp.ahurwitz.scannerapp.R;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.ReplaySubject;

/**
 * Created by ahurwitz on 3/31/17.
 */

public class BarcodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<String> barcodeValues = new ArrayList<>();

    private ReplaySubject<String> onCellSelectedSubscriber = ReplaySubject.create();

    BarcodeViewHolder viewHolder;

    public BarcodeAdapter() {

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.barcode_adapter_cell, parent, false);

        viewHolder = new BarcodeViewHolder(view, this);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {

        ((BarcodeViewHolder) holder).bind(barcodeValues.get(pos));

    }

    @Override
    public int getItemCount() {

        return barcodeValues.size();
    }

    public void addItems(List<String> barcodeValues) {
        this.barcodeValues.addAll(barcodeValues);
        notifyDataSetChanged();
    }

    public void swapItems(List<String> barcodeValues) {
        this.barcodeValues.clear();
        this.barcodeValues.addAll(barcodeValues);
        notifyDataSetChanged();
    }

    public Observable<String> onCellSelectedEvent() {
        return onCellSelectedSubscriber.asObservable();
    }

    @Override
    public void onClick(View v) {
        onCellSelectedSubscriber.onNext((String) v.getTag(viewHolder.barcodeView.getId()));
    }


}
