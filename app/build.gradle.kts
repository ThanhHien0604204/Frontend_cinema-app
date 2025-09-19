import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.ntth.movie_ticket_booking_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ntth.movie_ticket_booking_app"
        minSdk = 31
        targetSdk = 35
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.support.annotations)
    implementation(fileTree(mapOf(
        "dir" to "D:\\ZaloPay",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf<String>()
    )))
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.viewpager2)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    //implementation(libs.glide)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.android.volley:volley:1.2.1")
    // Retrofit + Gson converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Lifecycle (ViewModel + LiveData)
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.4")

    // UI
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.google.android.material:material:1.12.0")

    // Chrome Custom Tabs (mở order_url)
    implementation ("androidx.browser:browser:1.8.0")
    //zalo
    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")

    implementation ("jp.wasabeef:glide-transformations:4.3.0")
}