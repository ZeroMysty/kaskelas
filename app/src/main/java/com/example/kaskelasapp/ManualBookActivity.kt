package com.example.kaskelasapp

import android.os.Bundle
import android.webkit.WebView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ManualBookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_book)

        val webView = findViewById<WebView>(R.id.webViewManualBook)
        val btnBack = findViewById<ImageButton>(R.id.btnBackManual)

        btnBack.setOnClickListener {
            finish()
        }

        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/manual_book.html")
        
        BackgroundHelper.applyAnimatedBackground(this)
    }
}