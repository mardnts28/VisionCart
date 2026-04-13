package com.example.visioncart

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

open class BaseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    protected var globalTts: TextToSpeech? = null
    protected var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isWaitingForCommand = false
    private var toneGenerator: ToneGenerator? = null
    
    // Voice Assistant Overlay Views
    private var voiceSearchOverlay: View? = null
    private var tvVoiceStatus: TextView? = null
    private var tvVoiceCommand: TextView? = null
    private var ivVoiceWave: ImageView? = null

    // Real-time Text Feedback
    protected var onVoiceResultListener: ((String) -> Unit)? = null

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        val fontScale = when(prefs.getString("font_size", "Large")) {
            "Normal" -> 1.0f
            "Large" -> 1.3f
            "ExtraLarge" -> 1.6f
            else -> 1.3f
        }
        val config = newBase.resources.configuration
        config.fontScale = fontScale
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyCustomTheme()
        super.onCreate(savedInstanceState)
        globalTts = TextToSpeech(this, this)
        setupTtsListener()
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        injectVoiceOverlay()
        setupGlobalSpeechRecognizer()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        injectVoiceOverlay()
        setupGlobalSpeechRecognizer()
    }

    private fun injectVoiceOverlay() {
        val content = findViewById<ViewGroup>(android.R.id.content)
        if (content.findViewById<View>(R.id.voiceSearchOverlay) != null) return
        
        val overlay = layoutInflater.inflate(R.layout.layout_voice_assistant, content, false)
        content.addView(overlay)
        
        voiceSearchOverlay = overlay.findViewById(R.id.voiceSearchOverlay)
        tvVoiceStatus = overlay.findViewById(R.id.tvVoiceStatus)
        tvVoiceCommand = overlay.findViewById(R.id.tvVoiceCommand)
        ivVoiceWave = overlay.findViewById(R.id.ivVoiceWave)
        
        overlay.findViewById<View>(R.id.btnCancelVoice)?.setOnClickListener {
            hideVoiceOverlay()
            stopListening()
            isWaitingForCommand = false
            startListeningInternal() // Go back to wake-word mode
        }
    }

    private fun setupTtsListener() {
        globalTts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                runOnUiThread { stopListening() }
            }
            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    if (utteranceId == "wake_response") {
                        isWaitingForCommand = true
                        startListeningInternal()
                    } else {
                        isWaitingForCommand = false
                        startListeningInternal()
                    }
                }
            }
            override fun onError(utteranceId: String?) {
                runOnUiThread { startListeningInternal() }
            }
        })
    }

    private fun applyCustomTheme() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        val theme = prefs.getString("color_scheme", "YellowBlack")
        when(theme) {
            "WhiteBlue" -> setTheme(R.style.Theme_VisionCart_WhiteBlue)
            "BlackWhite" -> setTheme(R.style.Theme_VisionCart_BlackWhite)
            else -> setTheme(R.style.Theme_VisionCart_YellowBlack)
        }
    }

    protected fun setupGlobalSpeechRecognizer() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            
            val speechListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { 
                    isListening = true 
                    if (isWaitingForCommand) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                        showVoiceOverlay("Listening...", "Say a command")
                    }
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    if (isWaitingForCommand) {
                        val scale = 1.0f + (rmsdB / 10f).coerceIn(0f, 0.6f)
                        ivVoiceWave?.scaleX = scale
                        ivVoiceWave?.scaleY = scale
                    }
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { 
                    isListening = false
                }
                override fun onError(error: Int) { 
                    isListening = false
                    if (isWaitingForCommand) {
                        hideVoiceOverlay()
                        isWaitingForCommand = false
                    }
                    if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        startListeningInternal()
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val result = matches[0].lowercase()
                        if (isWaitingForCommand) {
                            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                            runOnUiThread {
                                tvVoiceCommand?.text = result
                            }
                            onVoiceResultListener?.invoke(result)
                            handleGlobalVoiceCommand(result)
                            isWaitingForCommand = false
                            hideVoiceOverlay()
                        } else {
                            if (result.contains("vision cart") || result.contains("visioncart")) {
                                val commandPart = result.replace("vision cart", "").replace("visioncart", "").trim()
                                if (commandPart.isNotEmpty()) {
                                    onVoiceResultListener?.invoke(commandPart)
                                    handleGlobalVoiceCommand(commandPart)
                                } else {
                                    triggerWakeWord()
                                }
                            } else {
                                startListeningInternal()
                            }
                        }
                    } else {
                        startListeningInternal()
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partial = matches[0]
                        if (isWaitingForCommand) {
                            runOnUiThread {
                                tvVoiceCommand?.text = partial
                            }
                        }
                        onVoiceResultListener?.invoke(partial)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
            speechRecognizer?.setRecognitionListener(speechListener)
            startListeningInternal()
        }
    }

    private fun startListeningInternal() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerWakeWord() {
        vibrate(100)
        speak("How can I help you?", true, "wake_response")
    }

    fun startListeningForCommand() {
        if (isTtsReady) {
            triggerWakeWord()
        }
    }

    open fun handleGlobalVoiceCommand(command: String) {
        val clean = command.lowercase().trim()
        when {
            clean.contains("scan") || clean.contains("camera") -> {
                speak("Launching scanner")
                val intent = Intent(this, ScanActivity::class.java)
                intent.putExtra("auto_scan", true)
                startActivity(intent)
            }
            clean.contains("history") || clean.contains("scanned") -> {
                speak("Opening your history")
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            clean.contains("home") || clean.contains("main") -> {
                speak("Going to home screen")
                if (this !is MainActivity) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            clean.contains("settings") || clean.contains("accessibility") || clean.contains("config") -> {
                speak("Opening settings")
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            clean.contains("theme") || clean.contains("color") || clean.contains("scheme") -> {
                toggleTheme()
            }
            clean.contains("stop") || clean.contains("exit") || clean.contains("close") -> {
                globalTts?.stop()
                hideVoiceOverlay()
            }
            else -> {
                speak("I heard $command. You can say scan, history, or change theme.")
            }
        }
    }

    protected fun toggleTheme() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        val current = prefs.getString("color_scheme", "YellowBlack")
        val next = when(current) {
            "YellowBlack" -> "WhiteBlue"
            "WhiteBlue" -> "BlackWhite"
            else -> "YellowBlack"
        }
        prefs.edit().putString("color_scheme", next).apply()
        speak("Applying $next theme")
        vibrateSuccess()
        recreate()
    }

    private fun showVoiceOverlay(status: String, commandText: String) {
        voiceSearchOverlay?.visibility = View.VISIBLE
        tvVoiceStatus?.text = status
        tvVoiceCommand?.text = commandText
    }

    private fun hideVoiceOverlay() {
        voiceSearchOverlay?.visibility = View.GONE
    }

    private fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        isListening = false
    }

    override fun onPause() {
        super.onPause()
        globalTts?.stop()
        stopListening()
    }

    override fun onResume() {
        super.onResume()
        if (isTtsReady) startListeningInternal()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            globalTts?.language = Locale.US
            val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
            globalTts?.setSpeechRate(prefs.getFloat("speech_rate", 1.0f))
            isTtsReady = true
            onTtsReady()
            startListeningInternal()
        }
    }

    open fun onTtsReady() {}

    fun speak(text: String?, waitForCommand: Boolean = false, utteranceId: String = "global_audio") {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return
        
        if (isTtsReady && text != null) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            globalTts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun setVocalButton(view: View?, description: String) {
        view?.setOnLongClickListener {
            speak(description)
            vibrate(30)
            true
        }
        view?.contentDescription = description
    }

    fun vibrate(duration: Long = 50) {
        com.example.visioncart.util.VibrationUtil.vibrateClick(this)
    }

    fun vibrateSuccess() {
        com.example.visioncart.util.VibrationUtil.vibrateSuccess(this)
    }

    fun vibrateError() {
        com.example.visioncart.util.VibrationUtil.vibrateError(this)
    }

    override fun onDestroy() {
        globalTts?.stop()
        globalTts?.shutdown()
        speechRecognizer?.destroy()
        toneGenerator?.release()
        super.onDestroy()
    }
}
