import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("io.sentry.android.gradle") version "4.14.1"
}

android {
    namespace = "com.example.rollingicon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rollingicon"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        buildConfig = true
        flavorDimensions += "env"  // Define flavor dimension
    }

    productFlavors {
        create("dev") {
            dimension = "env"  // Specify the dimension for this flavor
            applicationIdSuffix = ".dev"  // Append '.dev' for development
            versionNameSuffix = "-dev"  // Append '-dev' for development version
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "banner_all", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "banner_splash", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_home", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_all", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "native_language", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_language_alt", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_onboarding", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "appopen_resume", "\"ca-app-pub-3940256099942544/9257395921\"")
            // Add other dev-specific configurations here
        }
        create("prod") {
            dimension = "env"  // Specify the dimension for this flavor
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "banner_all", "\"ca-app-pub-3940256099942544/9214589741\"")  
            buildConfigField("String", "banner_splash", "\"ca-app-pub-3940256099942544/9214589741\"")  
            buildConfigField("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")  
            buildConfigField("String", "inter_home", "\"ca-app-pub-3940256099942544/1033173712\"")  
            buildConfigField("String", "inter_all", "\"ca-app-pub-3940256099942544/1033173712\"")  
            buildConfigField("String", "native_language", "\"ca-app-pub-3940256099942544/2247696110\"")  
            buildConfigField("String", "native_language_alt", "\"ca-app-pub-3940256099942544/2247696110\"")  
            buildConfigField("String", "native_onboarding", "\"ca-app-pub-3940256099942544/2247696110\"")  
            buildConfigField("String", "appopen_resume", "\"ca-app-pub-3940256099942544/9257395921\"")  
            // Add production-specific configurations here
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    android.applicationVariants.all {
        this.outputs.forEach { output ->
            if (output is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                val formattedDate = SimpleDateFormat("dd-MM-yyyy HH:mm").format(Date())
                val newName = "RollingIcon_v${versionName}(${versionCode})_${buildType.name}_$formattedDate"
                var outputFileName = "$newName.apk"

                outputFileName = if (outputFileName.endsWith(".aab")) {
                    "$newName.aab"
                } else {
                    "$newName.apk"
                }

                outputFileName = outputFileName
                    .replace("release", "prod")
                    .replace("debug", "dev")
                    .replace("qa", "qa")
                    .replace("staging", "staging")
                    .replace("-.-", ".")
                    .replace("--", "-")
                    .replace("-aab", ".aab")
                    .replace("-apk", ".apk")
                output.outputFileName = outputFileName
            }

        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-svg:3.0.4")
    implementation("com.google.android.gms:play-services-ads:23.6.0")

}