package com.example.kaskelasapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivity
import com.example.kaskelasapp.ui.members.AnggotaActivity
import com.example.kaskelasapp.ui.history.RiwayatActivity
import com.example.kaskelasapp.ui.settings.SettingsActivity

object NavigationHelper {

    fun setupNavigation(activity: Activity) {
        val sharedPref = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val useSidebar = sharedPref.getBoolean("use_sidebar_nav", false)

        setupBottomNav(activity, !useSidebar)
        setupDrawerNav(activity, useSidebar)
    }

    private fun setupBottomNav(activity: Activity, isVisible: Boolean) {
        val bottomNav = activity.findViewById<View>(R.id.bottomNavContainer)
        bottomNav?.visibility = if (isVisible) View.VISIBLE else View.GONE

        if (isVisible) {
            val navHome = activity.findViewById<View>(R.id.navHome)
            val navHistory = activity.findViewById<View>(R.id.navHistory)
            val navProfile = activity.findViewById<View>(R.id.navProfile)
            val navSettings = activity.findViewById<View>(R.id.navSettings)

            val ivHome = activity.findViewById<ImageView>(R.id.ivNavHome)
            val ivHistory = activity.findViewById<ImageView>(R.id.ivNavHistory)
            val ivProfile = activity.findViewById<ImageView>(R.id.ivNavProfile)
            val ivSettings = activity.findViewById<ImageView>(R.id.ivNavSettings)

            val colorActive = android.graphics.Color.parseColor("#2196F3")
            val colorInactive = android.graphics.Color.parseColor("#999999")

            val items = listOf(
                ivHome to MainActivity::class.java,
                ivHistory to RiwayatActivity::class.java,
                ivProfile to AnggotaActivity::class.java,
                ivSettings to SettingsActivity::class.java
            )

            items.forEach { (icon, clazz) ->
                icon?.setColorFilter(if (clazz.isInstance(activity)) colorActive else colorInactive)
            }

            navHome?.setOnClickListener { navigateTo(activity, MainActivity::class.java) }
            navHistory?.setOnClickListener { navigateTo(activity, RiwayatActivity::class.java) }
            navProfile?.setOnClickListener { navigateTo(activity, AnggotaActivity::class.java) }
            navSettings?.setOnClickListener { navigateTo(activity, SettingsActivity::class.java) }
        }
    }

    private fun setupDrawerNav(activity: Activity, isVisible: Boolean) {
        // Find DrawerLayout and Menu Button based on activity
        val (drawerLayout, menuButton, contentFrame) = when (activity) {
            is MainActivity -> {
                val dl = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
                Triple(dl, activity.findViewById<ImageButton>(R.id.btnMenu), dl?.getChildAt(0))
            }
            is AnggotaActivity -> {
                val dl = activity.findViewById<DrawerLayout>(R.id.drawerLayoutAnggota)
                Triple(dl, activity.findViewById<ImageButton>(R.id.btnMenuAnggota), dl?.getChildAt(0))
            }
            is RiwayatActivity -> {
                val dl = activity.findViewById<DrawerLayout>(R.id.drawerLayoutRiwayat)
                Triple(dl, activity.findViewById<ImageButton>(R.id.btnMenuRiwayat), dl?.getChildAt(0))
            }
            is SettingsActivity -> {
                val dl = activity.findViewById<DrawerLayout>(R.id.drawerLayoutSettings)
                Triple(dl, activity.findViewById<ImageButton>(R.id.btnMenuSettings), dl?.getChildAt(0))
            }
            else -> Triple(null, null, null)
        }

        val blurOverlay = activity.findViewById<View>(R.id.blurOverlay)
        menuButton?.visibility = if (isVisible) View.VISIBLE else View.GONE
        
        if (isVisible && drawerLayout != null) {
            // Make default scrim transparent to use our custom blur/dim
            drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT)

            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    blurOverlay?.apply {
                        visibility = if (slideOffset > 0) View.VISIBLE else View.GONE
                        alpha = slideOffset
                        setBackgroundColor(android.graphics.Color.parseColor("#60000000"))
                    }

                    // For API 31+, apply real Gaussian Blur
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        if (slideOffset > 0.01f) {
                            val blurRadius = slideOffset * 25f
                            contentFrame?.setRenderEffect(
                                android.graphics.RenderEffect.createBlurEffect(
                                    blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP
                                )
                            )
                        } else {
                            contentFrame?.setRenderEffect(null)
                        }
                    }
                }

                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {
                    blurOverlay?.visibility = View.GONE
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        contentFrame?.setRenderEffect(null)
                    }
                }
                override fun onDrawerStateChanged(newState: Int) {}
            })

            menuButton?.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            // Setup Sidebar Items
            activity.findViewById<View>(R.id.sideNavHome)?.setOnClickListener { 
                navigateTo(activity, MainActivity::class.java)
                drawerLayout.closeDrawers()
            }
            activity.findViewById<View>(R.id.sideNavHistory)?.setOnClickListener {
                navigateTo(activity, RiwayatActivity::class.java)
                drawerLayout.closeDrawers()
            }
            activity.findViewById<View>(R.id.sideNavMembers)?.setOnClickListener {
                navigateTo(activity, AnggotaActivity::class.java)
                drawerLayout.closeDrawers()
            }
            activity.findViewById<View>(R.id.sideNavSettings)?.setOnClickListener {
                navigateTo(activity, SettingsActivity::class.java)
                drawerLayout.closeDrawers()
            }
            activity.findViewById<View>(R.id.sideNavLogout)?.setOnClickListener {
                // Handle Logout
                val intent = Intent(activity, MainActivity::class.java) 
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
            }
        } else {
            drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            blurOverlay?.visibility = View.GONE
        }
    }

    private fun navigateTo(activity: Activity, target: Class<*>) {
        if (!target.isInstance(activity)) {
            val intent = Intent(activity, target)
            if (target == MainActivity::class.java) {
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }
}
