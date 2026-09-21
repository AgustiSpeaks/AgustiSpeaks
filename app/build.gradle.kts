plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.tunombre.agustispeaks"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tunombre.agustispeaks"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// La salida de las pruebas de tesis se copia tal cual al informe, y lleva
// acentos, eñes y algún símbolo matemático. Sin fijar la codificación, el JDK
// toma la del sistema y en Windows eso puede escupir "Ã³" en vez de "ó": una
// tabla ilegible para pegar, y distinta según la máquina que la corra.
tasks.withType<Test>().configureEach {
    defaultCharacterEncoding = "UTF-8"
}

dependencies {
    // Core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose (BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.sizeclass)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Debug / tooling
    debugImplementation(libs.androidx.ui.tooling)

    // Tests locales en la JVM. Nada de esto entra en el APK ni toca la red.
    // `org.json` viene en Android, pero en los tests locales es un stub que
    // lanza "not mocked": hace falta la implementación real de la JVM para que
    // el parser se pueda probar sin dispositivo.
    testImplementation(libs.junit)
    testImplementation(libs.json)
}
