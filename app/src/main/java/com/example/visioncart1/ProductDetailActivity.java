package com.example.visioncart1;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean ttsReady = false;

    private LinearLayout btnReadAloud;
    private TextView tvReadAloudTitle, tvReadAloudSub;
    private RelativeLayout voiceReadingCard;
    private TextView tvVoiceReadingText;

    private final String productText =
            "Product: Tomato Soup by Campbell's. " +
                    "Price: 85 pesos. " +
                    "Expiration date: December 2025. " +
                    "Weight: 305 grams. " +
                    "Category: Canned Goods. " +
                    "Allergens: None. " +
                    "Ingredients: Tomato Puree, Water, Tomato Paste, " +
                    "High Fructose Corn Syrup, Salt, Potassium Chloride, " +
                    "Natural Flavoring, Citric Acid, Ascorbic Acid Vitamin C. " +
                    "Nutritional facts per serving: " +
                    "Calories: 90 kcal. Total fat: 0g. " +
                    "Sodium: 480mg. Total carbohydrates: 20g. " +
                    "Sugars: 12g. Protein: 2g.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        tts = new TextToSpeech(this, this);

        btnReadAloud     = findViewById(R.id.btnReadAloud);
        tvReadAloudTitle = findViewById(R.id.tvReadAloudTitle);
        tvReadAloudSub   = findViewById(R.id.tvReadAloudSub);
        voiceReadingCard = findViewById(R.id.voiceReadingCard);
        tvVoiceReadingText = findViewById(R.id.tvVoiceReadingText);

        // ── Receive data from History or Scan ──
        Intent incoming = getIntent();
        String brand       = incoming.getStringExtra("brand");
        String name        = incoming.getStringExtra("name");
        String price       = incoming.getStringExtra("price");
        String expires     = incoming.getStringExtra("expires");
        String weight      = incoming.getStringExtra("weight");
        String barcode     = incoming.getStringExtra("barcode");
        String category    = incoming.getStringExtra("category");
        String ingredients = incoming.getStringExtra("ingredients");
        String allergens   = incoming.getStringExtra("allergens");

        // ── Apply to views if data exists ──
        if (brand != null) {
            TextView tvBrand = findViewById(R.id.tvBrand);
            if (tvBrand != null) tvBrand.setText(brand);
        }
        if (name != null) {
            TextView tvName = findViewById(R.id.tvProductName);
            if (tvName != null) tvName.setText(name);
        }
        if (price != null) {
            TextView tvPrice = findViewById(R.id.tvPrice);
            if (tvPrice != null) tvPrice.setText(price);
        }
        if (expires != null) {
            TextView tvExpires = findViewById(R.id.tvExpires);
            if (tvExpires != null) tvExpires.setText(expires);
        }
        if (weight != null) {
            TextView tvWeight = findViewById(R.id.tvWeight);
            if (tvWeight != null) tvWeight.setText(weight);
        }
        if (barcode != null) {
            TextView tvBarcode = findViewById(R.id.tvBarcode);
            if (tvBarcode != null) tvBarcode.setText(barcode);
        }
        if (category != null) {
            TextView tvCategory = findViewById(R.id.tvCategory);
            if (tvCategory != null) tvCategory.setText(category);
        }
        if (ingredients != null) {
            TextView tvIngredients = findViewById(R.id.tvIngredients);
            if (tvIngredients != null) tvIngredients.setText(ingredients);
        }
        if (allergens != null) {
            TextView tvAllergens = findViewById(R.id.tvAllergens);
            if (tvAllergens != null) tvAllergens.setText(allergens);
        }

        // Back
        FrameLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Scan again
        FrameLayout btnScanAgain = findViewById(R.id.btnScanAgain);
        btnScanAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
            finish();
        });

        // Read Aloud
        btnReadAloud.setOnClickListener(v -> readAloud());

        // Close voice reading card
        findViewById(R.id.btnCloseReading).setOnClickListener(v -> {
            stopReading();
        });

        // Nutrition expand/collapse
        LinearLayout nutritionContent = findViewById(R.id.nutritionContent);
        TextView tvArrow = findViewById(R.id.tvNutritionArrow);
        findViewById(R.id.nutritionHeader).setOnClickListener(v -> {
            if (nutritionContent.getVisibility() == View.VISIBLE) {
                nutritionContent.setVisibility(View.GONE);
                tvArrow.setText("∨");
            } else {
                nutritionContent.setVisibility(View.VISIBLE);
                tvArrow.setText("∧");
            }
        });

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
            finish();
        });
        findViewById(R.id.navHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
        findViewById(R.id.navSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }

    private void readAloud() {
        // Switch button to "READING ALOUD..."
        tvReadAloudTitle.setText("READING ALOUD...");
        tvReadAloudSub.setText("Hearing product details now");
        btnReadAloud.setBackgroundResource(R.drawable.bg_read_aloud_btn);

        // Show voice card
        tvVoiceReadingText.setText(productText);
        showFadeIn(voiceReadingCard);

        // TTS
        if (ttsReady) {
            tts.speak(productText, TextToSpeech.QUEUE_FLUSH, null, "readAloud");
        }
    }

    private void stopReading() {
        if (tts != null && tts.isSpeaking()) tts.stop();
        tvReadAloudTitle.setText("READ ALOUD");
        tvReadAloudSub.setText("Hear all product information");
        voiceReadingCard.setVisibility(View.GONE);
    }

    private void showFadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(300);
        view.startAnimation(fade);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
            ttsReady = true;
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
