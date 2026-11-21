plugins {
    alias(libs.plugins.buildlogic.data)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.data.files"
}

dependencies {
    api(projects.core.common)
    api(projects.data.analytics)

    implementation(libs.kotlin.serialization.json)

    testImplementation(libs.junit.junit)
}
