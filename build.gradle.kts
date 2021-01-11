import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("org.jetbrains.dokka") version "1.4.20"
}

group = "io.github.ytg1234"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
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
    implementation("io.github.microutils", "kotlin-logging", "1.12.0")

    // Config
    implementation("com.uchuhimo", "konf-core", "0.23.0")
    implementation("com.uchuhimo", "konf-toml", "0.23.0")

    // TESTING
    testImplementation(sourceSets["main"].output)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            displayName.set("BackgroundCat Kotlin")
            includes.from("Module.md")
        }
    }
}
