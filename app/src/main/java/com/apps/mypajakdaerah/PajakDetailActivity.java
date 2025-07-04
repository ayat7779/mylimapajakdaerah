package com.apps.mypajakdaerah;

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

public class PajakDetailActivity extends AppCompatActivity {

    public static final String EXTRA_AKRONIM_PAJAK = "extra_akronim_pajak";
    public static final String EXTRA_REALISASI_DARI_LIST = "extra_realisasi_dari_list";

    private TextView tvDetailNamaPajak, tvDetailTahun, tvDetailTarget, tvDetailRealisasi;
    private TextView tvDetailPersentase; // Tambahkan ini
    private TextView tvTw1, tvTw2, tvTw3, tvTw4;
    private TextView tvDetailErrorMessage;
    private ProgressBar progressBar;

    private NumberFormat currencyFormatter;
    private String selectedAkronim = "";
    private double realisasiDariMainList = 0.0;
    private double targetDariDetailApi = 0.0; // Tambahkan ini untuk menyimpan nilai target


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

        tvDetailNamaPajak = findViewById(R.id.tv_detail_nama_pajak);
        tvDetailTahun = findViewById(R.id.tv_detail_tahun);
        tvDetailTarget = findViewById(R.id.tv_detail_target);
        tvDetailRealisasi = findViewById(R.id.tv_detail_realisasi);
        tvDetailPersentase = findViewById(R.id.tv_detail_persentase); // Inisialisasi ini
        tvTw1 = findViewById(R.id.tv_tw1);
        tvTw2 = findViewById(R.id.tv_tw2);
        tvTw3 = findViewById(R.id.tv_tw3);
        tvTw4 = findViewById(R.id.tv_tw4);
        tvDetailErrorMessage = findViewById(R.id.tv_detail_error_message);
        progressBar = findViewById(R.id.detail_progress_bar);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        if (getIntent().hasExtra(EXTRA_AKRONIM_PAJAK)) {
            selectedAkronim = getIntent().getStringExtra(EXTRA_AKRONIM_PAJAK);
            realisasiDariMainList = getIntent().getDoubleExtra(EXTRA_REALISASI_DARI_LIST, 0.0);

            setTitle("Detail " + selectedAkronim);
            loadPajakDetailData();
        } else {
            Toast.makeText(this, "Akronim pajak tidak ditemukan.", Toast.LENGTH_SHORT).show();
            tvDetailErrorMessage.setText("Akronim pajak tidak ditemukan.");
            tvDetailErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    private void loadPajakDetailData() {
        progressBar.setVisibility(View.VISIBLE);
        tvDetailErrorMessage.setVisibility(View.GONE);
        resetDetailTextViews();

        tvDetailRealisasi.setText("Realisasi: " + currencyFormatter.format(realisasiDariMainList));
        // Reset persentase awal
        tvDetailPersentase.setText("Persentase: -");


        String apiUrl = "http://e-keuangan.riau.go.id/api/getDataPajak.php";
        Log.d("API_URL_DETAIL", "URL Detail: " + apiUrl);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("API_RESPONSE_DETAIL", response);
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
                        Log.e("API_ERROR_DETAIL", "Error: " + errorMessage, error);
                    }
                });
        queue.add(stringRequest);
    }

    private void parseAndDisplayDetailData(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            int status = jsonObject.optInt("status", 0);

            if (status == 1) {
                JSONArray recordsArray = jsonObject.getJSONArray("records");
                PajakDetail foundDetail = null;

                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject record = recordsArray.getJSONObject(i);
                    String akronim = record.optString("akronim");

                    if (akronim.equalsIgnoreCase(selectedAkronim)) {
                        foundDetail = new PajakDetail(
                                record.optString("id"),
                                record.optString("tahun"),
                                record.optString("kode_pajak"),
                                record.optString("nama_pajak"),
                                akronim,
                                record.optDouble("target"),
                                record.optDouble("realisasi"),
                                record.optDouble("tw1"),
                                record.optDouble("tw2"),
                                record.optDouble("tw3"),
                                record.optDouble("tw4")
                        );
                        break;
                    }
                }

                if (foundDetail != null) {
                    tvDetailNamaPajak.setText(foundDetail.getNamaPajak() + " (" + foundDetail.getAkronim() + ")");
                    tvDetailTahun.setText("Tahun: " + foundDetail.getTahun());

                    targetDariDetailApi = foundDetail.getTarget(); // Simpan nilai target
                    tvDetailTarget.setText("Target: " + currencyFormatter.format(targetDariDetailApi));

                    tvDetailRealisasi.setText("Realisasi: " + currencyFormatter.format(realisasiDariMainList));

                    // --- Perhitungan dan Tampilan Persentase Realisasi ---
                    if (targetDariDetailApi > 0) {
                        double persentase = (realisasiDariMainList / targetDariDetailApi) * 100;
                        tvDetailPersentase.setText("Persentase: " + String.format(Locale.getDefault(), "%.2f%%", persentase));
                        tvDetailPersentase.setTextColor(getResources().getColor(android.R.color.black)); // Atur warna agar jelas
                    } else {
                        tvDetailPersentase.setText("Persentase: 0.00%"); // Jika target 0, persentase 0
                        tvDetailPersentase.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                    // ----------------------------------------------------

                    tvTw1.setText("TW1: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw1() * 100));
                    tvTw2.setText("TW2: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw2() * 100));
                    tvTw3.setText("TW3: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw3() * 100));
                    tvTw4.setText("TW4: " + String.format(Locale.getDefault(), "%.2f%%", foundDetail.getTw4() * 100));

                    tvDetailErrorMessage.setVisibility(View.GONE);
                } else {
                    tvDetailErrorMessage.setText("Detail pajak untuk '" + selectedAkronim + "' tidak ditemukan.");
                    tvDetailErrorMessage.setVisibility(View.VISIBLE);
                    // Tidak reset realisasi dan persentase
                    tvDetailPersentase.setText("Persentase: -"); // Reset persentase jika detail tidak ditemukan
                    tvDetailTarget.setText("Target: -"); // Reset target jika detail tidak ditemukan
                }

            } else {
                tvDetailErrorMessage.setText("Status API tidak berhasil.");
                tvDetailErrorMessage.setVisibility(View.VISIBLE);
                // Tidak reset realisasi dan persentase
                tvDetailPersentase.setText("Persentase: -");
                tvDetailTarget.setText("Target: -");
            }

        } catch (JSONException e) {
            String errorMessage = "Kesalahan parsing detail data: " + e.getMessage();
            tvDetailErrorMessage.setText(errorMessage);
            tvDetailErrorMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("JSON_PARSE_ERROR_DETAIL", "Error parsing JSON: " + e.getMessage(), e);
            // Tidak reset realisasi dan persentase
            tvDetailPersentase.setText("Persentase: -");
            tvDetailTarget.setText("Target: -");
        }
    }

    private void resetDetailTextViews() {
        tvDetailNamaPajak.setText("Nama Pajak: -");
        tvDetailTahun.setText("Tahun: -");
        tvDetailTarget.setText("Target: -"); // Reset target juga di sini
        // tvDetailRealisasi.setText("Realisasi: -"); // JANGAN RESET INI
        tvDetailPersentase.setText("Persentase: -"); // Reset persentase di sini
        tvTw1.setText("TW1: -");
        tvTw2.setText("TW2: -");
        tvTw3.setText("TW3: -");
        tvTw4.setText("TW4: -");
    }
}