package com.apps.mypajakdaerah;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements RealisasiAdapter.OnItemClickListener {

    private EditText etTanggalAwal, etTanggalAkhir;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private TextView tvTotalPajak;
    private RealisasiAdapter realisasiAdapter;
    private List<RealisasiItem> realisasiList;
    private final List<PajakDetail> allPajakDetails = new ArrayList<>();
    private Calendar calendarAwal, calendarAkhir;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat yearFormatter;
    private NumberFormat currencyFormatter;
    private static final String API_URL_RINGKASAN = "https://api2.bapenda.riau.go.id/json/api_realisasi.php";
    private static final String API_URL_DETAIL_BASE = "http://e-keuangan.riau.go.id/api/selectDetailPajakAll.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi komponen UI
        etTanggalAwal = findViewById(R.id.et_tanggal_awal);
        etTanggalAkhir = findViewById(R.id.et_tanggal_akhir);
        Button btnMuatData = findViewById(R.id.btn_muat_data);
        progressBar = findViewById(R.id.progress_bar);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        tvTotalPajak = findViewById(R.id.tv_total_pajak);

        // Tambahkan tombol untuk navigasi
        Button btnHariIni = findViewById(R.id.btn_hari_ini);
        Button btnBulanIni = findViewById(R.id.btn_bulan_ini);
        Button btnTahunIni = findViewById(R.id.btn_tahun_ini);

        // Inisialisasi RecyclerView
        RecyclerView rvRealisasi = findViewById(R.id.rv_realisasi);
        realisasiList = new ArrayList<>();
        realisasiAdapter = new RealisasiAdapter(realisasiList, this);
        rvRealisasi.setLayoutManager(new LinearLayoutManager(this));
        rvRealisasi.setAdapter(realisasiAdapter);

        // Inisialisasi tanggal
        calendarAwal = Calendar.getInstance();
        calendarAkhir = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        yearFormatter = new SimpleDateFormat("yyyy", Locale.getDefault());
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Set listener untuk EditText
        etTanggalAwal.setOnClickListener(v -> showDatePicker(etTanggalAwal, calendarAwal));
        etTanggalAkhir.setOnClickListener(v -> showDatePicker(etTanggalAkhir, calendarAkhir));
        btnMuatData.setOnClickListener(v -> loadApiDataRingkasan());
        btnHariIni.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            etTanggalAwal.setText(dateFormatter.format(today.getTime()));
            etTanggalAkhir.setText(dateFormatter.format(today.getTime()));
            loadApiDataRingkasan();
        });

        btnBulanIni.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            etTanggalAwal.setText(dateFormatter.format(cal.getTime()));

            cal = Calendar.getInstance();
            etTanggalAkhir.setText(dateFormatter.format(cal.getTime()));
            loadApiDataRingkasan();
        });

        btnTahunIni.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            etTanggalAwal.setText(dateFormatter.format(cal.getTime()));

            cal = Calendar.getInstance();
            etTanggalAkhir.setText(dateFormatter.format(cal.getTime()));
            loadApiDataRingkasan();
        });

        tvTotalPajak.setOnClickListener(v -> {
            String tglAkhirStr = etTanggalAkhir.getText().toString();
            if (TextUtils.isEmpty(tglAkhirStr)) {
                Toast.makeText(MainActivity.this, "Silakan pilih tanggal akhir untuk menentukan tahun target.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String selectedYear = yearFormatter.format(Objects.requireNonNull(dateFormatter.parse(tglAkhirStr)));
                if (realisasiList != null && !realisasiList.isEmpty()) {
                    loadAllPajakDetailsForTotalView(selectedYear);
                } else {
                    Toast.makeText(MainActivity.this, "Silakan muat data realisasi terlebih dahulu.", Toast.LENGTH_SHORT).show();
                }
            } catch (java.text.ParseException e) {
                Toast.makeText(MainActivity.this, "Format tanggal akhir tidak valid.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error parsing date: " + e.getMessage());
            }
        });
    }

    // Fungsi untuk menampilkan DatePickerDialog
    private void showDatePicker(final EditText editText, final Calendar calendar) {
        new DatePickerDialog(MainActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);
                    editText.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // Fungsi untuk memuat data ringkasan dari API
    private void loadApiDataRingkasan() {
        String tglAwal = etTanggalAwal.getText().toString();
        String tglAkhir = etTanggalAkhir.getText().toString();

        if (TextUtils.isEmpty(tglAwal) || TextUtils.isEmpty(tglAkhir)) {
            Toast.makeText(this, "Silakan pilih tanggal awal dan tanggal akhir", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.GONE);
        tvTotalPajak.setText("Total Pajak: -");
        realisasiAdapter.updateData(new ArrayList<>());
        allPajakDetails.clear();

        String apiUrl = API_URL_RINGKASAN + "?tgl_awal=" + tglAwal + "&tgl_akhir=" + tglAkhir;
        Log.d("API_URL_RINGKASAN", "URL: " + apiUrl);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("API_RESPONSE_RINGKASAN", response);
                    parseAndDisplayRingkasanData(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Gagal memuat data ringkasan: " + (error.getMessage() != null ? error.getMessage() : "Kesalahan tidak diketahui");
                    tvErrorMessage.setText(errorMessage);
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR_RINGKASAN", "Error: " + errorMessage, error);
                });

        queue.add(stringRequest);
    }

    // Fungsi untuk memparsing dan menampilkan data ringkasan
    private void parseAndDisplayRingkasanData(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            List<RealisasiItem> newList = new ArrayList<>();
            double totalPajak = 0.0;

            double pkb = jsonObject.optDouble("realisasi_pkb", 0.0);
            newList.add(new RealisasiItem("Realisasi PKB", pkb, "PKB"));
            totalPajak += pkb;

            double bbnkb = jsonObject.optDouble("realisasi_bbnkb", 0.0);
            newList.add(new RealisasiItem("Realisasi BBNKB", bbnkb, "BBNKB"));
            totalPajak += bbnkb;

            double ap = jsonObject.optDouble("realisasi_ap", 0.0);
            newList.add(new RealisasiItem("Realisasi Air Permukaan", ap, "PAP"));
            totalPajak += ap;

            double pbbkb = jsonObject.optDouble("realisasi_pbbkb", 0.0);
            newList.add(new RealisasiItem("Realisasi PBBKB", pbbkb, "PBBKB"));
            totalPajak += pbbkb;

            double pajakRokok = jsonObject.optDouble("realisasi_pajak_rokok", 0.0);
            newList.add(new RealisasiItem("Realisasi Pajak Rokok", pajakRokok, "PR"));
            totalPajak += pajakRokok;

            double pab = jsonObject.optDouble("realisasi_pab", 0.0);
            newList.add(new RealisasiItem("Realisasi Pajak Alat Berat", pab, "PAB"));
            totalPajak += pab;

            double omblb = jsonObject.optDouble("realisasi_omblb", 0.0);
            newList.add(new RealisasiItem("Realisasi Opsen Mineral Bukan Logam dan Batuan", omblb, "OMBLB"));
            totalPajak += omblb;

            realisasiAdapter.updateData(newList);
            tvTotalPajak.setText("Total Pajak: " + currencyFormatter.format(totalPajak));

            if (totalPajak == 0.0) {
                tvErrorMessage.setText("Tidak ada data realisasi yang valid ditemukan untuk rentang tanggal ini.");
                tvErrorMessage.setVisibility(View.VISIBLE);
            } else {
                tvErrorMessage.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            String errorMessage = "Kesalahan parsing data ringkasan: " + e.getMessage();
            tvErrorMessage.setText(errorMessage);
            tvErrorMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("JSON_PARSE_ERROR_RINGKASAN", "Error parsing JSON: " + e.getMessage(), e);
            realisasiAdapter.updateData(new ArrayList<>());
            tvTotalPajak.setText("Total Pajak: -");
        }
    }

    // Fungsi untuk memuat detail pajak keseluruhan untuk total view
    private void loadAllPajakDetailsForTotalView(String year) {
        progressBar.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.GONE);
        allPajakDetails.clear();

        String apiUrl = API_URL_DETAIL_BASE + "?key=" + year;
        Log.d("API_URL_TOTAL_DETAIL", "URL Total Detail: " + apiUrl);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("API_RESPONSE_TOTAL_DETAIL", response);
                    parseAndStartTotalDetailActivity(response, year);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Gagal memuat detail pajak keseluruhan: " + (error.getMessage() != null ? error.getMessage() : "Kesalahan tidak diketahui");
                    tvErrorMessage.setText(errorMessage);
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR_TOTAL_DETAIL", "Error: " + errorMessage, error);
                });

        queue.add(stringRequest);
    }

    // Fungsi untuk memparsing dan memulai TotalDetailActivity
    private void parseAndStartTotalDetailActivity(String jsonResponse, String selectedYear) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            int status = jsonObject.optInt("status", 0);

            if (status == 1) {
                JSONArray recordsArray = jsonObject.getJSONArray("records");
                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject record = recordsArray.getJSONObject(i);
                    PajakDetail detail = new PajakDetail(
                            record.optString("id"),
                            record.optString("tahun"),
                            record.optString("kode_pajak"),
                            record.optString("nama_pajak"),
                            record.optString("akronim"),
                            record.optDouble("target"),
                            record.optDouble("realisasi"),
                            record.optDouble("tw1"),
                            record.optDouble("tw2"),
                            record.optDouble("tw3"),
                            record.optDouble("tw4")
                    );
                    // Filter berdasarkan tahun yang diminta (jika API tidak 100% memfilter)
                    if (detail.getTahun().equals(selectedYear)) {
                        allPajakDetails.add(detail);
                    }
                }

                double totalRealisasiMainApi = 0.0;
                for (RealisasiItem item : realisasiList) {
                    totalRealisasiMainApi += item.getNilaiRealisasi();
                }

                Intent intent = new Intent(MainActivity.this, TotalPajakDetailActivity.class);
                intent.putExtra(TotalPajakDetailActivity.EXTRA_ALL_PAJAK_DETAILS, new ArrayList<>(allPajakDetails));
                intent.putExtra(TotalPajakDetailActivity.EXTRA_TOTAL_REALISASI_MAIN, totalRealisasiMainApi);
                intent.putExtra(TotalPajakDetailActivity.EXTRA_SELECTED_YEAR, selectedYear);
                startActivity(intent);

            } else {
                tvErrorMessage.setText("Status API detail keseluruhan tidak berhasil atau data kosong.");
                tvErrorMessage.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            String errorMessage = "Kesalahan parsing data detail keseluruhan: " + e.getMessage();
            tvErrorMessage.setText(errorMessage);
            tvErrorMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("JSON_PARSE_ERROR_TOTAL_DETAIL", "Error parsing JSON: " + e.getMessage(), e);
        }
    }

    // Fungsi untuk menangani klik item pada RecyclerView
    @Override
    public void onItemClick(RealisasiItem item) {
        String tglAkhirStr = etTanggalAkhir.getText().toString();
        if (TextUtils.isEmpty(tglAkhirStr)) {
            Toast.makeText(MainActivity.this, "Silakan pilih tanggal akhir untuk menentukan tahun target.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedYearForDetail = null;
        try {
            selectedYearForDetail = yearFormatter.format(Objects.requireNonNull(dateFormatter.parse(tglAkhirStr)));
        } catch (java.text.ParseException e) {
            Toast.makeText(MainActivity.this, "Format tanggal akhir tidak valid.", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Error parsing date for detail view: " + e.getMessage());
            return;
        }

        List<String> supportedAkronims = new ArrayList<>();
        supportedAkronims.add("PKB");
        supportedAkronims.add("BBNKB");
        supportedAkronims.add("PAP");
        supportedAkronims.add("PBBKB");
        supportedAkronims.add("PR");
        supportedAkronims.add("PAB");
        supportedAkronims.add("OMBLB");

        if (supportedAkronims.contains(item.getAkronimPajak())) {
            Intent intent = new Intent(MainActivity.this, PajakDetailActivity.class);
            intent.putExtra(PajakDetailActivity.EXTRA_AKRONIM_PAJAK, item.getAkronimPajak());
            intent.putExtra(PajakDetailActivity.EXTRA_REALISASI_DARI_LIST, item.getNilaiRealisasi());
            intent.putExtra(PajakDetailActivity.EXTRA_SELECTED_YEAR, selectedYearForDetail);
            intent.putExtra(PajakDetailActivity.EXTRA_SELECTED_END_DATE, tglAkhirStr);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Detail untuk " + item.getNamaJenisPajak() + " belum tersedia.", Toast.LENGTH_SHORT).show();
        }
    }
}