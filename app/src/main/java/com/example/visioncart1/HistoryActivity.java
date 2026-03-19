package com.example.visioncart1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.widget.FrameLayout;

public class HistoryActivity extends AppCompatActivity {

    // Simple data model for a scanned product
    public static class ScannedProduct {
        public String brand;
        public String name;
        public String price;
        public String time;
        public String expires;
        public String weight;
        public String barcode;
        public String category;
        public String ingredients;
        public String allergens;

        public ScannedProduct(String brand, String name, String price, String time,
                              String expires, String weight, String barcode,
                              String category, String ingredients, String allergens) {
            this.brand       = brand;
            this.name        = name;
            this.price       = price;
            this.time        = time;
            this.expires     = expires;
            this.weight      = weight;
            this.barcode     = barcode;
            this.category    = category;
            this.ingredients = ingredients;
            this.allergens   = allergens;
        }
    }

    // Static list so history persists across activity navigation
    public static ArrayList<ScannedProduct> historyList = new ArrayList<>();

    private LinearLayout emptyState;
    private LinearLayout filledState;
    private LinearLayout historyContainer;
    private TextView tvItemCount;
    private LinearLayout btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Views
        emptyState      = findViewById(R.id.emptyState);
        filledState     = findViewById(R.id.filledState);
        historyContainer = findViewById(R.id.historyList);
        tvItemCount     = findViewById(R.id.tvItemCount);
        btnClear        = findViewById(R.id.btnClear);

        // Remove the sample static item
        RelativeLayout sampleItem = findViewById(R.id.sampleHistoryItem);
        if (sampleItem != null) sampleItem.setVisibility(View.GONE);

        // Scan First Product button
        LinearLayout btnScanFirst = findViewById(R.id.btnScanFirst);
        btnScanFirst.setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class))
        );

        // Clear button
        btnClear.setOnClickListener(v -> {
            historyList.clear();
            refreshUI();
        });

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navScan).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class))
        );
        findViewById(R.id.navHistory).setOnClickListener(v -> { /* already here */ });
        findViewById(R.id.navSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        if (historyList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            filledState.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            filledState.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);

            int count = historyList.size();
            tvItemCount.setText(count + " product" + (count > 1 ? "s" : "") + " scanned recently");

            // Rebuild the list
            historyContainer.removeAllViews();
            for (ScannedProduct product : historyList) {
                addHistoryItem(product);
            }
        }
    }

    private void addHistoryItem(ScannedProduct product) {
        View item = LayoutInflater.from(this)
                .inflate(R.layout.item_history, historyContainer, false);

        TextView tvBrand   = item.findViewById(R.id.tvBrand);
        TextView tvName    = item.findViewById(R.id.tvProductName);
        TextView tvPrice   = item.findViewById(R.id.tvPrice);
        TextView tvTime    = item.findViewById(R.id.tvTime);
        FrameLayout btnDetail = item.findViewById(R.id.btnDetail);
        FrameLayout btnSpeak  = item.findViewById(R.id.btnSpeak);

        tvBrand.setText(product.brand);
        tvName.setText(product.name);
        tvPrice.setText(product.price);
        tvTime.setText(product.time);

        // ── Arrow button → go to Product Details ──
        btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, ProductDetailActivity.class);
            intent.putExtra("brand",    product.brand);
            intent.putExtra("name",     product.name);
            intent.putExtra("price",    product.price);
            intent.putExtra("time",     product.time);
            intent.putExtra("expires",  product.expires);
            intent.putExtra("weight",   product.weight);
            intent.putExtra("barcode",  product.barcode);
            intent.putExtra("category", product.category);
            startActivity(intent);
        });

        // ── Speaker button → read aloud ──
        btnSpeak.setOnClickListener(v -> {
            // TTS will read the product info
            android.speech.tts.TextToSpeech tts = new android.speech.tts.TextToSpeech(
                    this, status -> {});
            String text = "Product: " + product.name + " by " + product.brand +
                    ". Price: " + product.price;
            tts.speak(text,
                    android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
        });

        historyContainer.addView(item);
    }
}