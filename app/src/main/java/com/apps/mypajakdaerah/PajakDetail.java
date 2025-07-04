package com.apps.mypajakdaerah;

import java.io.Serializable;
public class PajakDetail implements Serializable {
    private String id;
    private String tahun;
    private String kodePajak;
    private String namaPajak;
    private String akronim;
    private double target;
    private double realisasi;
    private double tw1;
    private double tw2;
    private double tw3;
    private double tw4;

    // Konstruktor
    public PajakDetail(String id, String tahun, String kodePajak, String namaPajak, String akronim,
                       double target, double realisasi, double tw1, double tw2, double tw3, double tw4) {
        this.id = id;
        this.tahun = tahun;
        this.kodePajak = kodePajak;
        this.namaPajak = namaPajak;
        this.akronim = akronim;
        this.target = target;
        this.realisasi = realisasi;
        this.tw1 = tw1;
        this.tw2 = tw2;
        this.tw3 = tw3;
        this.tw4 = tw4;
    }

    // Getter
    public String getId() { return id; }
    public String getTahun() { return tahun; }
    public String getKodePajak() { return kodePajak; }
    public String getNamaPajak() { return namaPajak; }
    public String getAkronim() { return akronim; }
    public double getTarget() { return target; }
    public double getRealisasi() { return realisasi; }
    public double getTw1() { return tw1; }
    public double getTw2() { return tw2; }
    public double getTw3() { return tw3; }
    public double getTw4() { return tw4; }

    // Anda bisa tambahkan setter jika diperlukan, tapi untuk tampilan biasanya cukup getter.
}
