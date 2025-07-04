package com.apps.mypajakdaerah;

public class RealisasiItem {
    private String namaJenisPajak;
    private double nilaiRealisasi;
    private String akronimPajak; // Tambahkan ini

    // Perbarui konstruktor
    public RealisasiItem(String namaJenisPajak, double nilaiRealisasi, String akronimPajak) {
        this.namaJenisPajak = namaJenisPajak;
        this.nilaiRealisasi = nilaiRealisasi;
        this.akronimPajak = akronimPajak;
    }

    public String getNamaJenisPajak() {
        return namaJenisPajak;
    }

    public double getNilaiRealisasi() {
        return nilaiRealisasi;
    }

    // Tambahkan getter untuk akronim
    public String getAkronimPajak() {
        return akronimPajak;
    }
}
