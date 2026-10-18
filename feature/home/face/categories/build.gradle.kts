plugins {
    id("buildlogic.android.feature")
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.feature.home.face.categories"
}

dependencies {
    api(projects.core.common)
    api(projects.core.ui)
    api(projects.core.designsystem)
    api(projects.domain.models)
    api(projects.domain.usescases.game)
    implementation(libs.lottie.compose)

    api(projects.feature.home.face.common)

    testImplementation(libs.junit.junit)
}
