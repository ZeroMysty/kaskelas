plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // PERBAIKAN 1: Pakai kurung dan petik dua
}

android {
    namespace = "com.example.kaskelasapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kaskelasapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ROOM DATABASE SETUP (Sintaks Kotlin DSL)
    val room_version = "2.8.4"

    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")

    // MPAndroidChart untuk grafik
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Keamanan (App Lock)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // JSON Parsing untuk Backup
    implementation("com.google.code.gson:gson:2.11.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0")
}