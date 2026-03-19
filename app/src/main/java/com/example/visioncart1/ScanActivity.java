package com.example.visioncart1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScanActivity extends AppCompatActivity {

    private boolean isScanning = false;

    // Viewfinder corners
    private ImageView cornerIdleTL, cornerIdleTR, cornerIdleBL, cornerIdleBR;
    private ImageView cornerGreenTL, cornerGreenTR, cornerGreenBL, cornerGreenBR;

    // Center hints
    private LinearLayout centerIdleHint;
    private TextView tvDetecting;

    // Info + button
    private TextView tvInfoCard, tvScanBtnText;
    private LinearLayout btnScanNow;

    // Voice card
    private RelativeLayout voiceAssistantCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Corners
        cornerIdleTL  = findViewById(R.id.cornerIdleTL);
        cornerIdleTR  = findViewById(R.id.cornerIdleTR);
        cornerIdleBL  = findViewById(R.id.cornerIdleBL);
        cornerIdleBR  = findViewById(R.id.cornerIdleBR);
        cornerGreenTL = findViewById(R.id.cornerGreenTL);
        cornerGreenTR = findViewById(R.id.cornerGreenTR);
        cornerGreenBL = findViewById(R.id.cornerGreenBL);
        cornerGreenBR = findViewById(R.id.cornerGreenBR);

        centerIdleHint = findViewById(R.id.centerIdleHint);
        tvDetecting    = findViewById(R.id.tvDetecting);
        tvInfoCard     = findViewById(R.id.tvInfoCard);
        tvScanBtnText  = findViewById(R.id.tvScanBtnText);
        btnScanNow     = findViewById(R.id.btnScanNow);
        voiceAssistantCard = findViewById(R.id.voiceAssistantCard);

        // Header buttons
        FrameLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Scan Now button
        btnScanNow.setOnClickListener(v -> {
            if (!isScanning) startScanning();
        });

        // Close voice card
        findViewById(R.id.btnCloseVoice).setOnClickListener(v ->
                voiceAssistantCard.setVisibility(View.GONE)
        );

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navScan).setOnClickListener(v -> { });
        findViewById(R.id.navHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
        findViewById(R.id.navSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }

    private void startScanning() {
        isScanning = true;

        // Switch to green corners
        cornerIdleTL.setVisibility(View.GONE);
        cornerIdleTR.setVisibility(View.GONE);
        cornerIdleBL.setVisibility(View.GONE);
        cornerIdleBR.setVisibility(View.GONE);
        cornerGreenTL.setVisibility(View.VISIBLE);
        cornerGreenTR.setVisibility(View.VISIBLE);
        cornerGreenBL.setVisibility(View.VISIBLE);
        cornerGreenBR.setVisibility(View.VISIBLE);

        // Show detecting label
        centerIdleHint.setVisibility(View.GONE);
        tvDetecting.setVisibility(View.VISIBLE);

        // Update info card + button
        tvInfoCard.setText("Barcode detected!");
        tvScanBtnText.setText("DETECTED!");
        btnScanNow.setBackgroundResource(R.drawable.bg_detected_button);

        // Show voice assistant card
        showFadeIn(voiceAssistantCard);

        // After 1.5 seconds → go to Product Details
        new Handler().postDelayed(() -> {
            // Save to history
            HistoryActivity.historyList.add(0,
                    new HistoryActivity.ScannedProduct(
                            "Kellogg's",
                            "Original Corn Flakes",
                            "₱ 132.00",
                            "Just now",
                            "September 2026",
                            "370g",
                            "038000199271",
                            "Breakfast",
                            "Corn, Sugar, Salt, Niacinamide, Reduced Iron, Zinc, " +
                                    "Thiamin Mononitrate, Riboflavin, Folic Acid.",
                            "Contains Wheat. Made in a facility that processes Tree Nuts."
                    )
            );

            // Go to product detail
            Intent intent = new Intent(ScanActivity.this, ProductDetailActivity.class);
            startActivity(intent);
        }, 1500);
    }

    private void showFadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(300);
        view.startAnimation(fade);
    }
}