plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.sreecharan.app_01_subscription_treemap.baselineprofile"
    compileSdk = 36

    defaultConfig {
        // Macrobenchmark needs API 28+.
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    // The generator runs against the release build type of :app.
    testBuildType = "release"
}

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.uiautomator)
}
