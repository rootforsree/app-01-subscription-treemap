import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("apptemplate.android.application")
    id("apptemplate.library.compose")
    id("apptemplate.library.hilt")
    id("apptemplate.build.spotless")
    id("apptemplate.build.detekt")
    // KSP again for Room (idempotent; the hilt convention already applies it).
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.baselineprofile)
}

// Versioning: versionCode from the CI run number (strictly increasing, what
// Play requires), versionName from the release tag, local-dev defaults.
val versionCodeProp = providers.gradleProperty("versionCode").getOrElse("1").toInt()
val versionNameProp = providers.gradleProperty("versionName").getOrElse("1.0.0-dev")

extensions.configure<ApplicationExtension> {
    namespace = "com.sreecharan.app_01_subscription_treemap"

    defaultConfig {
        applicationId = "com.sreecharan.app_01_subscription_treemap"
        targetSdk = 36
        versionCode = versionCodeProp
        versionName = versionNameProp
    }

    // NOTE: signingConfigs must live inside the android extension. At the top
    // level of a Kotlin DSL script the name does not resolve (AGP 9).
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            val hasKey = !System.getenv("KEYSTORE_PATH").isNullOrBlank()
            signingConfig =
                if (hasKey) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
            if (!hasKey) logger.warn("No release keystore in env: signing release with debug key")
            isMinifyEnabled = true
            isShrinkResources = true
            // proguard-android-optimize.txt: the plain proguard-android.txt
            // implies -dontoptimize and AGP 9 rejects it.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

// Proto DataStore codegen: lite runtime, protoc version pinned in the catalog.
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                // Android adds no default outputs: create (not named) the java
                // builtin, then enable lite codegen for the javalite runtime.
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

// Room schema export: every schema version is committed for migration tests.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    // hiltViewModel() lives here since androidx.hilt 1.4.0.
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.protobuf.javalite)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.collections.immutable)
    implementation(libs.serialization.json)
    // Sideloaded baseline profiles (bundled baseline-prof.txt) need this.
    implementation(libs.androidx.profileinstaller)
    // Consumer -> producer wiring for the :baselineprofile module. The
    // androidx.baselineprofile plugin adds this configuration to the app.
    baselineProfile(project(":baselineprofile"))
    coreLibraryDesugaring(libs.desugar)
    debugImplementation(libs.compose.ui.tooling)

    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
}
