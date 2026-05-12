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
        BackgroundHelper.applyAnimatedBackground(this)

        val cardLogo = findViewById<android.view.View>(R.id.cardLogoSplash)
        val layoutText = findViewById<android.widget.LinearLayout>(R.id.layoutTextSplash)
        val logoGlow = findViewById<android.view.View>(R.id.logoGlow)
        val flashOverlay = findViewById<android.view.View>(R.id.flashOverlay)
        val p1 = findViewById<android.view.View>(R.id.p1)
        val p2 = findViewById<android.view.View>(R.id.p2)
        val p3 = findViewById<android.view.View>(R.id.p3)
        val p4 = findViewById<android.view.View>(R.id.p4)

        // 1. Persiapan Awal (Hidden)
        cardLogo.alpha = 0f
        cardLogo.scaleX = 3f // Mulai dari sangat besar (Aggressive zoom in)
        cardLogo.scaleY = 3f
        
        // 2. Ledakan Logo (Aggressive Zoom In)
        cardLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400) // Sangat cepat
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                // Flash Effect
                flashOverlay.alpha = 0.8f
                flashOverlay.animate().alpha(0f).setDuration(300).start()
                
                // Shake Effect
                cardLogo.animate().translationYBy(-20f).setDuration(50).withEndAction {
                    cardLogo.animate().translationYBy(40f).setDuration(50).withEndAction {
                        cardLogo.animate().translationY(0f).setDuration(50).start()
                    }.start()
                }.start()

                // Particle Burst
                p1?.animate()?.alpha(1f)?.translationX(-300f)?.translationY(-400f)?.scaleX(2f)?.scaleY(2f)?.setDuration(600)?.start()
                p2?.animate()?.alpha(1f)?.translationX(350f)?.translationY(-250f)?.scaleX(1.5f)?.scaleY(1.5f)?.setDuration(700)?.start()
                p3?.animate()?.alpha(1f)?.translationX(-200f)?.translationY(500f)?.scaleX(3f)?.scaleY(3f)?.setDuration(550)?.start()
                p4?.animate()?.alpha(1f)?.translationX(400f)?.translationY(300f)?.scaleX(2.5f)?.scaleY(2.5f)?.setDuration(650)?.start()

                // Logo Glow Intense
                logoGlow?.animate()?.alpha(1f)?.scaleX(2f)?.scaleY(2f)?.setDuration(300)?.start()

                // 3. Teks Reveal (Snappy)
                layoutText?.let {
                    it.translationY = 100f
                    it.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(200)
                        .setInterpolator(android.view.animation.OvershootInterpolator())
                        .start()
                }
            }
            .start()

        // Transisi ke Onboarding atau MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
            val isFinished = sharedPref.getBoolean("onboarding_finished", false)
            
            val intent = if (isFinished) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3500)
    }
}

