package com.apps.mypajakdaerah;

public class RealisasiItem {
    private String namaJenisPajak;
    private double nilaiRealisasi;

    public RealisasiItem(String namaJenisPajak, double nilaiRealisasi) {
        this.namaJenisPajak = namaJenisPajak;
        this.nilaiRealisasi = nilaiRealisasi;
    }

    public String getNamaJenisPajak() {
        return namaJenisPajak;
    }

    public double getNilaiRealisasi() {
        return nilaiRealisasi;
    }
}
