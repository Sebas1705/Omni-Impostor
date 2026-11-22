plugins {
    alias(libs.plugins.buildlogic.data)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.data.repositories"
}

dependencies {
    api(projects.core.common)
    api(projects.data.analytics)
    api(projects.data.authentication)
    api(projects.data.couchbase)
    api(projects.data.datastore)
    api(projects.data.files)
    api(projects.data.firestore)
    api(projects.data.retrofit)
    api(projects.data.room)

    testImplementation(libs.junit.junit)
    testImplementation(libs.kotlinx.test)
    testImplementation(libs.mockito.kotlin)
}