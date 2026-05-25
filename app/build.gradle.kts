plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.stproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.stproject"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    // Base de Données
    implementation(platform(libs.firebase.bom)) // Import de la librairie Firebase Cloud Storage

    implementation(libs.firebase.firestore) // Import de firebase firestore
    implementation(libs.firebase.storage) // Import de firebase Cloud Storage

    // Tout ce qui est lié à la localisation
    implementation(libs.playlocation)
    implementation(libs.osmdroid)
    implementation(libs.google.maps)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.libraries.places:places:3.5.0") // pour des suggestions auto de lieu
    implementation("com.google.android.material:material:1.12.0") //material design
    implementation("com.makeramen:roundedimageview:2.3.0")//Image view
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.7") // composant pour navigation
    implementation("androidx.navigation:navigation-ui-ktx:2.9.7")
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
}