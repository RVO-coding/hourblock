plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.hourblocker"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.hourblocker"
        minSdk = 29   // CallScreeningService role requires Android 10+
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
