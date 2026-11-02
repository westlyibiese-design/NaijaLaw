plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Signing details come from environment variables only.
// The keystore is never inside the project and never committed.
val signingKeystorePath: String? = System.getenv("NAIJALAW_KEYSTORE_PATH")
val signingKeystorePassword: String? = System.getenv("NAIJALAW_KEYSTORE_PASSWORD")
val signingKeyAlias: String? = System.getenv("NAIJALAW_KEY_ALIAS")
val signingKeyPassword: String? = System.getenv("NAIJALAW_KEY_PASSWORD")

val hasSigningEnv = !signingKeystorePath.isNullOrBlank() &&
    !signingKeystorePassword.isNullOrBlank() &&
    !signingKeyAlias.isNullOrBlank() &&
    !signingKeyPassword.isNullOrBlank()

android {
    namespace = "com.westly.naijalaw"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.westly.naijalaw"
        minSdk = 26
        targetSdk = 35
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
        versionName = "1.0"
    }

    signingConfigs {
        if (hasSigningEnv) {
            create("release") {
                storeFile = file(signingKeystorePath!!)
                storePassword = signingKeystorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // Code and resource shrinking (R8). Keep rules are in proguard-rules.pro.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasSigningEnv) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
}
