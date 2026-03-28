package com.example.visioncart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class ProductDetailActivity : BaseActivity() {

    // Data from intent
    private var brand: String? = null
    private var name: String? = null
    private var price: String? = null
    private var expires: String? = null
    private var category: String? = null
    private var ingredients: String? = null
    private var allergens: String? = null
    private var healthRating: String? = "Moderate"

    override fun onTtsReady() {
        startStructuredReadAloud()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Receive data
        val incoming = intent
        brand = incoming.getStringExtra("brand") ?: "Unknown"
        name = incoming.getStringExtra("name") ?: "Unknown"
        price = incoming.getStringExtra("price") ?: "₱00.00"
        expires = incoming.getStringExtra("expires") ?: "N/A"
        category = incoming.getStringExtra("category") ?: "Food"
        ingredients = incoming.getStringExtra("ingredients") ?: "Not shown"
        allergens = incoming.getStringExtra("allergens") ?: "None"
        healthRating = incoming.getStringExtra("healthRating") ?: "Moderate"

        displayProductInfo()

        findViewById<FrameLayout>(R.id.btnBack).setOnClickListener { 
            globalTts?.stop()
            finish() 
        }

        // Fix: Link Read Aloud button
        findViewById<LinearLayout>(R.id.btnReadAloud).setOnClickListener {
            startStructuredReadAloud()
        }

        // Navigation
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }
    }

    private fun displayProductInfo() {
        findViewById<TextView>(R.id.tvBrand).text = brand
        findViewById<TextView>(R.id.tvProductName).text = name
        findViewById<TextView>(R.id.tvPrice).text = price
        findViewById<TextView>(R.id.tvExpires).text = expires
        findViewById<TextView>(R.id.tvCategory).text = category
        findViewById<TextView>(R.id.tvIngredients).text = ingredients
        findViewById<TextView>(R.id.tvAllergens).text = allergens
        findViewById<TextView>(R.id.tvHealthRating)?.text = healthRating
    }

    private fun startStructuredReadAloud() {
        if (!isTtsReady) return
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return

        val intro = "Product: $name. "
        val ratingMsg = "The health rating is $healthRating. "
        val expireMsg = "This product expires on $expires. "
        val allergenMsg = if (allergens?.contains("None", true) == true) "No allergens detected." else "Warning: this contains $allergens."

        // Read segments
        globalTts?.setPitch(1.0f)
        globalTts?.speak(intro, TextToSpeech.QUEUE_FLUSH, null, "intro")
        
        // Priority alert check
        if (healthRating == "Unhealthy") globalTts?.setPitch(1.3f) else globalTts?.setPitch(1.0f)
        globalTts?.speak(ratingMsg, TextToSpeech.QUEUE_ADD, null, "rating")
        
        globalTts?.setPitch(1.0f)
        globalTts?.speak(expireMsg, TextToSpeech.QUEUE_ADD, null, "expires")
        
        if (!allergenMsg.contains("No allergens", true)) globalTts?.setPitch(1.5f) else globalTts?.setPitch(1.0f)
        globalTts?.speak(allergenMsg, TextToSpeech.QUEUE_ADD, null, "allergens")
        
        globalTts?.setPitch(1.0f)
    }
}
