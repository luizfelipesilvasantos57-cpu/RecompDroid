plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}
android {
    namespace = "com.recompdroid"; compileSdk = 35
    defaultConfig { applicationId = "com.recompdroid"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.0.2" }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}

androidComponents {
    onVariants { variant ->
        if (variant.name == "debug") {
            variant.outputs.forEach { output ->
                output.outputFileName.set("RecompDroid.apk")
            }
        }
    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
}
