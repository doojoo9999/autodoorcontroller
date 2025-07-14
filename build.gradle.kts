plugins {
    kotlin("jvm") version "2.1.21"
    id("io.ktor.plugin") version "3.2.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven-central.storage.apis.com")
    }
    uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    mavenLocal()
    flatDir {
        dirs("libs")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-thymeleaf-jvm")

    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}