import java.util.Properties

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use(::load)
    }
}

val youtubeDataApiKey = (
    localProperties.getProperty("youtube.data.api.key")
        ?: providers.gradleProperty("youtube.data.api.key").orNull
        ?: ""
).replace("\\", "\\\\").replace("\"", "\\\"")

val admobAppId = (
    localProperties.getProperty("admob.app.id")
        ?: providers.gradleProperty("admob.app.id").orNull
        ?: "ca-app-pub-3940256099942544~3347511713"
).replace("\\", "\\\\").replace("\"", "\\\"")

val admobHomeBannerUnitId = (
    localProperties.getProperty("admob.banner.home.unit.id")
        ?: providers.gradleProperty("admob.banner.home.unit.id").orNull
        ?: "ca-app-pub-3940256099942544/6300978111"
).replace("\\", "\\\\").replace("\"", "\\\"")

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.vidygo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.vidygo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "YOUTUBE_DATA_API_KEY", "\"$youtubeDataApiKey\"")
        buildConfigField("String", "ADMOB_HOME_BANNER_UNIT_ID", "\"$admobHomeBannerUnitId\"")
        manifestPlaceholders["admobAppId"] = admobAppId
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.glide)
    implementation(libs.preference)
    implementation(libs.core.splashscreen)
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}