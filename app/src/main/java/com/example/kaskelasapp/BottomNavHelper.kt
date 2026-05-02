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
        val colorActive = android.graphics.Color.parseColor("#2196F3") // Blue for active
        val colorInactive = android.graphics.Color.parseColor("#999999") // Gray for inactive

        val ivHome = activity.findViewById<android.widget.ImageView>(R.id.ivNavHome)
        val ivHistory = activity.findViewById<android.widget.ImageView>(R.id.ivNavHistory)
        val ivProfile = activity.findViewById<android.widget.ImageView>(R.id.ivNavProfile)
        val ivSettings = activity.findViewById<android.widget.ImageView>(R.id.ivNavSettings)

        // Reset all to Inactive
        val items = listOf(
            Pair(ivHome, MainActivity::class.java),
            Pair(ivHistory, RiwayatActivity::class.java),
            Pair(ivProfile, AnggotaActivity::class.java),
            Pair(ivSettings, SettingsActivity::class.java)
        )

        for (item in items) {
            item.first?.setColorFilter(colorInactive)
            
            if (item.second.isInstance(activity)) {
                item.first?.setColorFilter(colorActive)
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
