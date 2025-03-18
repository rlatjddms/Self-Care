plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.capstone_hospital"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.capstone_hospital"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets") // assets 폴더 경로 설정
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("net.sourceforge.jtds:jtds:1.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0") // 챗 gpt (OkHttp)
    implementation("com.android.volley:volley:1.2.0") // phpmyadmin 연동
    implementation ("com.naver.maps:map-sdk:3.14.0") // 네이버 지도
    implementation ("com.google.android.gms:play-services-location:18.0.0") // 현재 위치

    // 다중 마커
    implementation ("com.squareup.retrofit2:retrofit:2.0.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("org.jsoup:jsoup:1.12.1") // html 크롤링
    implementation ("com.github.bumptech.glide:glide:4.9.0") // 이미지 url 로드

    implementation ("org.tensorflow:tensorflow-lite:2.10.0") // 머신러닝 모델
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") // 막대그래프

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // 메일 전송
    implementation(files("libs/activation.jar"))
    implementation(files("libs/additionnal.jar"))
    implementation(files("libs/mail.jar"))
}