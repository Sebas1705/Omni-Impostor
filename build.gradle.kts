plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.gms) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.benmanes.versions)
    alias(libs.plugins.dependency.analyze) apply false
}

tasks.named("dependencyUpdates").configure {
    group = "verification"
    description = "Generates a dependency update report used by the monthly maintenance workflow."
}

allprojects {
    configurations.all {
        resolutionStrategy {
            // IMPORTANT: Force protobuf version due to transitive dependency conflict
            // Affected modules: Firebase Firestore, DataStore integration
            // Root cause: Multiple Firebase libraries request different protobuf versions
            // Last validated: 2025-01-13 with Kotlin 2.1.10 and AGP 8.13.2
            // TODO: Re-evaluate with each Firebase/Datastore major upgrade
            // Consider removing once Firebase libraries align on protobuf versions
            force("com.google.protobuf:protobuf-javalite:3.19.4")
        }
    }
}

tasks.register("coverageUnitTestAll") {
    group = "verification"
    description = "Runs unit tests and generates Jacoco reports for all subprojects that support it."

    // Depends on all subproject jacocoTestReport tasks when present.
    dependsOn(
        provider {
            subprojects
                .filter { it.tasks.names.contains("jacocoTestReport") }
                .map { "${it.path}:jacocoTestReport" }
        }
    )
}

// Gradle 9 fails a Test task whose module has test sources but no discoverable test (e.g. only helpers).
// Most modules have no tests yet, so that must not break `testDebugUnitTest` for the whole project.
subprojects {
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests = false
    }
}
