plugins {
    alias(libs.plugins.buildlogic.core)
    alias(libs.plugins.buildlogic.unit.test)
}

android {

    namespace = "es.sebas1705.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.resources)

    testImplementation(libs.junit.junit)
}
