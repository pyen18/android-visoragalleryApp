plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}



android {
    namespace = "com.example.visoragallery"
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
        // MUST match Kotlin 1.9.24
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += setOf(
                // Google / Apache duplicated META-INF
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",

                // Chữ ký JAR (Java SE only)
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.RSA",

                // Apache / Google license metadata
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {

    // ---------------- CORE ----------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // ---------------- COMPOSE ----------------
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // ---------------- NAVIGATION ----------------
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ---------------- VIEWMODEL ----------------
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // ---------------- IMAGE LOADING ----------------
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ---------------- PERMISSIONS ----------------
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // ---------------- ZOOM ----------------
    implementation("me.saket.telephoto:zoomable-image-coil:0.7.1")

    // ---------------- ROOM ----------------
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // ---------------- TEST ----------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Google Drive API
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Google API core
    implementation("com.google.api-client:google-api-client:2.6.0")

    // 🔥 BẮT BUỘC – CHO GoogleAccountCredential
    implementation("com.google.api-client:google-api-client-android:2.6.0")

    // HTTP + JSON
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // Drive API (bản tồn tại)
    implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0")
}
