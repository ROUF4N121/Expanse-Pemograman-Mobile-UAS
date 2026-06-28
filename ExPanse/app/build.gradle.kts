plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler) // Aktifkan plugin Compose
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rousoft.expanse"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rousoft.expanse"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // INI DIA TOMBOL SAKLAR UNTUK MENYALAKAN JETPACK COMPOSE!
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- LIBRARY UNTUK JETPACK COMPOSE ---
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // --- LIBRARY UNTUK EXPANSE ---
    // 1. Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    add("ksp", "androidx.room:room-compiler:$roomVersion")

    // 2. Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // 3. CameraX
    implementation("androidx.camera:camera-core:1.6.1")
    implementation("androidx.camera:camera-camera2:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    // Library tambahan wajib agar CameraX tidak error di Android Studio
    implementation("com.google.guava:guava:31.1-android")

    // 4. Google ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // 5. Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")

    // Library untuk Google Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}