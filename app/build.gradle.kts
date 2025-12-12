plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.visoragallery"

    // FIX: AndroidX ONLY supports up to API 34
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.visoragallery"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // ------------------------- CORE -------------------------
    implementation("androidx.core:core-ktx:1.13.1")

    // FIX version lifecycle (cũ quá → lỗi LifecycleOwner)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // FIX version activity-compose (1.8 quá cũ)
    implementation("androidx.activity:activity-compose:1.9.3")

    // ------------------------- COMPOSE -------------------------
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.ui:ui-tooling-preview")

    // ------------------------- NAVIGATION -------------------------
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ------------------------- LIFECYCLE & VM -------------------------
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // ------------------------- IMAGE LOADING -------------------------
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ------------------------- ACCOMPANIST -------------------------
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // ------------------------- ZOOM -------------------------
    implementation("me.saket.telephoto:zoomable-image-coil:0.7.1")

    // ------------------------- TEST -------------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
