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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RealisasiAdapter.OnItemClickListener {
    //--------------------------------------------------------
    private EditText etTanggalAwal, etTanggalAkhir;
    private Button btnMuatData;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private TextView tvTotalPajak;
    // Tombol-tombol baru untuk pilihan cepat
    private Button btnHariIni, btnBulanIni, btnTahunIni;
    private RecyclerView rvRealisasi;
    private RealisasiAdapter realisasiAdapter;
    private List<RealisasiItem> realisasiList;
    private Calendar calendarAwal, calendarAkhir;
    private SimpleDateFormat dateFormatter;
    private NumberFormat currencyFormatter;
    private static final String API_URL_BASE = "https://api2.bapenda.riau.go.id/json/api_realisasi.php";
    //-------------------------------------------------------
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

        etTanggalAwal = findViewById(R.id.et_tanggal_awal);
        etTanggalAkhir = findViewById(R.id.et_tanggal_akhir);
        btnMuatData = findViewById(R.id.btn_muat_data);
        progressBar = findViewById(R.id.progress_bar);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        tvTotalPajak = findViewById(R.id.tv_total_pajak);

        btnHariIni = findViewById(R.id.btn_hari_ini);
        btnBulanIni = findViewById(R.id.btn_bulan_ini);
        btnTahunIni = findViewById(R.id.btn_tahun_ini);

        rvRealisasi = findViewById(R.id.rv_realisasi);
        realisasiList = new ArrayList<>();
        realisasiAdapter = new RealisasiAdapter(realisasiList, this);
        rvRealisasi.setLayoutManager(new LinearLayoutManager(this));
        rvRealisasi.setAdapter(realisasiAdapter);

        calendarAwal = Calendar.getInstance();
        calendarAkhir = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        etTanggalAwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etTanggalAwal, calendarAwal);
            }
        });

        etTanggalAkhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etTanggalAkhir, calendarAkhir);
            }
        });

        btnMuatData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadApiData();
            }
        });

        btnHariIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar today = Calendar.getInstance();
                etTanggalAwal.setText(dateFormatter.format(today.getTime()));
                etTanggalAkhir.setText(dateFormatter.format(today.getTime()));
                loadApiData();
            }
        });

        btnBulanIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                etTanggalAwal.setText(dateFormatter.format(cal.getTime()));

                cal = Calendar.getInstance();
                etTanggalAkhir.setText(dateFormatter.format(cal.getTime()));
                loadApiData();
            }
        });

        btnTahunIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_YEAR, 1);
                etTanggalAwal.setText(dateFormatter.format(cal.getTime()));

                cal = Calendar.getInstance();
                etTanggalAkhir.setText(dateFormatter.format(cal.getTime()));
                loadApiData();
            }
        });
    }

    private void showDatePicker(final EditText editText, final Calendar calendar) {
        new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        editText.setText(dateFormatter.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void loadApiData() {
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

        String apiUrl = API_URL_BASE + "?tgl_awal=" + tglAwal + "&tgl_akhir=" + tglAkhir;
        Log.d("API_URL", "URL: " + apiUrl);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("API_RESPONSE", response);
                        parseAndDisplayData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        String errorMessage = "Gagal memuat data: " + (error.getMessage() != null ? error.getMessage() : "Kesalahan tidak diketahui");
                        tvErrorMessage.setText(errorMessage);
                        tvErrorMessage.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR", "Error: " + errorMessage, error);
                    }
                });

        queue.add(stringRequest);
    }

    private void parseAndDisplayData(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            List<RealisasiItem> newList = new ArrayList<>();
            double totalPajak = 0.0;

            double pkb = jsonObject.optDouble("realisasi_pkb", 0.0);
            newList.add(new RealisasiItem("Realisasi PKB", pkb, "PKB"));
            totalPajak += pkb; // Tambahkan ke total

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


            realisasiAdapter.updateData(newList);
            tvTotalPajak.setText("Total Pajak: " + currencyFormatter.format(totalPajak));

            if (totalPajak == 0.0) {
                tvErrorMessage.setText("Tidak ada data realisasi yang valid ditemukan untuk rentang tanggal ini.");
                tvErrorMessage.setVisibility(View.VISIBLE);
            } else {
                tvErrorMessage.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            String errorMessage = "Kesalahan parsing data: " + e.getMessage();
            tvErrorMessage.setText(errorMessage);
            tvErrorMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("JSON_PARSE_ERROR", "Error parsing JSON: " + e.getMessage(), e);
            realisasiAdapter.updateData(new ArrayList<>());
            tvTotalPajak.setText("Total Pajak: -");
        }
    }

    @Override
    public void onItemClick(RealisasiItem item) {
        // Akronim yang didukung untuk detail
        List<String> supportedAkronims = new ArrayList<>();
        supportedAkronims.add("PKB");
        supportedAkronims.add("BBNKB");
        supportedAkronims.add("PAP");
        supportedAkronims.add("PBBKB");
        supportedAkronims.add("PR");
        // Tambahkan akronim lain jika API detail Anda mendukungnya (misal PAB, OMBLB)
        // supportedAkronims.add("PAB");
        // supportedAkronims.add("OMBLB");


        if (supportedAkronims.contains(item.getAkronimPajak())) {
            Intent intent = new Intent(MainActivity.this, PajakDetailActivity.class);
            intent.putExtra(PajakDetailActivity.EXTRA_AKRONIM_PAJAK, item.getAkronimPajak());
            // ----- TAMBAHAN PENTING INI -----
            intent.putExtra(PajakDetailActivity.EXTRA_REALISASI_DARI_LIST, item.getNilaiRealisasi());
            // ----------------------------------
            startActivity(intent);
        } else {
            Toast.makeText(this, "Detail untuk " + item.getNamaJenisPajak() + " belum tersedia.", Toast.LENGTH_SHORT).show();
        }
    }
}