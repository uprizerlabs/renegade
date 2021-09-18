import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    kotlin("jvm") version "1.5.30"
    java
}

group = "uprizerlabs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.30")
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

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("io.kotlintest:kotlintest:2.0.7")
    
  //  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "16"
    targetCompatibility = "16"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}