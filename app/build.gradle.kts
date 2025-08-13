plugins {
    id("com.android.application")
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
        targetSdk = 34
        versionCode = 15
        versionName = "1.0.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            applicationIdSuffix = ".release"
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
                "\"https://business.sekretess.io/api/v1/businesses\""
            )
            buildConfigField("String", "RABBIT_MQ_URI", "\"amqps://%s:%s@mq.sekretess.net:5671\"")
        }
        create("internal-test") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".test"
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
                "\"https://business.test.sekretess.io/api/v1/businesses\""
            )
            buildConfigField("String", "RABBIT_MQ_URI", "\"amqps://%s:%s@mq.sekretess.net:5671\"")

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
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")//Encrypted database
    implementation("com.auth0.android:jwtdecode:2.0.0")
    implementation("net.openid:appauth:0.9.1")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.signal:libsignal-client:0.64.1")
    implementation("androidx.sqlite:sqlite:2.5.2")
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.rabbitmq:amqp-client:5.25.0")
    implementation("androidx.navigation:navigation-fragment:2.9.0")
    implementation("androidx.navigation:navigation-ui:2.9.0")
    implementation("org.apache.commons:commons-lang3:3.4")
    runtimeOnly("org.signal:libsignal-android:0.64.1")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}