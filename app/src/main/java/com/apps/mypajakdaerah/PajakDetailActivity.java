package com.apps.mypajakdaerah;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Locale;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PajakDetailActivity extends AppCompatActivity {
    // Deklarasi konstanta untuk intent
    public static final String EXTRA_AKRONIM_PAJAK = "extra_akronim_pajak";
    public static final String EXTRA_REALISASI_DARI_LIST = "extra_realisasi_dari_list";
    public static final String EXTRA_SELECTED_YEAR = "extra_selected_year";
    public static final String EXTRA_SELECTED_END_DATE = "extra_selected_end_date";

    // Deklarasi komponen UI
    private TextView tvDetailNamaPajak, tvDetailTahun, tvDetailTarget, tvDetailRealisasi;
    private TextView tvDetailPersentase;
    private TextView tvTw1, tvTw2, tvTw3, tvTw4;
    private TextView tvDetailErrorMessage;
    private ProgressBar progressBar;
    private TextView tvTargetNarasi;

    // Deklarasi objek untuk format mata uang dan tanggal
    private NumberFormat currencyFormatter;
    private String selectedAkronim = "";
    private double realisasiDariMainList = 0.0;
    private double targetDariDetailApi = 0.0;
    private String selectedYear = "";
    private String selectedEndDateStr = "";
    private SimpleDateFormat dateFormatter;

    // Deklarasi URL API
    private static final String API_URL_DETAIL_BASE = "http://e-keuangan.riau.go.id/api/selectDetailPajakAll.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pajak_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi komponen UI
        tvDetailNamaPajak = findViewById(R.id.tv_detail_nama_pajak);
        tvDetailTahun = findViewById(R.id.tv_detail_tahun);
        tvDetailTarget = findViewById(R.id.tv_detail_target);
        tvDetailRealisasi = findViewById(R.id.tv_detail_realisasi);
        tvDetailPersentase = findViewById(R.id.tv_detail_persentase);
        tvTw1 = findViewById(R.id.tv_tw1);
        tvTw2 = findViewById(R.id.tv_tw2);
        tvTw3 = findViewById(R.id.tv_tw3);
        tvTw4 = findViewById(R.id.tv_tw4);
        tvDetailErrorMessage = findViewById(R.id.tv_detail_error_message);
        progressBar = findViewById(R.id.detail_progress_bar);
        tvTargetNarasi = findViewById(R.id.tv_target_narasi);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (getIntent().hasExtra(EXTRA_AKRONIM_PAJAK) &&
                getIntent().hasExtra(EXTRA_SELECTED_YEAR)&&
                getIntent().hasExtra(EXTRA_SELECTED_END_DATE)) {
            selectedAkronim = getIntent().getStringExtra(EXTRA_AKRONIM_PAJAK);
            realisasiDariMainList = getIntent().getDoubleExtra(EXTRA_REALISASI_DARI_LIST, 0.0);
            selectedYear = getIntent().getStringExtra(EXTRA_SELECTED_YEAR);
            selectedEndDateStr = getIntent().getStringExtra(EXTRA_SELECTED_END_DATE);

            setTitle("Detail " + selectedAkronim + " (" + selectedYear + ")");
            loadPajakDetailData();
        } else {
            Toast.makeText(this, "Akronim, tahun, atau tanggal akhir tidak ditemukan.", Toast.LENGTH_SHORT).show();
            tvDetailErrorMessage.setText("Akronim, tahun, atau tanggal akhir tidak ditemukan.");
            tvDetailErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    // Fungsi untuk memuat data detail pajak dari API
    private void loadPajakDetailData() {
        progressBar.setVisibility(View.VISIBLE);
        tvDetailErrorMessage.setVisibility(View.GONE);
        resetDetailTextViews();

        tvDetailRealisasi.setText("Realisasi: " + currencyFormatter.format(realisasiDariMainList));
        tvDetailPersentase.setText("Persentase: -");
        tvDetailTahun.setText("Tahun: " + selectedYear);

        // Sesuaikan format URL dengan parameter 'key'
        String apiUrl = API_URL_DETAIL_BASE + "?key=" + selectedYear;
        Log.d("API_URL_DETAIL_SINGLE", "URL Detail Single: " + apiUrl);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("API_RESPONSE_DETAIL_SINGLE", response);
                        parseAndDisplayDetailData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        String errorMessage = "Gagal memuat detail data: " + (error.getMessage() != null ? error.getMessage() : "Kesalahan tidak diketahui");
                        tvDetailErrorMessage.setText(errorMessage);
                        tvDetailErrorMessage.setVisibility(View.VISIBLE);
                        Toast.makeText(PajakDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR_DETAIL_SINGLE", "Error: " + errorMessage, error);
                    }
                });
        queue.add(stringRequest);
    }

    // Fungsi untuk memparsing dan menampilkan data detail pajak
    private void parseAndDisplayDetailData(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            // Cek status, asumsikan status 1 = berhasil
            int status = jsonObject.optInt("status", 0);

            if (status == 1) {
                // Asumsi data record ada di bawah key "data" jika statusnya 1
                JSONArray recordsArray = jsonObject.getJSONArray("records"); // Mungkin "records" atau "data"
                if (recordsArray == null) {
                    recordsArray = jsonObject.optJSONArray("records"); // Jika tidak ada, coba "records"
                }

                if (recordsArray == null) {
                    throw new JSONException("No 'data' or 'records' array found in JSON response.");
                }

                PajakDetail foundDetail = null;

                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject record = recordsArray.getJSONObject(i);

                    String akronim = record.optString("akronim");
                    String tahunData = record.optString("tahun");

                    // Filter berdasarkan akronim DAN tahun yang dipilih
                    if (akronim.equalsIgnoreCase(selectedAkronim) && tahunData.equals(selectedYear)) {
                        foundDetail = new PajakDetail(
                                record.optString("id"),
                                record.optString("tahun"),      // Asumsi key "Tahun"
                                record.optString("kode_pajak"), // Asumsi key "KodePajak"
                                record.optString("nama_pajak"), // Asumsi key "NamaPajak"
                                akronim,
                                record.optDouble("target"),    // Asumsi key "Target"
                                record.optDouble("realisasi"), // Asumsi key "realisasi"
                                record.optDouble("tw1"),       // Asumsi key "Tw1"
                                record.optDouble("tw2"),       // Asumsi key "Tw2"
                                record.optDouble("tw3"),       // Asumsi key "Tw3"
                                record.optDouble("tw4")        // Asumsi key "Tw4"
                        );
                        break;
                    }
                }

                if (foundDetail != null) {
                    tvDetailNamaPajak.setText(foundDetail.getNamaPajak() + " (" + foundDetail.getAkronim() + ")");
                    tvDetailTahun.setText("Tahun: " + foundDetail.getTahun());

                    targetDariDetailApi = foundDetail.getTarget();
                    tvDetailTarget.setText("Target: " + currencyFormatter.format(targetDariDetailApi));
                    tvDetailRealisasi.setText("Realisasi: " + currencyFormatter.format(realisasiDariMainList));

                    double persentaseRealisasi = 0.0;
                    if (targetDariDetailApi > 0) {
                        persentaseRealisasi = (realisasiDariMainList / targetDariDetailApi) * 100;
                        tvDetailPersentase.setText("Persentase: " + String.format(Locale.getDefault(), "%.2f%%", persentaseRealisasi));
                        tvDetailPersentase.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        tvDetailPersentase.setTypeface(null, Typeface.BOLD_ITALIC);
                    } else {
                        tvDetailPersentase.setText("Persentase: 0.00%");
                        tvDetailPersentase.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tvDetailPersentase.setTypeface(null, android.graphics.Typeface.BOLD);
                    }

                    tvTw1.setText("TW1: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw1() * 100));
                    tvTw2.setText("TW2: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw2() * 100));
                    tvTw3.setText("TW3: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw3() * 100));
                    tvTw4.setText("TW4: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw4() * 100));

                    // --- LOGIKA PENANDA TW & NARASI TARGET ---
                    try {
                        Date endDate = dateFormatter.parse(selectedEndDateStr);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endDate);
                        int month = cal.get(Calendar.MONTH) + 1; // Bulan dimulai dari 0 (Januari)

                        tvTw1.setTextColor(Color.BLACK);
                        tvTw2.setTextColor(Color.BLACK);
                        tvTw3.setTextColor(Color.BLACK);
                        tvTw4.setTextColor(Color.BLACK);
                        tvTw1.setTypeface(null, android.graphics.Typeface.NORMAL);
                        tvTw2.setTypeface(null, android.graphics.Typeface.NORMAL);
                        tvTw3.setTypeface(null, android.graphics.Typeface.NORMAL);
                        tvTw4.setTypeface(null, android.graphics.Typeface.NORMAL);
                        tvTargetNarasi.setVisibility(View.GONE); // Sembunyikan narasi secara default

                        double targetPersentaseTriwulan = 0.0;
                        String triwulanNama = "";

                        if (month >= 1 && month <= 3) {
                            tvTw1.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            tvTw1.setTypeface(null, android.graphics.Typeface.BOLD);
                            targetPersentaseTriwulan = foundDetail.getTw1() * 100;
                            triwulanNama = "TW-1";
                        } else if (month >= 4 && month <= 6) {
                            tvTw2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            tvTw2.setTypeface(null, android.graphics.Typeface.BOLD);
                            targetPersentaseTriwulan = foundDetail.getTw2() * 100;
                            triwulanNama = "TW-2";
                        } else if (month >= 7 && month <= 9) {
                            tvTw3.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            tvTw3.setTypeface(null, android.graphics.Typeface.BOLD);
                            targetPersentaseTriwulan = foundDetail.getTw3() * 100;
                            triwulanNama = "TW-3";
                        } else if (month >= 10 && month <= 12) {
                            tvTw4.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            tvTw4.setTypeface(null, android.graphics.Typeface.BOLD);
                            targetPersentaseTriwulan = foundDetail.getTw4() * 100;
                            triwulanNama = "TW-4";
                        }

                        // Logika Narasi Target
                        if (targetDariDetailApi > 0 && targetPersentaseTriwulan > 0) {
                            if (persentaseRealisasi < targetPersentaseTriwulan) {
                                double selisih = targetPersentaseTriwulan - persentaseRealisasi;
                                String narasi = "Belum mencapai target " + triwulanNama + "\ndengan selisih " +
                                        String.format(Locale.getDefault(), "%.2f%%", selisih);
                                tvTargetNarasi.setText(narasi);
                                tvTargetNarasi.setVisibility(View.VISIBLE);
                            }
                        }

                    } catch (java.text.ParseException e) {
                        Log.e("PajakDetailActivity", "Error parsing selectedEndDateStr: " + e.getMessage());
                    }
                    // --- AKHIR LOGIKA PENANDA TW & NARASI TARGET ---
                    tvDetailErrorMessage.setVisibility(View.GONE);
                } else {
                    tvDetailErrorMessage.setText("Detail pajak untuk '" + selectedAkronim + "' tahun '" + selectedYear + "' tidak ditemukan.");
                    tvDetailErrorMessage.setVisibility(View.VISIBLE);
                    tvDetailPersentase.setText("Persentase: -");
                    tvDetailTarget.setText("Target: -");
                    tvTargetNarasi.setVisibility(View.GONE); // Sembunyikan narasi
                    resetTwTextViews(); // Pastikan TW juga direset
                }

            } else {
                tvDetailErrorMessage.setText("Status API tidak berhasil atau data kosong.");
                tvDetailErrorMessage.setVisibility(View.VISIBLE);
                tvDetailPersentase.setText("Persentase: -");
                tvDetailTarget.setText("Target: -");
                tvTargetNarasi.setVisibility(View.GONE); // Sembunyikan narasi
                resetTwTextViews(); // Pastikan TW juga direset
            }

        } catch (JSONException e) {
            String errorMessage = "Kesalahan parsing detail data: " + e.getMessage();
            tvDetailErrorMessage.setText(errorMessage);
            tvDetailErrorMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("JSON_PARSE_ERROR_DETAIL_SINGLE", "Error parsing JSON: " + e.getMessage(), e);
            tvDetailPersentase.setText("Persentase: -");
            tvDetailTarget.setText("Target: -");
            tvTargetNarasi.setVisibility(View.GONE); // Sembunyikan narasi
            resetTwTextViews(); // Pastikan TW juga direset
        }
    }

    // Fungsi untuk mereset tampilan detail jika data tidak ditemukan
    private void resetDetailTextViews() {
        tvDetailNamaPajak.setText("Nama Pajak: -");
        tvDetailTahun.setText("Tahun: -");
        tvDetailTarget.setText("Target: -");
        tvDetailPersentase.setText("Persentase: -");
        tvTargetNarasi.setVisibility(View.GONE); // Sembunyikan narasi
        resetTwTextViews(); // Panggil fungsi reset TW
    }

    // Fungsi untuk mereset tampilan TW jika data tidak ditemukan
    private void resetTwTextViews() {
        tvTw1.setText("TW1: -");
        tvTw2.setText("TW2: -");
        tvTw3.setText("TW3: -");
        tvTw4.setText("TW4: -");
        // Reset warna dan gaya teks TW ke default
        tvTw1.setTextColor(Color.BLACK);
        tvTw2.setTextColor(Color.BLACK);
        tvTw3.setTextColor(Color.BLACK);
        tvTw4.setTextColor(Color.BLACK);
        tvTw1.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTw2.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTw3.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTw4.setTypeface(null, android.graphics.Typeface.NORMAL);
    }
}