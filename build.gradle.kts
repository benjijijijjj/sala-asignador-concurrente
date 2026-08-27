plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.ucmaule"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "com.ucmaule.SalaAsignadorKt"
}

kotlin {
    jvmToolchain(11)
}