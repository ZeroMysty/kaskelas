package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import java.util.concurrent.Executor

@android.annotation.SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        // FIX #8: Timeout sebelum re-auth diperlukan (5 menit)
        private const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L
        private const val PREF_LAST_AUTH_TIME = "last_auth_time"
    }

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
        cardLogo.scaleX = 3f
        cardLogo.scaleY = 3f
        
        // 2. Ledakan Logo (Aggressive Zoom In)
        cardLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
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

                // Teks Reveal
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
            
            val settingsPref = getSharedPreferences("SettingsKas", android.content.Context.MODE_PRIVATE)
            val isAppLockEnabled = settingsPref.getBoolean("app_lock_enabled", false)

            if (isAppLockEnabled) {
                val biometricManager = androidx.biometric.BiometricManager.from(this)
                val canAuthenticate = biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                    showBiometricPrompt {
                        // Catat waktu autentikasi berhasil
                        settingsPref.edit().putLong(PREF_LAST_AUTH_TIME, System.currentTimeMillis()).apply()
                        navigateToNextScreen(isFinished)
                    }
                } else {
                    settingsPref.edit().putBoolean("app_lock_enabled", false).apply()
                    Toast.makeText(this, "Perangkat tidak mendukung kunci biometrik. Fitur dinonaktifkan.", Toast.LENGTH_LONG).show()
                    navigateToNextScreen(isFinished)
                }
            } else {
                navigateToNextScreen(isFinished)
            }
        }, 3500)
    }

    override fun onResume() {
        super.onResume()
        // FIX #8: Re-auth jika app kembali dari background setelah > 5 menit
        // Hanya berlaku jika SplashActivity sudah di-create sebelumnya (bukan fresh start)
        // Cek dengan melihat apakah ada saved state yang menandakan activity sudah pernah berjalan
        checkReAuthIfNeeded()
    }

    private fun checkReAuthIfNeeded() {
        val settingsPref = getSharedPreferences("SettingsKas", android.content.Context.MODE_PRIVATE)
        val isAppLockEnabled = settingsPref.getBoolean("app_lock_enabled", false)
        if (!isAppLockEnabled) return

        val lastAuthTime = settingsPref.getLong(PREF_LAST_AUTH_TIME, 0L)
        val timeSinceAuth = System.currentTimeMillis() - lastAuthTime

        // Jika sudah lebih dari 5 menit sejak autentikasi terakhir, paksa auth ulang
        if (lastAuthTime > 0 && timeSinceAuth > AUTH_TIMEOUT_MS) {
            val biometricManager = androidx.biometric.BiometricManager.from(this)
            val canAuthenticate = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                showBiometricPrompt {
                    settingsPref.edit().putLong(PREF_LAST_AUTH_TIME, System.currentTimeMillis()).apply()
                }
            }
        }
    }

    private fun navigateToNextScreen(isFinished: Boolean) {
        val intent = if (isFinished) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }
        
        startActivity(intent)
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(android.app.Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Autentikasi gagal: $errString", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autentikasi gagal. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Kunci Aplikasi Kas Kelas")
            .setSubtitle("Gunakan sidik jari atau PIN Anda untuk membuka")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
