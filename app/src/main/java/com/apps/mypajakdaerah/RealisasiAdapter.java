package com.apps.mypajakdaerah;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RealisasiAdapter extends RecyclerView.Adapter<RealisasiAdapter.RealisasiViewHolder> {

    private List<RealisasiItem> realisasiList;
    private NumberFormat currencyFormatter; // Untuk format mata uang

    public RealisasiAdapter(List<RealisasiItem> realisasiList) {
        this.realisasiList = realisasiList;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    }

    @NonNull
    @Override
    public RealisasiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_realisasi, parent, false);
        return new RealisasiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RealisasiViewHolder holder, int position) {
        RealisasiItem currentItem = realisasiList.get(position);
        holder.tvNamaPajak.setText(currentItem.getNamaJenisPajak());
        holder.tvNilaiRealisasi.setText(currencyFormatter.format(currentItem.getNilaiRealisasi()));
    }

    @Override
    public int getItemCount() {
        return realisasiList.size();
    }

    public void updateData(List<RealisasiItem> newList) {
        this.realisasiList.clear();
        this.realisasiList.addAll(newList);
        notifyDataSetChanged(); // Memberi tahu RecyclerView bahwa data telah berubah
    }

    public static class RealisasiViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNamaPajak;
        public TextView tvNilaiRealisasi;

        public RealisasiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaPajak = itemView.findViewById(R.id.tv_item_nama_pajak);
            tvNilaiRealisasi = itemView.findViewById(R.id.tv_item_nilai_realisasi);
        }
    }
}
