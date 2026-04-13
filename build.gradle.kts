// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

dependencies {
   implementation("androidx.core:core-splashscreen:1.0.1")
}
