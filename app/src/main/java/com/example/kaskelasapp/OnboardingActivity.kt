package com.example.kaskelasapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        BackgroundHelper.applyAnimatedBackground(this)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerOnboarding)
        val btnNext = findViewById<Button>(R.id.btnNextOnboarding)

        val pages = listOf(
            OnboardingPage(
                "Kas Kelas",
                "Solusi cerdas untuk mengelola iuran kas kelompok atau kelasmu secara transparan.",
                R.drawable.ic_wallet
            ),
            OnboardingPage(
                "Dashboard Modern",
                "Pantau saldo dan statistik pengeluaran dengan grafik yang interaktif dan mewah.",
                R.drawable.ic_search
            ),
            OnboardingPage(
                "Siap Mulai?",
                "Kami akan membersihkan data uji coba agar kamu bisa mulai mencatat data kelasmu sendiri.",
                R.drawable.ic_plus_circle_green
            )
        )

        viewPager.adapter = OnboardingAdapter(pages)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pages.size - 1) {
                    btnNext.text = "Mulai Sekarang"
                } else {
                    btnNext.text = "Lanjut"
                }
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < pages.size - 1) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        // 1. Reset Database (Factory Reset)
        val db = DatabaseHelper(this)
        db.resetDatabase()

        // 2. Mark as done in SharedPreferences
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("onboarding_finished", true)
            apply()
        }

        // 3. Go to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

data class OnboardingPage(val title: String, val desc: String, val iconRes: Int)

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tvOnboardingTitle)
        val desc = view.findViewById<TextView>(R.id.tvOnboardingDesc)
        val icon = view.findViewById<ImageView>(R.id.ivOnboardingIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = pages[position]
        holder.title.text = page.title
        holder.desc.text = page.desc
        holder.icon.setImageResource(page.iconRes)
    }

    override fun getItemCount() = pages.size
}
