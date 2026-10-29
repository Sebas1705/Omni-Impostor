package libraries

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Configures Jacoco support for Android library modules.
 *
 * This plugin centralizes the jacoco report + verification task registration that used to live in
 * the root `jacoco.gradle.kts` script. It registers `jacocoTestReport` and
 * `jacocoTestCoverageVerification` for the module where the plugin is applied.
 */
class JacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply jacoco plugin for this module
            pluginManager.apply("jacoco")

            // Set a stable tool version
            extensions.configure(JacocoPluginExtension::class.java) {
                toolVersion = "0.8.14"
            }

            // Ensure android resources are available in local unit tests when needed.
            // Support both library and application modules: configure whichever extension exists.
            // (Some modules already set this; this is safe and keeps behavior consistent.)
            extensions.findByType(LibraryExtension::class.java)?.let { libExt ->
                libExt.testOptions.unitTests.isIncludeAndroidResources = true
            } ?: run {
                extensions.findByType(ApplicationExtension::class.java)?.let { appExt ->
                    appExt.testOptions.unitTests.isIncludeAndroidResources = true
                }
            }

            // Force Test tasks to write jacoco exec data to a stable location.
            tasks.withType(Test::class.java).configureEach {
                extensions.configure(JacocoTaskExtension::class.java) {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                    setDestinationFile(layout.buildDirectory.file("jacoco/${name}.exec").get().asFile)
                }
            }

            // Android registers its test and compile tasks after this plugin runs, so `findByName`
            // here used to return null: the report then had no execution data, was SKIPPED, and
            // never pulled the tests in. A TaskCollection is resolved when the task graph is built.
            val unitTestTasks = tasks.matching { it.name == "testDebugUnitTest" }
            val compileTasks = tasks.matching { it.name == "compileDebugKotlin" || it.name == "compileDebugJavaWithJavac" }

            // The Test tasks above write their data to build/jacoco/<taskName>.exec. A file tree
            // (not a file) keeps the report SKIPPED rather than failed in a module whose test task
            // produced nothing.
            val execFilesProvider = fileTree(layout.buildDirectory.dir("jacoco")) { include("testDebugUnitTest.exec") }

            // Collect class dirs for Android + Kotlin.
            val javaClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes"))
            val kotlinClasses = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug"))

            // Typical exclusions.
            val excludes = listOf(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "**/di/**",
                "**/*_Factory.*",
                "**/*_MembersInjector.*",
                "**/Hilt_*.*",
                "**/*JsonAdapter.*"
            )

            val classDirectoriesProvider = files(
                javaClasses.matching { exclude(excludes) },
                kotlinClasses.matching { exclude(excludes) }
            )

            val sourcesProvider = files(
                "src/main/java",
                "src/main/kotlin"
            )

            // Report task
            tasks.register("jacocoTestReport", JacocoReport::class.java) {
                group = "verification"
                description = "Generates Jacoco coverage reports for unit tests (debug)."

                dependsOn(unitTestTasks)
                dependsOn(compileTasks)

                classDirectories.setFrom(classDirectoriesProvider)
                sourceDirectories.setFrom(sourcesProvider)
                executionData.setFrom(execFilesProvider)

                // Ensure directories exist (some Windows setups don't create them automatically before writing)
                doFirst {
                    val htmlDir = layout.buildDirectory.dir("reports/jacoco/test/html").get().asFile
                    val xmlFile = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
                    htmlDir.mkdirs()
                    xmlFile.parentFile.mkdirs()
                }

                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    csv.required.set(false)

                    xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml"))
                    html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
                }

                // Register outputs explicitly
                outputs.dir(layout.buildDirectory.dir("reports/jacoco/test/html"))
                outputs.file(layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml"))

                // Force the report to run even if Gradle thinks it's up-to-date (helps on Windows + jacoco agent flakiness)
                outputs.upToDateWhen { false }

                doFirst {
                    logger.lifecycle("[jacoco] ${project.path} execData: ${execFilesProvider.files}")
                }

                doLast {
                    val xmlFile = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
                    val htmlIndex = layout.buildDirectory.file("reports/jacoco/test/html/index.html").get().asFile
                    if (!xmlFile.exists() || !htmlIndex.exists()) {
                        throw GradleException(
                            "Jacoco report files were not generated for ${project.path}. " +
                                "Expected xml=${xmlFile.absolutePath} exists=${xmlFile.exists()}, " +
                                "html=${htmlIndex.absolutePath} exists=${htmlIndex.exists()}"
                        )
                    }
                }
            }

            // Verification task with 90% line coverage (as required by AGENTS.md)
            tasks.register("jacocoTestCoverageVerification", JacocoCoverageVerification::class.java) {
                group = "verification"
                description = "Verifies Jacoco coverage with minimum thresholds (unit tests)."

                dependsOn(unitTestTasks)
                dependsOn(compileTasks)

                classDirectories.setFrom(classDirectoriesProvider)
                sourceDirectories.setFrom(sourcesProvider)
                executionData.setFrom(execFilesProvider)

                // Add minimum threshold (90% lines)
                violationRules {
                    rule {
                        enabled = true
                        limit {
                            counter = "LINE"
                            value = "COVEREDRATIO"
                            minimum = "0.90".toBigDecimal()
                        }
                    }
                }
            }
        }
    }
}
