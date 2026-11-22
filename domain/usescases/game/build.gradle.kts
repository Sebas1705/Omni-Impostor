plugins {
    alias(libs.plugins.buildlogic.domain)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.domain.usescases.game"
}

dependencies {
    api(projects.core.common)
    api(projects.data.repositories)
    api(projects.domain.models)
    api(projects.domain.mappers)
    api(projects.domain.usescases.ranking)

    testImplementation(libs.junit.junit)
    testImplementation(libs.kotlinx.test)
    testImplementation(libs.mockito.kotlin)
}
