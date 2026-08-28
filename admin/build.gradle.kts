import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun configuredBackendUrl(): String? {
    project.findProperty("RAKSHA_BACKEND_BASE_URL")
        ?.toString()
        ?.trim()
        ?.trimEnd('/')
        ?.takeIf { it.isNotBlank() }
        ?.let { return it }

    // Keep the Admin app aligned with the existing Sister app's local configuration
    // when the URL is currently stored in app/gradle.properties on the developer machine.
    val sisterProperties = rootProject.file("app/gradle.properties")
    if (sisterProperties.isFile) {
        val properties = Properties()
        sisterProperties.inputStream().use { properties.load(it) }
        properties.getProperty("RAKSHA_BACKEND_BASE_URL")
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
    }

    return null
}

val configuredBackendBaseUrl = configuredBackendUrl()

android {
    namespace = "com.rajnish.rakshabandhan.admin"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "com.rajnish.rakshabandhan.admin"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BACKEND_BASE_URL", "\"${configuredBackendBaseUrl ?: "http://10.0.2.2:8080"}\"")
        }
        release {
            optimization { enable = false }
            buildConfigField("String", "BACKEND_BASE_URL", "\"${configuredBackendBaseUrl ?: error("RAKSHA_BACKEND_BASE_URL must be configured for release builds")}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
