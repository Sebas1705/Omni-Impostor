plugins {
    alias(libs.plugins.buildlogic.domain)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.domain.mappers"
}

dependencies {
    api(projects.core.common)
    api(projects.domain.models)
    api(projects.data.datastore)
    api(projects.data.files)
    api(projects.data.firestore)
    api(projects.data.retrofit)
    api(projects.data.room)
}