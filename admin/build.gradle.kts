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

fun configuredLocalAdminApiKey(): String? {
    project.findProperty("RAKSHA_ADMIN_API_KEY")
        ?.toString()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let { return it }

    val backendEnv = rootProject.file("backend/.env")
    if (backendEnv.isFile) {
        backendEnv.useLines { lines ->
            lines.firstNotNullOfOrNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("ADMIN_API_KEY=")) {
                    trimmed.substringAfter("=", "").trim().removeSurrounding("\"").removeSurrounding("'")
                        .takeIf { it.isNotBlank() }
                } else null
            }
        }?.let { return it }
    }

    return null
}

val configuredBackendBaseUrl = configuredBackendUrl()
val configuredLocalAdminApiKey = configuredLocalAdminApiKey()

android {
    namespace = "com.rajnish.rakshabandhan.admin"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "com.rajnish.rakshabandhan.admin"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["allowCleartextTraffic"] = false
    }

    buildTypes {
        debug {
            manifestPlaceholders["allowCleartextTraffic"] = true
            buildConfigField("String", "BACKEND_BASE_URL", "\"${configuredBackendBaseUrl ?: "http://10.0.2.2:8080"}\"")
            buildConfigField("String", "ADMIN_API_KEY", "\"${configuredLocalAdminApiKey?.replace("\\", "\\\\")?.replace("\"", "\\\"") ?: ""}\"")
        }
        release {
            manifestPlaceholders["allowCleartextTraffic"] = false
            optimization { enable = false }
            buildConfigField("String", "BACKEND_BASE_URL", "\"${configuredBackendBaseUrl ?: error("RAKSHA_BACKEND_BASE_URL must be configured for release builds")}\"")
            buildConfigField("String", "ADMIN_API_KEY", "\"\"")
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
