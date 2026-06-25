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
)

val admobHomeBannerUnitId = (
    localProperties.getProperty("admob.banner.home.unit.id")
        ?: providers.gradleProperty("admob.banner.home.unit.id").orNull
        ?: "ca-app-pub-3940256099942544/6300978111"
)

val uploadStoreFile = localProperties.getProperty("upload.store.file")
    ?: providers.gradleProperty("upload.store.file").orNull
val uploadStorePassword = localProperties.getProperty("upload.store.password")
    ?: providers.gradleProperty("upload.store.password").orNull
val uploadKeyAlias = localProperties.getProperty("upload.key.alias")
    ?: providers.gradleProperty("upload.key.alias").orNull
val uploadKeyPassword = localProperties.getProperty("upload.key.password")
    ?: providers.gradleProperty("upload.key.password").orNull

val hasReleaseSigningConfig = !uploadStoreFile.isNullOrBlank()
        && !uploadStorePassword.isNullOrBlank()
        && !uploadKeyAlias.isNullOrBlank()
        && !uploadKeyPassword.isNullOrBlank()

plugins {
    alias(libs.plugins.android.application)
}

fun requireVersionPart(key: String): Int {
    val raw = providers.gradleProperty(key).orNull
        ?: error("Missing required Gradle property '$key'. Define it in gradle.properties.")
    return raw.toIntOrNull()
        ?: error("Gradle property '$key' must be an integer. Current value: '$raw'")
}

val versionMajor = requireVersionPart("VERSION_MAJOR")
val versionMinor = requireVersionPart("VERSION_MINOR")
val versionPatch = requireVersionPart("VERSION_PATCH")

android {
    namespace = "io.github.nicobdroid.vidygo"
    compileSdk = 36

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = file(uploadStoreFile!!)
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "io.github.nicobdroid.vidygo"
        minSdk = 26
        targetSdk = 36
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "YOUTUBE_DATA_API_KEY", "\"$youtubeDataApiKey\"")
        buildConfigField("String", "ADMOB_HOME_BANNER_UNIT_ID", "\"$admobHomeBannerUnitId\"")
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    implementation(libs.activity)
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