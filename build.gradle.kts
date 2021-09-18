import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    // Getting compiler errors with 1.5.30 :-(
    kotlin("jvm") version "1.4.30"
    java
}

group = "uprizerlabs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.30")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.30")
    implementation("com.github.sanity:pairAdjacentViolators:1.4.16")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("de.ruedigermoeller:fst:3.0.3")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
    implementation("com.fatboyindustrial.gson-javatime-serialisers:gson-javatime-serialisers:1.1.1")
    implementation("org.tukaani:xz:1.9")
    implementation("org.danilopianini:gson-extras:0.2.2")
    implementation("com.eatthepath:jvptree:0.3.0")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation("org.slf4j:slf4j-api:1.7.32")

    // Logging
    implementation("io.github.microutils:kotlin-logging:2.0.11")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.6.3")

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "15"
    targetCompatibility = "15"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}