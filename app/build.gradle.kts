plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "io.sekretess"
    compileSdk = 34
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "io.sekretess"
        minSdk = 30
        targetSdk = 35
        versionCode = 26
        versionName = "1.0.26"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }


    buildTypes {
        release {
            resValue("string", "app_name", "Sekretess")
            isMinifyEnabled = false
            isDebuggable = false
//            applicationIdSuffix = ".release"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField(
                "String",
                "AUTH_API_URL",
                "\"https://auth.sekretess.io/realms/consumer/.well-known/openid-configuration\""
            )
            buildConfigField(
                "String",
                "CONSUMER_API_URL",
                "\"https://consumer.sekretess.io/api/v1/consumers\""
            )
            buildConfigField(
                "String",
                "BUSINESS_API_URL",
                "\"https://business.sekretess.net/api/v1/businesses\""
            )
            buildConfigField("String", "RABBIT_MQ_URI", "\"amqps://%s:%s@mq.sekretess.net:5671\"")
        }
        create("internal-test") {
            isDebuggable = false
            resValue("string", "app_name", "Sekretess-test")
            signingConfig = signingConfigs.getByName("debug")
//            applicationIdSuffix = ".test"
            buildConfigField(
                "String",
                "AUTH_API_URL",
                "\"https://auth.test.sekretess.io/realms/consumer/.well-known/openid-configuration\""
            )
            buildConfigField(
                "String",
                "CONSUMER_API_URL",
                "\"https://consumer.test.sekretess.io/api/v1/consumers\""
            )
            buildConfigField(
                "String",
                "BUSINESS_API_URL",
                "\"https://business.test.sekretess.net/api/v1/businesses\""
            )
            buildConfigField("String", "RABBIT_MQ_URI", "\"amqps://%s:%s@mq.test.sekretess.net:30071\"")

        }

        create("internal-test-debug") {
            isDebuggable = true
            resValue("string", "app_name", "Sekretess-test")
            signingConfig = signingConfigs.getByName("debug")
//            applicationIdSuffix = ".test"
            buildConfigField(
                "String",
                "AUTH_API_URL",
                "\"https://auth.test.sekretess.io/realms/consumer/.well-known/openid-configuration\""
            )
            buildConfigField(
                "String",
                "CONSUMER_API_URL",
                "\"https://consumer.test.sekretess.io/api/v1/consumers\""
            )
            buildConfigField(
                "String",
                "BUSINESS_API_URL",
                "\"https://business.test.sekretess.net/api/v1/businesses\""
            )
            buildConfigField("String", "RABBIT_MQ_URI", "\"amqps://%s:%s@mq.test.sekretess.net:30071\"")

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
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
//    implementation("net.zetetic:android-database-sqlcipher:4.5.4")//Encrypted database
    implementation("com.auth0.android:jwtdecode:2.0.2")
    implementation("net.openid:appauth:0.11.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.signal:libsignal-client:0.80.1")
    implementation("androidx.sqlite:sqlite:2.6.0")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.rabbitmq:amqp-client:5.26.0")
    implementation("androidx.navigation:navigation-fragment:2.9.4")
    implementation("androidx.navigation:navigation-ui:2.9.4")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    runtimeOnly("org.signal:libsignal-android:0.78.2")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.19.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.19.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}