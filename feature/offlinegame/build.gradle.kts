plugins {
    alias(libs.plugins.buildlogic.feature)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.feature.offlinegame"
}

dependencies {
    api(projects.core.common)
    api(projects.core.ui)
    api(projects.core.designsystem)
    api(projects.core.resources)

    api(projects.domain.models)
    api(projects.domain.managers)
    api(projects.domain.usescases.game)
    api(projects.domain.usescases.ranking)

    implementation(libs.material.icons.extended)

    testImplementation(libs.kotlinx.test)
    testImplementation(libs.mockito.kotlin)
}
