plugins {
    kotlin("jvm") version "1.5.31"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion = "0.36.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.exposed","exposed-core",exposedVersion)
    implementation("org.jetbrains.exposed","exposed-dao",exposedVersion)
    implementation("org.jetbrains.exposed","exposed-jdbc",exposedVersion)
    implementation("org.postgresql","postgresql","42.3.1")
    implementation("junit","junit","4.13.1")
}