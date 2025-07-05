package com.apps.mypajakdaerah;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TotalPajakDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ALL_PAJAK_DETAILS = "extra_all_pajak_details";
    public static final String EXTRA_TOTAL_REALISASI_MAIN = "extra_total_realisasi_main";
    public static final String EXTRA_SELECTED_YEAR = "extra_selected_year"; // <--- ADD THIS LINE

    private TextView tvTotalTarget, tvTotalRealisasi, tvTotalPersentase;
    private TextView tvTotalPajakTitleYear;
    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_total_pajak_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTotalPajakTitleYear = findViewById(R.id.tv_total_pajak_title_year);
        tvTotalTarget = findViewById(R.id.tv_total_target);
        tvTotalRealisasi = findViewById(R.id.tv_total_realisasi);
        tvTotalPersentase = findViewById(R.id.tv_total_persentase);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        if (getIntent().hasExtra(EXTRA_ALL_PAJAK_DETAILS) &&
                getIntent().hasExtra(EXTRA_TOTAL_REALISASI_MAIN) &&
                getIntent().hasExtra(EXTRA_SELECTED_YEAR)) {

            ArrayList<PajakDetail> allPajakDetails = (ArrayList<PajakDetail>) getIntent().getSerializableExtra(EXTRA_ALL_PAJAK_DETAILS);
            double totalRealisasiMain = getIntent().getDoubleExtra(EXTRA_TOTAL_REALISASI_MAIN, 0.0);
            String selectedYear = getIntent().getStringExtra(EXTRA_SELECTED_YEAR);

            tvTotalPajakTitleYear.setText("Ringkasan Total Pajak Daerah Tahun " + selectedYear);

            calculateAndDisplayTotals(allPajakDetails, totalRealisasiMain);
        } else {
            Toast.makeText(this, "Data total pajak tidak tersedia.", Toast.LENGTH_SHORT).show();
            tvTotalPajakTitleYear.setText("Ringkasan Total Pajak Daerah");
            tvTotalTarget.setText("Total Target: -");
            tvTotalRealisasi.setText("Total Realisasi: -");
            tvTotalPersentase.setText("Persentase: -");
        }
    }

    private void calculateAndDisplayTotals(List<PajakDetail> details, double totalRealisasiMain) {
        double calculatedTotalTarget = 0.0;

        if (details != null) {
            for (PajakDetail pajak : details) {
                calculatedTotalTarget += pajak.getTarget();
            }
        }

        double totalPersentase = 0.0;
        if (calculatedTotalTarget > 0) {
            totalPersentase = (totalRealisasiMain / calculatedTotalTarget) * 100;
        }

        tvTotalTarget.setText("Total Target: " + currencyFormatter.format(calculatedTotalTarget));
        tvTotalRealisasi.setText("Total Realisasi: " + currencyFormatter.format(totalRealisasiMain));
        tvTotalPersentase.setText("Persentase: " + String.format(Locale.getDefault(), "%.2f%%", totalPersentase));

        if (totalPersentase >= 100) {
            tvTotalPersentase.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else if (totalPersentase > 0) {
            tvTotalPersentase.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTotalPersentase.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
}