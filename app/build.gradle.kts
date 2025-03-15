plugins {
    alias(libs.plugins.android.application)
    id("com.chaquo.python")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Add Chaquopy plugin
}

android {
    namespace = "com.example.lasttele"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lasttele"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // Define ABIs for native libraries
            abiFilters += listOf("arm64-v8a", "x86_64","armeabi-v7a","x86") // Add others if necessary
        }
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
    buildToolsVersion = "34.0.0"
    ndkVersion = "28.0.12674087 rc2"
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

chaquopy {
    defaultConfig {
        buildPython("C:/Users/USER/AppData/Local/Programs/Python/Python38-32/python.exe")
        pip {
            install("telethon")  // Add Telethon dependency
        }
    }
}


dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.lifecycle:lifecycle-process:2.6.2")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

}
