package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val cardLogo = findViewById<CardView>(R.id.cardLogoSplash)
        val layoutText = findViewById<LinearLayout>(R.id.layoutTextSplash)

        // Load Animations
        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_premium_anim)
        val textAnim = AnimationUtils.loadAnimation(this, R.anim.text_fade_in)

        // Start Logo Animation
        cardLogo.startAnimation(logoAnim)
        
        // Munculkan teks dengan delay setelah logo mulai geser
        Handler(Looper.getMainLooper()).postDelayed({
            layoutText.visibility = android.view.View.VISIBLE
            layoutText.startAnimation(textAnim)
        }, 1200)

        // Transisi ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3500) 
    }
}
