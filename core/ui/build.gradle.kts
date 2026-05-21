plugins {
    alias(libs.plugins.buildlogic.core)
}

android {

    namespace = "es.sebas1705.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.resources)
}