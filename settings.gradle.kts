pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ImpostorAndroidGame"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")

include(":core")
include(":core:common")
include(":core:designsystem")
include(":core:resources")
include(":core:ui")

include(":data")
include(":data:analytics")
include(":data:authentication")
include(":data:firestore")
include(":data:room")
include(":data:datastore")
include(":data:files")
include(":data:retrofit")
include(":data:couchbase")
include(":data:repositories")

include(":domain")
include(":domain:managers")
include(":domain:models")
include(":domain:services")
include(":domain:mappers")
include(":domain:usescases")
include(":domain:usescases:analytics")
include(":domain:usescases:authentication")
include(":domain:usescases:game")
include(":domain:usescases:ranking")
include(":domain:usescases:opendb")
include(":domain:usescases:settings")

include(":feature")
include(":feature:main")
include(":feature:home:face:common")
include(":feature:home:face:nav")
include(":feature:home:face:categories")
include(":feature:home:face:users")
include(":feature:home:face:mode")
include(":feature:home:ranking")
include(":feature:home:profile")
include(":feature:login")
include(":feature:offlinegame")
include(":feature:splash")
include(":feature:debug")
include(":feature:settings")
