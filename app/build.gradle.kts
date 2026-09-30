import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.FileInputStream
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

googleServices {
    missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.cleankrcustomer.kzqwm"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePropsFile = file("${rootDir}/keystore.properties")
            val keystoreProps = Properties()
            if (keystorePropsFile.exists()) {
                keystoreProps.load(FileInputStream(keystorePropsFile))
            }

            val releaseKeystore = file("${rootDir}/release.keystore")
            val base64Keystore = file("${rootDir}/release.keystore.base64")
            if (!releaseKeystore.exists() && base64Keystore.exists()) {
                val decoded = Base64.getDecoder().decode(base64Keystore.readText().trim())
                releaseKeystore.writeBytes(decoded)
            }

            val envKeystorePath = System.getenv("KEYSTORE_PATH")
            val propKeystorePath = keystoreProps.getProperty("storeFile")
            val storeFileTarget = if (!envKeystorePath.isNullOrBlank() && file(envKeystorePath).exists()) {
                file(envKeystorePath)
            } else if (!propKeystorePath.isNullOrBlank() && file("${rootDir}/$propKeystorePath").exists()) {
                file("${rootDir}/$propKeystorePath")
            } else if (releaseKeystore.exists()) {
                releaseKeystore
            } else {
                throw GradleException("FATAL: Production release keystore does NOT exist! Refusing to sign with debug key.")
            }

            storeFile = storeFileTarget
            storePassword = System.getenv("STORE_PASSWORD")
                ?: keystoreProps.getProperty("storePassword")
                ?: "cleankr2026"
            keyAlias = System.getenv("KEY_ALIAS")
                ?: keystoreProps.getProperty("keyAlias")
                ?: "cleankr_release"
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: keystoreProps.getProperty("keyPassword")
                ?: "cleankr2026"
        }
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)

    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
