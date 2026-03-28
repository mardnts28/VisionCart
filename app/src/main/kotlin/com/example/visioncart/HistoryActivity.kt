package com.example.visioncart

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visioncart.R
import java.util.ArrayList
import java.util.Locale

class HistoryActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    data class ScannedProduct(
        var brand: String?,
        var name: String?,
        var price: String?,
        var time: String?,
        var expires: String?,
        var weight: String?,
        var barcode: String?,
        var category: String?,
        var ingredients: String?,
        var allergens: String?,
        var healthRating: String? = "Moderate"
    )

    companion object {
        @JvmStatic
        val historyList: ArrayList<ScannedProduct> = ArrayList()
    }

    private var emptyState: LinearLayout? = null
    private var filledState: LinearLayout? = null
    private var historyContainer: LinearLayout? = null
    private var tvItemCount: TextView? = null
    private var btnClear: LinearLayout? = null
    
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize TTS
        tts = TextToSpeech(this, this)

        // Initialize Speech Recognizer
        setupSpeechRecognizer()

        // Views
        emptyState = findViewById(R.id.emptyState)
        filledState = findViewById(R.id.filledState)
        historyContainer = findViewById(R.id.historyList)
        tvItemCount = findViewById(R.id.tvItemCount)
        btnClear = findViewById(R.id.btnClear)

        val sampleItem = findViewById<RelativeLayout>(R.id.sampleHistoryItem)
        sampleItem?.visibility = View.GONE

        findViewById<LinearLayout>(R.id.btnScanFirst).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        btnClear?.setOnClickListener {
            historyList.clear()
            refreshUI()
        }

        // Bottom nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        refreshUI()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) { isListening = false }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        handleVoiceCommand(matches[0].lowercase())
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            speechRecognizer?.startListening(intent)
        }
    }

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("scan") -> startActivity(Intent(this, ScanActivity::class.java))
            command.contains("clear") -> {
                historyList.clear()
                refreshUI()
                speakStructured("History cleared")
            }
            command.contains("repeat") -> {
                if (historyList.isNotEmpty()) speakProduct(historyList.last())
            }
            command.contains("home") -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        setupSpeechRecognizer()
    }

    private fun speakStructured(text: String, isWarning: Boolean = false) {
        tts?.setPitch(if (isWarning) 1.5f else 1.0f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "structured_audio")
    }

    private fun speakProduct(product: ScannedProduct) {
        val name = product.name ?: "Unknown product"
        val health = product.healthRating ?: "Unknown rating"
        val expires = product.expires ?: "No expiration date"
        val allergens = product.allergens ?: "No allergen information"

        speakStructured("Product name: $name")
        
        val healthText = "Health rating is $health"
        val isUnhealthy = health.contains("unhealthy", ignoreCase = true)
        tts?.setPitch(if (isUnhealthy) 1.5f else 1.0f)
        tts?.speak(healthText, TextToSpeech.QUEUE_ADD, null, "health")

        tts?.setPitch(1.0f)
        tts?.speak("Expires on $expires", TextToSpeech.QUEUE_ADD, null, "expires")

        val allergenText = if (allergens.lowercase().contains("none")) {
            "No allergens detected"
        } else {
            "Warning. Contains allergens: $allergens"
        }
        val hasAllergens = !allergens.lowercase().contains("none")
        tts?.setPitch(if (hasAllergens) 1.6f else 1.0f)
        tts?.speak(allergenText, TextToSpeech.QUEUE_ADD, null, "allergens")
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        if (historyList.isEmpty()) {
            emptyState?.visibility = View.VISIBLE
            filledState?.visibility = View.GONE
            btnClear?.visibility = View.GONE
        } else {
            emptyState?.visibility = View.GONE
            filledState?.visibility = View.VISIBLE
            btnClear?.visibility = View.VISIBLE

            val count = historyList.size
            tvItemCount?.text = "$count products scanned recently"

            historyContainer?.removeAllViews()
            for (product in historyList) {
                addHistoryItem(product)
            }
        }
    }

    private fun addHistoryItem(product: ScannedProduct) {
        val item = LayoutInflater.from(this)
            .inflate(R.layout.item_history, historyContainer, false)

        val tvBrand = item.findViewById<TextView>(R.id.tvBrand)
        val tvName = item.findViewById<TextView>(R.id.tvProductName)
        val tvPrice = item.findViewById<TextView>(R.id.tvPrice)
        val tvTime = item.findViewById<TextView>(R.id.tvTime)
        val btnDetail = item.findViewById<FrameLayout>(R.id.btnDetail)
        val btnSpeak = item.findViewById<FrameLayout>(R.id.btnSpeak)

        tvBrand.text = product.brand
        tvName.text = product.name
        tvPrice.text = product.price
        tvTime.text = product.time

        btnDetail.setOnClickListener {
            val intent = Intent(this, ProductDetailActivity::class.java).apply {
                putExtra("brand", product.brand)
                putExtra("name", product.name)
                putExtra("price", product.price)
                putExtra("time", product.time)
                putExtra("expires", product.expires)
                putExtra("weight", product.weight)
                putExtra("barcode", product.barcode)
                putExtra("category", product.category)
            }
            startActivity(intent)
        }

        btnSpeak.setOnClickListener {
            speakProduct(product)
        }

        historyContainer?.addView(item)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
