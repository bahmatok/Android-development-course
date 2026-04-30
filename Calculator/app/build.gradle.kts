plugins {
    alias(libs.plugins.android.application)
}

fun getConfigValue(name: String): String? {
    val gradleValue = project.findProperty(name) as String?
    return if (!gradleValue.isNullOrBlank()) gradleValue else System.getenv(name)
}

android {
    namespace = "com.example.calculator"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.calculator"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "edition"
    productFlavors {
        create("demo") {
            dimension = "edition"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            buildConfigField("boolean", "HAS_ENGINEERING_FEATURES", "false")
        }
        create("full") {
            dimension = "edition"
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"
            buildConfigField("boolean", "HAS_ENGINEERING_FEATURES", "true")
        }
    }

    signingConfigs {
        val storeFilePath = getConfigValue("RELEASE_STORE_FILE")
        val storePasswordValue = getConfigValue("RELEASE_STORE_PASSWORD")
        val keyAliasValue = getConfigValue("RELEASE_KEY_ALIAS")
        val keyPasswordValue = getConfigValue("RELEASE_KEY_PASSWORD")

        if (
            !storeFilePath.isNullOrBlank() &&
            !storePasswordValue.isNullOrBlank() &&
            !keyAliasValue.isNullOrBlank() &&
            !keyPasswordValue.isNullOrBlank()
        ) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
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
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}