plugins {
    id("gradlebuild.distribution.api-java")
}

description = "Logging API consumed by the Gradle Enterprise plugin"

gradlebuildJava.usedInWorkers()

dependencies {
    implementation(project(":build-operations"))
    implementation(project(":base-annotations"))
    implementation(libs.jsr305)
}
