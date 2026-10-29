plugins {
    alias(libs.plugins.buildlogic.data)
    alias(libs.plugins.buildlogic.datastore)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.data.datastore"

    sourceSets {
        named("debug") {
            java.srcDir("build/generated/sources/proto/debug/java")
            kotlin.srcDir("build/generated/sources/proto/debug/kotlin")
        }
        named("release") {
            java.srcDir("build/generated/sources/proto/release/java")
            kotlin.srcDir("build/generated/sources/proto/release/kotlin")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    api(projects.data.analytics)
}