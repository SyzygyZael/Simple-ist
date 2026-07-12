import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.9"
}

android {
    namespace = "com.kevinnesbitt.simple_ist"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.kevinnesbitt.simple_ist"
        minSdk = 29
        targetSdk = 36
        versionCode = 15
        versionName = "simple-ist_1.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        getByName("debug") {
            // Android uses defaults here automatically
        }
    }
    buildTypes {
        release {
            optimization {
                enable = false
            }

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )

            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    implementation("sh.calvin.reorderable:reorderable:2.4.3")
    implementation("com.android.billingclient:billing:7.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    // Main Glance Widget support
    implementation("androidx.glance:glance-appwidget:1.1.1")

    // Optional: Material 3 styling support within Glance
    implementation("androidx.glance:glance-material3:1.1.1")
    ksp("androidx.room:room-compiler:2.7.1")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}