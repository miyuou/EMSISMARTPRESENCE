plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.emsismartpresence"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.emsismartpresence"
        minSdk = 29
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.foundation.android)
    implementation(libs.gridlayout)
    implementation(libs.firebase.storage)
    implementation(libs.ads.mobile.sdk)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-location:20.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.json:json:20230227")
    implementation ("androidx.core:core:1.13.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("com.google.android.material:material:1.11.0")

    // If you're using compactcalendarview, use the AndroidX version:0'
    implementation ("com.github.sundeepk:compact-calendar-view:3.0.0")
}