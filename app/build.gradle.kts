plugins {
    alias(libs.plugins.android.application)
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
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.libraries.places:places:3.5.0") // pour des suggestions auto de lieu
    implementation("com.google.android.material:material:1.12.0") //material design
    implementation("com.makeramen:roundedimageview:2.3.0")//Image view
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.7") // composant pour navigation
    implementation("androidx.navigation:navigation-ui-ktx:2.9.7")
}