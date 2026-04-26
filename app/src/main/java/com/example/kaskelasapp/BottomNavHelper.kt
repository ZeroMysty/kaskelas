@file:Suppress("DEPRECATION")
package com.example.kaskelasapp

import android.app.Activity
import android.content.Intent
import android.view.View

object BottomNavHelper {
    fun setupBottomNav(activity: Activity) {
        val navHome = activity.findViewById<View>(R.id.navHome)
        val navHistory = activity.findViewById<View>(R.id.navHistory)
        val navProfile = activity.findViewById<View>(R.id.navProfile)
        val navSettings = activity.findViewById<View>(R.id.navSettings)

        // Update UI Active State
        val colorActive = android.graphics.Color.parseColor("#111111")
        val colorInactive = android.graphics.Color.parseColor("#999999")

        val ivHome = activity.findViewById<android.widget.ImageView>(R.id.ivNavHome)
        val tvHome = activity.findViewById<android.widget.TextView>(R.id.tvNavHome)
        val ivHistory = activity.findViewById<android.widget.ImageView>(R.id.ivNavHistory)
        val tvHistory = activity.findViewById<android.widget.TextView>(R.id.tvNavHistory)
        val ivProfile = activity.findViewById<android.widget.ImageView>(R.id.ivNavProfile)
        val tvProfile = activity.findViewById<android.widget.TextView>(R.id.tvNavProfile)
        val ivSettings = activity.findViewById<android.widget.ImageView>(R.id.ivNavSettings)
        val tvSettings = activity.findViewById<android.widget.TextView>(R.id.tvNavSettings)

        // Reset all to Inactive
        val items = listOf(
            Triple(ivHome, tvHome, MainActivity::class.java),
            Triple(ivHistory, tvHistory, RiwayatActivity::class.java),
            Triple(ivProfile, tvProfile, AnggotaActivity::class.java),
            Triple(ivSettings, tvSettings, SettingsActivity::class.java)
        )

        for (item in items) {
            item.first?.setColorFilter(colorInactive)
            item.second?.setTextColor(colorInactive)
            item.second?.setTypeface(null, android.graphics.Typeface.NORMAL)
            
            if (item.third.isInstance(activity)) {
                item.first?.setColorFilter(colorActive)
                item.second?.setTextColor(colorActive)
                item.second?.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }

        navHome?.setOnClickListener {
            if (activity !is MainActivity) {
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
                activity.overridePendingTransition(0, 0)
            }
        }

        navHistory?.setOnClickListener {
            if (activity !is RiwayatActivity) {
                activity.startActivity(Intent(activity, RiwayatActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        navProfile?.setOnClickListener {
            if (activity !is AnggotaActivity) {
                activity.startActivity(Intent(activity, AnggotaActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        navSettings?.setOnClickListener {
            if (activity !is SettingsActivity) {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}
