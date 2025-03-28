plugins {
    alias(libs.plugins.android.application)
    id("com.chaquo.python")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.moon.TeleHeart"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moon.TeleHeart"
        minSdk = 28
        targetSdk = 35
        versionCode = 9
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Explicitly keep these languages
        resourceConfigurations += listOf("en", "ru")

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false  // Disabled to keep all resources

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Ensure all languages are included in the base APK
    bundle {
        language {
            enableSplit = false
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
            install("telethon")
            install("load_dotenv")
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
    implementation(libs.geoip2)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-process:2.6.2")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.gms:play-services-ads:24.1.0")
    implementation("com.yandex.android:mobileads:7.11.0")
}