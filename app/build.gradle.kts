plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.aayush.what2do"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.21")

    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-alpha3")
    // Kotlin core Android libraries
    implementation("androidx.core:core-ktx:1.1.0-alpha05")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.1.0-alpha03")
    // Room runtime library
    implementation("androidx.room:room-runtime:2.1.0-alpha06")

    // TextDrawable
    implementation("com.amulyakhare:com.amulyakhare.textdrawable:1.0.1")

    // Google's Material Design library
    implementation("com.google.android.material:material:1.1.0-alpha05")

    // Timber for logging
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Material DateTimePicker
    implementation("com.wdullaer:materialdatetimepicker:4.1.2")

    // Custom font injection library
    implementation("io.github.inflationx:calligraphy3:3.0.0")
    implementation("io.github.inflationx:viewpump:1.0.0")

    // RecyclerView animators
    implementation("jp.wasabeef:recyclerview-animators:3.0.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

    // Room compiler annotation processor for Kotlin
    kapt("androidx.room:room-compiler:2.1.0-alpha06")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")

    androidTestImplementation("androidx.test:runner:1.1.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
}
