package com.example.visioncart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import com.example.visioncart.api.GeminiService
import com.example.visioncart.db.AppDatabase
import kotlinx.coroutines.launch

class ProductDetailActivity : BaseActivity() {

    private var productId: Long = -1L
    private var isStarred: Boolean = false
    private var userPrice: String? = null
    
    // Data from intent
    private var brand: String? = null
    private var name: String? = null
    private var price: String? = null
    private var expires: String? = null
    private var category: String? = null
    private var ingredients: String? = null
    private var allergens: String? = null
    private var healthRating: String? = "Moderate"
    private var weight: String? = "N/A"
    private var barcode: String? = "N/A"

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val geminiService = GeminiService()

    override fun onTtsReady() {
        startStructuredReadAloud()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Receive data
        val incoming = intent
        productId = incoming.getLongExtra("productId", -1L)
        brand = incoming.getStringExtra("brand") ?: "Unknown"
        name = incoming.getStringExtra("name") ?: "Unknown"
        price = incoming.getStringExtra("price") ?: "₱00.00"
        expires = incoming.getStringExtra("expires") ?: "N/A"
        category = incoming.getStringExtra("category") ?: "Food"
        ingredients = incoming.getStringExtra("ingredients") ?: "Not shown"
        allergens = incoming.getStringExtra("allergens") ?: "None"
        healthRating = incoming.getStringExtra("healthRating") ?: "Moderate"
        weight = incoming.getStringExtra("weight") ?: "N/A"
        barcode = incoming.getStringExtra("barcode") ?: "N/A"
        isStarred = incoming.getBooleanExtra("isStarred", false)
        userPrice = incoming.getStringExtra("userPrice")

        displayProductInfo()

        findViewById<FrameLayout>(R.id.btnBack).setOnClickListener { 
            globalTts?.stop()
            finish() 
        }

        findViewById<LinearLayout>(R.id.btnReadAloud).setOnClickListener {
            startStructuredReadAloud()
        }

        findViewById<FrameLayout>(R.id.btnStar).setOnClickListener {
            toggleStar()
        }

        findViewById<LinearLayout>(R.id.btnEditPrice).setOnClickListener {
            showEditPriceDialog()
        }

        findViewById<FrameLayout>(R.id.btnAskGemini).setOnClickListener {
            performAiQA()
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
        findViewById<TextView>(R.id.tvPrice).text = userPrice ?: price
        findViewById<TextView>(R.id.tvExpires).text = expires
        findViewById<TextView>(R.id.tvCategory).text = category
        findViewById<TextView>(R.id.tvIngredients).text = ingredients
        findViewById<TextView>(R.id.tvAllergens).text = allergens
        findViewById<TextView>(R.id.tvHealthRating)?.text = healthRating
        findViewById<TextView>(R.id.tvWeight)?.text = weight
        findViewById<TextView>(R.id.tvBarcode)?.text = barcode
        updateStarUI()
    }

    private fun updateStarUI() {
        val ivStar = findViewById<ImageView>(R.id.ivStar)
        ivStar.alpha = if (isStarred) 1.0f else 0.4f
        
        val colorAttr = if (isStarred) R.attr.appPrimaryColor else R.attr.appTextSecondary
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(colorAttr, typedValue, true)
        ivStar.setColorFilter(typedValue.data)
    }

    private fun toggleStar() {
        isStarred = !isStarred
        vibrate(80)
        updateStarUI()
        speak(if (isStarred) "Item added to shopping list" else "Item removed from shopping list")
        lifecycleScope.launch {
            database.productDao().updateStarredStatus(productId, isStarred)
        }
    }

    private fun showEditPriceDialog() {
        val et = EditText(this)
        et.setText(userPrice ?: price)
        AlertDialog.Builder(this)
            .setTitle("Enter Price")
            .setView(et)
            .setPositiveButton("Save") { _, _ ->
                val newPrice = et.text.toString()
                userPrice = newPrice
                findViewById<TextView>(R.id.tvPrice).text = newPrice
                speak("Price updated to $newPrice")
                lifecycleScope.launch {
                    database.productDao().updatePrice(productId, newPrice)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performAiQA() {
        val question = findViewById<EditText>(R.id.etQuestion).text.toString()
        if (question.isEmpty()) return

        val tvResponse = findViewById<TextView>(R.id.tvAiResponse)
        tvResponse.text = "Gemini is thinking..."
        speak("Asking Gemini...")

        val productContext = "Brand: $brand, Name: $name, Ingredients: $ingredients, Allergens: $allergens, Category: $category"
        
        lifecycleScope.launch {
            val answer = geminiService.askNutritionalQuestion(question, productContext)
            tvResponse.text = answer ?: "I couldn't find an answer."
            speak(answer)
        }
    }

    override fun handleGlobalVoiceCommand(command: String) {
        val clean = command.lowercase().trim()
        when {
            clean.contains("ingredients") -> speak("Ingredients for $name are: $ingredients")
            clean.contains("calories") -> speak("Checking nutrition...") 
            clean.contains("star") || clean.contains("save") -> toggleStar()
            else -> super.handleGlobalVoiceCommand(command)
        }
    }

    private fun startStructuredReadAloud() {
        if (!isTtsReady) return
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return

        val allergenFound = allergens != null && !allergens!!.lowercase().contains("none")
        val allergenMsg = if (allergenFound) "ATTENTION: Allergen Warning. This product contains: $allergens. Please be very careful." else "No allergens detected."
        
        // Truncate long numeric values for speech (display stays full)
        val spokenName = truncateNumbersForSpeech(name ?: "")
        val spokenBrand = truncateNumbersForSpeech(brand ?: "")
        val spokenExpires = truncateNumbersForSpeech(expires ?: "N/A")
        val spokenBarcode = truncateNumbersForSpeech(barcode ?: "N/A")
        
        val intro = "Product: $spokenName by $spokenBrand. "
        val ratingMsg = "The health rating is $healthRating. "
        val expireMsg = "This product expires on $spokenExpires. "

        // Show card UI
        val card = findViewById<RelativeLayout>(R.id.voiceReadingCard)
        card.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvVoiceReadingText).text = intro + ratingMsg + expireMsg + allergenMsg
        findViewById<TextView>(R.id.btnCloseReading).setOnClickListener {
            card.visibility = View.GONE
            globalTts?.stop()
        }

        // 1. Safety First: Read allergens with higher pitch if found
        if (allergenFound) {
            globalTts?.setPitch(1.6f)
            globalTts?.speak(allergenMsg, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "allergens")
        } else {
            globalTts?.setPitch(1.0f)
            globalTts?.speak(intro, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "intro")
        }
        
        // 2. Normal Details
        globalTts?.setPitch(1.0f)
        if (allergenFound) globalTts?.speak(intro, android.speech.tts.TextToSpeech.QUEUE_ADD, null, "intro_delayed") 
        
        globalTts?.speak(ratingMsg, android.speech.tts.TextToSpeech.QUEUE_ADD, null, "rating")
        globalTts?.speak(expireMsg, android.speech.tts.TextToSpeech.QUEUE_ADD, null, "expires")
        
        if (!allergenFound) globalTts?.speak(allergenMsg, android.speech.tts.TextToSpeech.QUEUE_ADD, null, "no_allergens")
        
        globalTts?.setPitch(1.0f)
    }

    /**
     * Truncates any numeric sequence longer than 4 digits to just the first 4 digits.
     * Full values are still displayed in the UI; only spoken output is capped.
     */
    private fun truncateNumbersForSpeech(input: String): String {
        return input.replace(Regex("\\d{5,}")) { match ->
            match.value.take(4)
        }
    }
}
