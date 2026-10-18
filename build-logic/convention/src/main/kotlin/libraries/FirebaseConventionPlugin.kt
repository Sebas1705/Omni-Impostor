package libraries

import es.sebas1705.convention.implementation
import es.sebas1705.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A plugin that configures the dependencies for the Firebase libraries.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class FirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                implementation(libs.findLibrary("firebase-analytics").get())
                implementation(libs.findLibrary("firebase-crashlytics").get())
                implementation(libs.findLibrary("firebase-messaging").get())
                implementation(libs.findLibrary("firebase-auth").get())
                implementation(libs.findLibrary("firebase-firestore").get())
                implementation(libs.findLibrary("firebase-storage").get())
            }
        }
    }
}