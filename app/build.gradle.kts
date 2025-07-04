plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.imgodz.diecastvault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.imgodz.diecastvault"
        minSdk = 28
        targetSdk = 35
        versionCode = 4
        versionName = "1.2.1"

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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.material)
    implementation(libs.roomRuntime)
    annotationProcessor(libs.roomCompiler)
    implementation(libs.recyclerView)
    implementation(libs.lifecycleViewModel)
    implementation(libs.lifecycleLiveData)
    implementation(libs.cardView)
}