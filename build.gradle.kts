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
    jacoco
}

jacoco {
    toolVersion = libs.versions.orgJacocoCore.get()
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

// Subprojects are evaluated after the root, so anything that looks at their tasks has to be a
// provider resolved at execution time.
val coveredSubprojects = provider {
    subprojects.filter { it.tasks.names.contains("jacocoTestReport") }
}

// Same generated-code exclusions as JacocoConventionPlugin, so the merged total is the sum of the
// per-module reports.
val mergedCoverageExcludes = listOf(
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

val jacocoMergedReport = tasks.register<JacocoReport>("jacocoMergedReport") {
    group = "verification"
    description = "Merges every module's debug unit-test coverage into build/reports/jacoco/merged."

    dependsOn(coveredSubprojects.map { projects -> projects.map { "${it.path}:jacocoTestReport" } })

    executionData.setFrom(
        coveredSubprojects.map { projects ->
            projects.map { p -> p.fileTree(p.layout.buildDirectory.dir("jacoco")) { include("testDebugUnitTest.exec") } }
        }
    )
    classDirectories.setFrom(
        coveredSubprojects.map { projects ->
            projects.map { p ->
                files(
                    p.fileTree(p.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(mergedCoverageExcludes) },
                    p.fileTree(p.layout.buildDirectory.dir("intermediates/javac/debug/classes")) { exclude(mergedCoverageExcludes) }
                )
            }
        }
    )
    sourceDirectories.setFrom(
        coveredSubprojects.map { projects ->
            projects.flatMap { p -> listOf(p.file("src/main/java"), p.file("src/main/kotlin")) }
        }
    )

    val xmlReport = layout.buildDirectory.file("reports/jacoco/merged/jacocoMergedReport.xml")
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(xmlReport)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/merged/html"))
    }
    outputs.upToDateWhen { false }

    doLast {
        // The report totals are the <counter> children of <report>; the validate workflow reads the
        // same LINE counter to post the number in the job summary.
        val report = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }.newDocumentBuilder().parse(xmlReport.get().asFile)
        val children = report.documentElement.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeName == "counter" && node.attributes.getNamedItem("type").nodeValue == "LINE") {
                val covered = node.attributes.getNamedItem("covered").nodeValue.toInt()
                val total = covered + node.attributes.getNamedItem("missed").nodeValue.toInt()
                val percent = if (total == 0) 0.0 else covered * 100.0 / total
                logger.lifecycle("[jacoco] merged line coverage: %.2f%% (%d/%d lines)".format(percent, covered, total))
            }
        }
    }
}

tasks.register("coverageUnitTestAll") {
    group = "verification"
    description = "Runs unit tests and generates Jacoco reports (per module and merged) for all subprojects that support it."

    // Depends on all subproject jacocoTestReport tasks when present, plus the merged report.
    dependsOn(coveredSubprojects.map { projects -> projects.map { "${it.path}:jacocoTestReport" } })
    dependsOn(jacocoMergedReport)
}

// Gradle 9 fails a Test task whose module has test sources but no discoverable test (e.g. only helpers).
// Most modules have no tests yet, so that must not break `testDebugUnitTest` for the whole project.
subprojects {
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests = false
    }
}
