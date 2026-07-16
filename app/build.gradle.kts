plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun authValue(name: String, fallback: String = "") =
    providers.gradleProperty(name).orElse(fallback).get()

fun quoted(value: String) = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.synex.mobile"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.synex.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val auth0Domain = authValue("SYNEX_AUTH0_DOMAIN", "dev-5uxh5z65i7cmrxna.us.auth0.com")
        buildConfigField("String", "SYNEX_AUTH0_DOMAIN", quoted(auth0Domain))
        buildConfigField("String", "SYNEX_AUTH0_CLIENT_ID", quoted(authValue("SYNEX_AUTH0_CLIENT_ID")))
        buildConfigField("String", "SYNEX_AUTH0_AUDIENCE", quoted(authValue("SYNEX_AUTH0_AUDIENCE", "https://api.synex.app")))
        manifestPlaceholders["auth0Domain"] = auth0Domain
        manifestPlaceholders["auth0Scheme"] = "https"
    }

    buildTypes {
        debug {
            buildConfigField("String", "SYNEX_API_BASE_URL", "\"http://10.0.2.2:8080\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "SYNEX_API_BASE_URL", "\"https://api.synex.app\"")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":feature:overview"))
    implementation(project(":feature:markets"))
    implementation(project(":feature:portfolio"))
    implementation(project(":feature:account"))
    implementation(project(":feature:legal"))
    implementation(project(":feature:auth"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
