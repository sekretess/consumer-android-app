plugins {
    id("com.android.application")
}

android {
    namespace = "io.sekretess"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.sekretess"
        minSdk = 34
        targetSdk = 34
        versionCode =8
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        manifestPlaceholders["appAuthRedirectScheme"] = "com.sekretess"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable=false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation ("net.zetetic:android-database-sqlcipher:4.5.3")//Encrypted database
    implementation ("com.auth0.android:jwtdecode:2.0.0")
    implementation ("net.openid:appauth:0.9.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.signal:libsignal-client:0.64.1")
    implementation("androidx.sqlite:sqlite:2.5.1")
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.rabbitmq:amqp-client:5.20.0")
    implementation("androidx.navigation:navigation-fragment:2.9.0")
    implementation("androidx.navigation:navigation-ui:2.9.0")
    implementation("org.apache.commons:commons-lang3:3.4")
    runtimeOnly("org.signal:libsignal-android:0.64.1")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}