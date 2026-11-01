plugins {
    id("buildlogic.android.feature")
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.feature.home.face.nav"
}

dependencies {
    api(projects.core.common)
    api(projects.core.ui)
    api(projects.core.designsystem)
    api(projects.core.resources)

    api(projects.feature.home.face.common)
    api(projects.feature.home.face.categories)
    api(projects.feature.home.face.users)
    api(projects.feature.home.face.mode)
    api(projects.feature.home.ranking)
    api(projects.feature.home.profile)
    implementation(libs.material.icons.extended)

    testImplementation(libs.kotlinx.test)
    testImplementation(libs.mockito.kotlin)
}
