/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
}

val keystorePropertiesFile = rootProject.file("/signing.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists())
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.ghostwalker18.schedulepfc"
    compileSdk = 34

    bundle {
        language {
            enableSplit = false
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["keystore"].toString())
            storePassword = keystoreProperties["keystorePassword"].toString()
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
        }
    }

    defaultConfig {
        applicationId = "com.ghostwalker18.schedulepfc"
        minSdk = 26
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

        debug{
            isMinifyEnabled = false
            isDebuggable = true
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.guava:listenablefuture:1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jsoup:jsoup:1.12.2")
    implementation("javax.xml.stream:stax-api:1.0")
    implementation("com.fasterxml:aalto-xml:1.2.2")
    implementation("androidx.room:room-guava:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("androidx.room:room-compiler:2.6.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}