package com.example.visioncart1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private TextView btnFontNormal, btnFontLarge, btnFontExtraLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // --- SWITCHES ---
        Switch switchVoice    = findViewById(R.id.switchVoiceAssistant);
        Switch switchVibrate  = findViewById(R.id.switchVibration);
        Switch switchContrast = findViewById(R.id.switchHighContrast);

        // --- SPEECH RATE SLIDER ---
        SeekBar seekRate  = findViewById(R.id.seekSpeechRate);
        TextView tvRate   = findViewById(R.id.tvSpeechRate);
        String[] rateLabels = {"0.5x", "1x", "1.5x", "2x", "2.5x"};

        seekRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                tvRate.setText(rateLabels[p]);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        // --- FONT SIZE BUTTONS ---
        btnFontNormal     = findViewById(R.id.btnFontNormal);
        btnFontLarge      = findViewById(R.id.btnFontLarge);
        btnFontExtraLarge = findViewById(R.id.btnFontExtraLarge);

        btnFontNormal.setOnClickListener(v     -> setActiveFont(0));
        btnFontLarge.setOnClickListener(v      -> setActiveFont(1));
        btnFontExtraLarge.setOnClickListener(v -> setActiveFont(2));

        // --- BOTTOM NAV ---
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navScan).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class))
        );
        findViewById(R.id.navHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
        findViewById(R.id.navSettings).setOnClickListener(v -> { /* already here */ });
    }

    private void setActiveFont(int selected) {
        btnFontNormal.setBackgroundResource(
                selected == 0 ? R.drawable.bg_font_btn_active : R.drawable.bg_font_btn_inactive);
        btnFontNormal.setTextColor(getColor(
                selected == 0 ? R.color.bg_dark : R.color.text_gray));

        btnFontLarge.setBackgroundResource(
                selected == 1 ? R.drawable.bg_font_btn_active : R.drawable.bg_font_btn_inactive);
        btnFontLarge.setTextColor(getColor(
                selected == 1 ? R.color.bg_dark : R.color.text_gray));

        btnFontExtraLarge.setBackgroundResource(
                selected == 2 ? R.drawable.bg_font_btn_active : R.drawable.bg_font_btn_inactive);
        btnFontExtraLarge.setTextColor(getColor(
                selected == 2 ? R.color.bg_dark : R.color.text_gray));
    }
}