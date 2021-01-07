import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    application
}

group = "io.github.ytg1234"
version = "1.0"

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
    jcenter()
    maven(url = "https://maven.kotlindiscord.com/repository/maven-snapshots/")
    maven(url = "https://maven.kotlindiscord.com/repository/maven-releases/")
}

dependencies {
    implementation(kotlin("stdlib"))

    // Discord
    implementation("dev.kord", "kord-core", "0.7.0-SNAPSHOT")
    implementation("com.kotlindiscord.kord.extensions", "kord-extensions", "1.4.0-RC6") {
        exclude(group = "dev.kord", module = "kord-core")
    }

    implementation("org.slf4j", "slf4j-simple", "1.7.19")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("io.github.ytg1234.backgroundcatkotlin.BotKt")
}
