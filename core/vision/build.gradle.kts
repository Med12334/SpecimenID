plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.specimenid.vision"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Lien vers le module domaine pour accéder à ScannedBlock et Hazard
    implementation(project(":core:domain"))

    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    
    implementation(libs.mlkit.text.recognition)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
