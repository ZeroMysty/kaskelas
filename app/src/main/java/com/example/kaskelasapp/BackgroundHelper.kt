package com.example.kaskelasapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.animation.LinearInterpolator

object BackgroundHelper {

    fun applyAnimatedBackground(activity: Activity) {
        val shape1 = activity.findViewById<View>(R.id.bg_shape_1)
        val shape2 = activity.findViewById<View>(R.id.bg_shape_2)
        val shape3 = activity.findViewById<View>(R.id.bg_shape_3)
        val shape4 = activity.findViewById<View>(R.id.bg_shape_4)
        val shape5 = activity.findViewById<View>(R.id.bg_shape_5)
        val shape6 = activity.findViewById<View>(R.id.bg_shape_6)
        val shape7 = activity.findViewById<View>(R.id.bg_shape_7)

        // Drifting Animations (Organic Movement)
        animateDrift(shape1, 12000, 40f, -60f)
        animateDrift(shape2, 10000, -50f, 40f)
        animateDrift(shape3, 15000, 30f, 70f)
        
        // Rotation for Geometric Shapes
        animateRotation(shape4, 20000)
        animateRotation(shape5, 25000)
        animateRotation(shape7, 18000)

        // Pulse for Accent Shapes
        animatePulse(shape3, 4000)
        animatePulse(shape6, 3000)
        animateDrift(shape6, 8000, 20f, -20f)
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
