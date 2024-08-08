plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.hackathonproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hackathonproject"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.mysql.connector.java.v5149) // MySQL Connector/J 5.1.49 추가
    implementation (libs.swiperefreshlayout)
    implementation (libs.jbcrypt)
    implementation (libs.cardview)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.recyclerview)
    implementation (libs.swiperefreshlayout)
}
