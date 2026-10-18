package libraries

import es.sebas1705.convention.androidTestImplementation
import es.sebas1705.convention.libs
import es.sebas1705.convention.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import kotlin.math.max

/**
 * A plugin that configures the dependencies for the Test libraries.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class UnitTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.android")

            dependencies {
                androidTestImplementation(libs.findLibrary("kotlinx-test").get())
                androidTestImplementation(libs.findLibrary("mockwebserver").get())
                androidTestImplementation(libs.findLibrary("mockito").get())
                androidTestImplementation(libs.findLibrary("junit").get())
                androidTestImplementation(libs.findLibrary("espresso-core").get())

                testImplementation(libs.findLibrary("mockito").get())
                testImplementation(libs.findLibrary("junit").get())
                testImplementation(libs.findLibrary("mockito-kotlin").get())
                testImplementation(libs.findLibrary("test-core").get())
                testImplementation(libs.findLibrary("test-runner").get())
                testImplementation(libs.findLibrary("test-rules").get())
                testImplementation(libs.findLibrary("turbine").get())
                testImplementation(libs.findLibrary("reflections-test").get())
                testImplementation(libs.findLibrary("robolectric").get())
            }

            tasks.withType(Test::class.java).configureEach {
                // Keep test output consistent in CI and local runs.
                testLogging {
                    events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                    exceptionFormat = TestExceptionFormat.FULL
                }

                maxParallelForks = max(1, Runtime.getRuntime().availableProcessors() / 2)
                failFast = false
            }
        }
    }
}