package com.example.kaskelasapp.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import com.example.kaskelasapp.R
import android.app.Activity
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.kaskelasapp.data.DatabaseHelper

object BackgroundHelper {

    fun applyAnimatedBackground(activity: Activity) {
        val shape1 = activity.findViewById<View>(R.id.bg_shape_1)
        val shape2 = activity.findViewById<View>(R.id.bg_shape_2)
        val shape3 = activity.findViewById<View>(R.id.bg_shape_3)
        val shape4 = activity.findViewById<View>(R.id.bg_shape_4)
        val shape5 = activity.findViewById<View>(R.id.bg_shape_5)
        val shape6 = activity.findViewById<View>(R.id.bg_shape_6)
        val shape7 = activity.findViewById<View>(R.id.bg_shape_7)

        // Drifting Animations (Organic Movement) - Faster and more visible
        animateDrift(shape1, 8000, 60f, -80f)
        animateDrift(shape2, 7000, -70f, 50f)
        animateDrift(shape3, 10000, 40f, 90f)
        
        // Rotation for Geometric Shapes
        animateRotation(shape4, 15000)
        animateRotation(shape5, 20000)
        animateRotation(shape7, 12000)
    
        // Pulse for Accent Shapes
        animatePulse(shape3, 3000)
        animatePulse(shape6, 2500)
        animateDrift(shape6, 6000, 30f, -30f)
    }

    private fun animateDrift(view: View?, duration: Long, deltaX: Float, deltaY: Float) {
        view?.let {
            ObjectAnimator.ofFloat(it, "translationX", 0f, deltaX, 0f).apply {
                this.duration = duration
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = LinearInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(it, "translationY", 0f, deltaY, 0f).apply {
                this.duration = duration + 500 // Slight offset
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    private fun animateRotation(view: View?, duration: Long) {
        view?.let {
            ObjectAnimator.ofFloat(it, "rotation", 0f, 360f).apply {
                this.duration = duration
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    private fun animatePulse(view: View?, duration: Long) {
        view?.let {
            ObjectAnimator.ofFloat(it, "alpha", 0.2f, 0.6f, 0.2f).apply {
                this.duration = duration
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        }
    }
}

