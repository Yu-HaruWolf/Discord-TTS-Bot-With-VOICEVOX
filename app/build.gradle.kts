/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/8.0.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven( 
        url = "https://m2.dv8tion.net/releases"
    )
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("net.dv8tion:JDA:5.0.0-beta.20")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

application {
    // Define the main class for the application.
    mainClass.set("com.github.yu_haruwolf.discord_tts_bot_with_voicevox.Bot")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "com.github.yu_haruwolf.discord_tts_bot_with_voicevox.Bot"
    }
}