package com.example.visioncart

import android.Manifest
import android.content.Context
import android.content.Intent

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.visioncart.api.GeminiService
import com.example.visioncart.repository.FoodRepository
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : BaseActivity() {

    private var isScanning = false
    private var isProcessing = false
    private val foodRepository = FoodRepository()
    private val geminiService = GeminiService()
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    // Views
    private var previewView: PreviewView? = null
    private var cornerIdleTL: ImageView? = null
    private var cornerIdleTR: ImageView? = null
    private var cornerIdleBL: ImageView? = null
    private var cornerIdleBR: ImageView? = null
    private var cornerGreenTL: ImageView? = null
    private var cornerGreenTR: ImageView? = null
    private var cornerGreenBL: ImageView? = null
    private var cornerGreenBR: ImageView? = null
    private var centerIdleHint: LinearLayout? = null
    private var tvDetecting: TextView? = null
    private var tvInfoCard: TextView? = null
    private var tvScanBtnText: TextView? = null
    private var btnScanNow: LinearLayout? = null
    private var voiceAssistantCard: RelativeLayout? = null

    override fun onTtsReady() {
        speak("Scanning Screen. Align barcode with the frame and wait for vibration.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        // Bind Views
        previewView = findViewById(R.id.previewView)
        cornerIdleTL = findViewById(R.id.cornerIdleTL)
        cornerIdleTR = findViewById(R.id.cornerIdleTR)
        cornerIdleBL = findViewById(R.id.cornerIdleBL)
        cornerIdleBR = findViewById(R.id.cornerIdleBR)
        cornerGreenTL = findViewById(R.id.cornerGreenTL)
        cornerGreenTR = findViewById(R.id.cornerGreenTR)
        cornerGreenBL = findViewById(R.id.cornerGreenBL)
        cornerGreenBR = findViewById(R.id.cornerGreenBR)
        centerIdleHint = findViewById(R.id.centerIdleHint)
        tvDetecting = findViewById(R.id.tvDetecting)
        tvInfoCard = findViewById(R.id.tvInfoCard)
        tvScanBtnText = findViewById(R.id.tvScanBtnText)
        btnScanNow = findViewById(R.id.btnScanNow)
        voiceAssistantCard = findViewById(R.id.voiceAssistantCard)

        findViewById<FrameLayout>(R.id.btnBack).setOnClickListener { finish() }

        btnScanNow?.setOnClickListener {
            if (!isScanning) {
                checkCameraPermission()
            }
        }

        findViewById<TextView>(R.id.btnCloseVoice).setOnClickListener {
            voiceAssistantCard?.visibility = View.GONE
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScanning() {
        isScanning = true
        showScanningUI()
        startCamera()
    }

    private fun showScanningUI() {
        cornerIdleTL?.visibility = View.GONE
        cornerIdleTR?.visibility = View.GONE
        cornerIdleBL?.visibility = View.GONE
        cornerIdleBR?.visibility = View.GONE
        cornerGreenTL?.visibility = View.VISIBLE
        cornerGreenTR?.visibility = View.VISIBLE
        cornerGreenBL?.visibility = View.VISIBLE
        cornerGreenBR?.visibility = View.VISIBLE
        centerIdleHint?.visibility = View.GONE
        tvDetecting?.visibility = View.VISIBLE
        tvInfoCard?.text = "Align barcode with the frame"
        tvScanBtnText?.text = "SCANNING..."
        btnScanNow?.setBackgroundResource(R.drawable.bg_detected_button)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView?.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isProcessing) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && !isProcessing) {
                        // Capture bitmap for Gemini before closing imageProxy
                        val bitmap = imageProxy.toBitmap()
                        onBarcodeDetected(barcodes[0].displayValue ?: "", bitmap)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun onBarcodeDetected(barcode: String, bitmap: Bitmap?) {
        isProcessing = true
        
        // Vibration feedback
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("vibration_enabled", true)) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.vibrate(100)
        }

        runOnUiThread {

            tvDetecting?.text = "Detected: $barcode"
            voiceAssistantCard?.let { showFadeIn(it) }
            tvInfoCard?.text = "Barcode found! Fetching data..."
        }

        lifecycleScope.launch {
            // Start both API calls in parallel
            val productDeferred = async { foodRepository.fetchProduct(barcode) }
            val geminiDeferred = if (bitmap != null) {
                async { geminiService.detectExpirationDate(bitmap) }
            } else null

            val product = productDeferred.await()
            val expiryFromAI = geminiDeferred?.await()

            if (product != null) {
                // Combine data
                product.expires = expiryFromAI ?: "January 2026" // Fallback
                HistoryActivity.historyList.add(0, product)
                
                tvScanBtnText?.text = "DETECTED!"
                delay(800)
                val intent = Intent(this@ScanActivity, ProductDetailActivity::class.java).apply {
                    putExtra("brand", product.brand)
                    putExtra("name", product.name)
                    putExtra("price", product.price)
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
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScanActivity, "Product not in database.", Toast.LENGTH_SHORT).show()
                    isProcessing = false
                    tvDetecting?.text = "Detecting barcode..."
                }
            }
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        val plane = planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        
        // Simple conversion for YUV to Bitmap if needed
        if (format == ImageFormat.YUV_420_888) {
           val yubImage = YuvImage(bytes, ImageFormat.NV21, width, height, null)
           val out = ByteArrayOutputStream()
           yubImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
           val imageBytes = out.toByteArray()
           return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun showFadeIn(view: View) {
        view.visibility = View.VISIBLE
        val fade = AlphaAnimation(0f, 1f)
        fade.duration = 300
        view.startAnimation(fade)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
}



