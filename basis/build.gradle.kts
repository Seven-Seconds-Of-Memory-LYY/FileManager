plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.seven.basis"
    compileSdk {
        version = release(libs.versions.targetSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "src/main/keepRules/rules.keep")
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
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.okhttp)
    implementation(libs.squareup.logging.interceptor)
    implementation(libs.timber)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.mmkv)
    api(libs.kotlinx.serialization.json)
    api(libs.retrofit)
}