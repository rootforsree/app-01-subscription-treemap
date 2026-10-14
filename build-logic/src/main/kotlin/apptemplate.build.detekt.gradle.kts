import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    // Versionless: the plugin marker is on build-logic's classpath.
    // alias(libs.plugins.*) does NOT work here: the version catalog is not
    // in scope inside a precompiled script plugin's plugins {} block.
    id("io.gitlab.arturbosch.detekt")
}

// detekt.yml ships as a build-logic resource so every module shares one config.
val detektConfigUrl = javaClass.classLoader.getResource("detekt.yml")
    ?: error("detekt.yml not found on the build-logic classpath")

extensions.configure<DetektExtension> {
    config.setFrom(files(java.io.File(detektConfigUrl.toURI())))
    buildUponDefaultConfig = true
    allRules = false
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
}

// Wire detekt into `./gradlew check` alongside Spotless and Android Lint.
tasks.named("check") {
    dependsOn(tasks.withType<Detekt>())
}
