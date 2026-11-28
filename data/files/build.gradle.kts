plugins {
    alias(libs.plugins.buildlogic.data)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.data.files"

    sourceSets {
        // The concept map sources (concepts.jsonl, links.jsonl) are bundled as they are: the app compiles them on use.
        getByName("main").assets.srcDir("concepts")
    }
}

dependencies {
    api(projects.core.common)
    api(projects.data.analytics)

    implementation(libs.kotlin.serialization.json)

    testImplementation(libs.junit.junit)
}
