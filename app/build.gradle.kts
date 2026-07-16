plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun authValue(name: String, fallback: String = "") =
    providers.gradleProperty(name).orElse(fallback).get()

fun quoted(value: String) = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val configuredAuth0ClientId = authValue("SYNEX_AUTH0_CLIENT_ID").trim()
val configuredAuth0Domain = authValue("SYNEX_AUTH0_DOMAIN").trim()
val configuredAuth0Audience = authValue("SYNEX_AUTH0_AUDIENCE").trim()

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
    }

    buildTypes {
        debug {
            val domain = configuredAuth0Domain.ifBlank { "dev-5uxh5z65i7cmrxna.us.auth0.com" }
            val audience = configuredAuth0Audience.ifBlank { "https://api.synex.app" }
            buildConfigField("String", "SYNEX_AUTH0_DOMAIN", quoted(domain))
            buildConfigField("String", "SYNEX_AUTH0_CLIENT_ID", quoted(configuredAuth0ClientId))
            buildConfigField("String", "SYNEX_AUTH0_AUDIENCE", quoted(audience))
            buildConfigField("String", "SYNEX_API_BASE_URL", "\"http://10.0.2.2:8080\"")
            manifestPlaceholders["auth0Domain"] = domain
            manifestPlaceholders["auth0Scheme"] = "https"
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "SYNEX_AUTH0_DOMAIN", quoted(configuredAuth0Domain))
            buildConfigField("String", "SYNEX_AUTH0_CLIENT_ID", quoted(configuredAuth0ClientId))
            buildConfigField("String", "SYNEX_AUTH0_AUDIENCE", quoted(configuredAuth0Audience))
            buildConfigField("String", "SYNEX_API_BASE_URL", "\"https://api.synex.app\"")
            manifestPlaceholders["auth0Domain"] = configuredAuth0Domain
            manifestPlaceholders["auth0Scheme"] = "https"
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

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        val missing = listOf(
            "SYNEX_AUTH0_CLIENT_ID" to configuredAuth0ClientId,
            "SYNEX_AUTH0_DOMAIN" to configuredAuth0Domain,
            "SYNEX_AUTH0_AUDIENCE" to configuredAuth0Audience,
        ).filter { it.second.isBlank() }.map { it.first }
        check(missing.isEmpty()) {
            "Release authentication is not configured. Missing: ${missing.joinToString()}"
        }
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
