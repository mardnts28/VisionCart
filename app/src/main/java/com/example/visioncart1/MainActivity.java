package com.example.visioncart1;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout voiceAssistantCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        LinearLayout btnScanProduct = findViewById(R.id.btnScanProduct);
        LinearLayout btnHistory     = findViewById(R.id.btnHistory);
        LinearLayout btnAccessibility = findViewById(R.id.btnAccessibility);
        FrameLayout  btnMic         = findViewById(R.id.btnMic);
        voiceAssistantCard          = findViewById(R.id.voiceAssistantCard);
        TextView btnCloseVoice      = findViewById(R.id.btnCloseVoice);

        LinearLayout navHome     = findViewById(R.id.navHome);
        LinearLayout navScan     = findViewById(R.id.navScan);
        LinearLayout navHistory  = findViewById(R.id.navHistory);
        LinearLayout navSettings = findViewById(R.id.navSettings);

        // SCAN PRODUCT
        if (btnScanProduct != null) {
            btnScanProduct.setOnClickListener(v ->
                    startActivity(new Intent(this, ScanActivity.class))
            );
        }

        // HISTORY
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, HistoryActivity.class))
            );
        }

        // ACCESSIBILITY → Settings
        if (btnAccessibility != null) {
            btnAccessibility.setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class))
            );
        }

        // MIC BUTTON
        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                showVoiceCard();
                activateVoice();
            });
        }

        // CLOSE VOICE CARD
        if (btnCloseVoice != null) {
            btnCloseVoice.setOnClickListener(v -> hideVoiceCard());
        }

        // BOTTOM NAV
        if (navHome != null) {
            navHome.setOnClickListener(v -> { /* already home */ });
        }
        if (navScan != null) {
            navScan.setOnClickListener(v ->
                    startActivity(new Intent(this, ScanActivity.class))
            );
        }
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, HistoryActivity.class))
            );
        }
        if (navSettings != null) {
            navSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class))
            );
        }
    }

    private void showVoiceCard() {
        if (voiceAssistantCard != null) {
            voiceAssistantCard.setVisibility(View.VISIBLE);
            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(300);
            voiceAssistantCard.startAnimation(fade);
        }
    }

    private void hideVoiceCard() {
        if (voiceAssistantCard != null) {
            voiceAssistantCard.setVisibility(View.GONE);
        }
    }

    private void activateVoice() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Voice assistant is ready!");
            startActivityForResult(intent, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}