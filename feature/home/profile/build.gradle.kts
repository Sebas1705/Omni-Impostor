plugins {
    alias(libs.plugins.buildlogic.feature)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.feature.home.profile"
}

dependencies {
    api(projects.core.common)
    api(projects.core.ui)
    api(projects.core.designsystem)
    api(projects.domain.usescases.authentication)
    api(projects.domain.usescases.ranking)
    api(projects.domain.usescases.game)

    testImplementation(libs.kotlinx.test)
    testImplementation(libs.mockito.kotlin)
}
