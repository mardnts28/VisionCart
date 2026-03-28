package com.example.visioncart

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visioncart.R

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        findViewById<FrameLayout>(R.id.btnBack).setOnClickListener { finish() }

        val brand = intent.getStringExtra("brand") ?: "Unknown"
        val name = intent.getStringExtra("name") ?: "Unknown"
        val price = intent.getStringExtra("price") ?: "N/A"
        val expires = intent.getStringExtra("expires") ?: "N/A"
        val weight = intent.getStringExtra("weight") ?: "N/A"
        val barcode = intent.getStringExtra("barcode") ?: "N/A"
        val category = intent.getStringExtra("category") ?: "N/A"
        val ingredients = intent.getStringExtra("ingredients") ?: "Not available"
        val allergens = intent.getStringExtra("allergens") ?: "None listed"

        findViewById<TextView>(R.id.tvBrand).text = brand
        findViewById<TextView>(R.id.tvProductName).text = name
        findViewById<TextView>(R.id.tvPrice).text = price
        findViewById<TextView>(R.id.tvExpires).text = expires
        findViewById<TextView>(R.id.tvWeight).text = weight
        findViewById<TextView>(R.id.tvBarcode).text = barcode
        findViewById<TextView>(R.id.tvCategory).text = category
        findViewById<TextView>(R.id.tvIngredients).text = ingredients
        findViewById<TextView>(R.id.tvAllergens).text = allergens
    }
}
