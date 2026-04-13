package com.example.visioncart

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.visioncart.db.AppDatabase
import com.example.visioncart.model.ScannedProduct
import kotlinx.coroutines.flow.first 
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    private var emptyState: LinearLayout? = null
    private var filledState: LinearLayout? = null
    private var historyContainer: LinearLayout? = null
    private var tvItemCount: TextView? = null
    private var btnClear: LinearLayout? = null
    
    override fun onTtsReady() {
        lifecycleScope.launch {
            val count = database.productDao().getAllProducts().first().size
            speak("History Screen. You have $count items scanned recently.")
        }
    }

    private var isShoppingListActive = false
    private var btnToggleList: View? = null
    private var ivListIcon: ImageView? = null
    private var tvListLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Views
        emptyState = findViewById(R.id.emptyState)
        filledState = findViewById(R.id.filledState)
        historyContainer = findViewById(R.id.historyList)
        tvItemCount = findViewById(R.id.tvItemCount)
        btnClear = findViewById(R.id.btnClear)
        btnToggleList = findViewById(R.id.btnToggleShoppingList)
        ivListIcon = findViewById(R.id.ivListIcon)
        tvListLabel = findViewById(R.id.tvListLabel)

        val sampleItem = findViewById<RelativeLayout>(R.id.sampleHistoryItem)
        sampleItem?.visibility = View.GONE

        val btnScanFirst = findViewById<View>(R.id.btnScanFirst)
        setVocalButton(btnScanFirst, "Scan your first product")
        btnScanFirst?.setOnClickListener {
            vibrate()
            startActivity(Intent(this, ScanActivity::class.java))
        }

        setVocalButton(btnClear, "Clear all history")
        btnClear?.setOnClickListener {
            vibrateError()
            lifecycleScope.launch {
                database.productDao().clearHistory()
                speak("History cleared")
            }
        }

        btnToggleList?.setOnClickListener {
            toggleShoppingListFilter()
        }

        // Bottom nav
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navScan = findViewById<LinearLayout>(R.id.navScan)
        val navSettings = findViewById<LinearLayout>(R.id.navSettings)

        setVocalButton(navHome, "Home Screen")
        setVocalButton(navScan, "Scan Screen")
        setVocalButton(navSettings, "Settings Screen")

        navHome?.setOnClickListener { vibrate(); startActivity(Intent(this, MainActivity::class.java)); finish() }
        navScan?.setOnClickListener { vibrate(); startActivity(Intent(this, ScanActivity::class.java)) }
        navSettings?.setOnClickListener { vibrate(); startActivity(Intent(this, SettingsActivity::class.java)) }

        lifecycleScope.launch {
            database.productDao().getAllProducts().collect { list ->
                refreshUI(list)
            }
        }
    }

    private fun toggleShoppingListFilter() {
        isShoppingListActive = !isShoppingListActive
        vibrate(30)
        speak(if (isShoppingListActive) "Showing shopping list items only" else "Showing all history items")
        
        ivListIcon?.alpha = if (isShoppingListActive) 1.0f else 0.4f
        tvListLabel?.text = if (isShoppingListActive) "Shopping List" else "All Items"
        btnToggleList?.setBackgroundResource(if (isShoppingListActive) R.drawable.bg_theme_option_active else R.drawable.bg_dark_rounded)
        
        lifecycleScope.launch {
            val list = database.productDao().getAllProducts().first()
            refreshUI(list)
        }
    }

    private fun speakStructured(text: String, isWarning: Boolean = false) {
        globalTts?.setPitch(if (isWarning) 1.5f else 1.0f)
        globalTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "structured_audio")
    }

    private fun speakProduct(product: ScannedProduct) {
        val name = product.name ?: "Unknown product"
        val health = product.healthRating ?: "Unknown rating"
        val expires = product.expires ?: "No expiration date"
        val allergens = product.allergens ?: "No allergen information"

        speakStructured("Product name: $name")
        
        val healthText = "Health rating is $health"
        val isUnhealthy = health.contains("unhealthy", ignoreCase = true)
        globalTts?.setPitch(if (isUnhealthy) 1.5f else 1.0f)
        globalTts?.speak(healthText, TextToSpeech.QUEUE_ADD, null, "health")

        globalTts?.setPitch(1.0f)
        globalTts?.speak("Expires on $expires", TextToSpeech.QUEUE_ADD, null, "expires")

        val allergenText = if (allergens.lowercase().contains("none")) {
            "No allergens detected"
        } else {
            "ATTENTION: Allergen Warning. This product contains: $allergens. Please be careful."
        }
        val hasAllergens = !allergens.lowercase().contains("none")
        globalTts?.setPitch(if (hasAllergens) 1.6f else 1.0f)
        globalTts?.speak(allergenText, TextToSpeech.QUEUE_ADD, null, "allergens")
    }

    private fun refreshUI(list: List<ScannedProduct>) {
        val filteredList = if (isShoppingListActive) list.filter { it.isStarred } else list
        
        if (list.isEmpty()) {
            btnToggleList?.visibility = View.GONE
            btnClear?.visibility = View.GONE
        } else {
            btnToggleList?.visibility = View.VISIBLE
            btnClear?.visibility = View.VISIBLE
        }

        if (filteredList.isEmpty()) {
            emptyState?.visibility = View.VISIBLE
            filledState?.visibility = View.GONE
            
            val emptyMsg = if (isShoppingListActive) "No starred items yet" else "No products scanned yet"
            emptyState?.findViewById<TextView>(R.id.tvEmptyMsg)?.text = emptyMsg
        } else {
            emptyState?.visibility = View.GONE
            filledState?.visibility = View.VISIBLE

            val count = filteredList.size
            tvItemCount?.text = if (isShoppingListActive) "$count starred items in your list" else "$count products scanned recently"

            historyContainer?.removeAllViews()
            for (product in filteredList) {
                addHistoryItem(product)
            }
        }
    }

    private fun addHistoryItem(product: ScannedProduct) {
        val item = LayoutInflater.from(this).inflate(R.layout.item_history, historyContainer, false)

        val tvBrand = item.findViewById<TextView>(R.id.tvBrand)
        val tvName = item.findViewById<TextView>(R.id.tvProductName)
        val tvPrice = item.findViewById<TextView>(R.id.tvPrice)
        val tvTime = item.findViewById<TextView>(R.id.tvTime)
        val ivStarred = item.findViewById<ImageView>(R.id.ivItemStarred)
        val btnDetail = item.findViewById<FrameLayout>(R.id.btnDetail)
        val btnSpeak = item.findViewById<FrameLayout>(R.id.btnSpeak)

        tvBrand.text = product.brand
        tvName.text = product.name
        tvPrice.text = product.userPrice ?: product.price
        tvTime.text = product.time
        ivStarred?.visibility = if (product.isStarred) View.VISIBLE else View.GONE

        btnDetail.setOnClickListener {
            val intent = Intent(this, ProductDetailActivity::class.java).apply {
                putExtra("productId", product.id)
                putExtra("brand", product.brand)
                putExtra("name", product.name)
                putExtra("price", product.price)
                putExtra("userPrice", product.userPrice)
                putExtra("isStarred", product.isStarred)
                putExtra("time", product.time)
                putExtra("expires", product.expires)
                putExtra("weight", product.weight)
                putExtra("barcode", product.barcode)
                putExtra("category", product.category)
                putExtra("ingredients", product.ingredients)
                putExtra("allergens", product.allergens)
                putExtra("healthRating", product.healthRating)
            }
            startActivity(intent)
        }

        btnSpeak.setOnClickListener {
            speakProduct(product)
        }

        historyContainer?.addView(item)
    }
}
