import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30" apply false
    id("org.jetbrains.dokka") version "1.4.20"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://maven.kotlindiscord.com/repository/maven-snapshots/")
        maven(url = "https://maven.kotlindiscord.com/repository/maven-releases/")
    }

    if (this.path == ":") return@allprojects // fancy subprojects {}

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")

    group = "io.github.ytg1234.kordextbackgroundcat"

    dependencies {
        implementation(kotlin("stdlib"))

        // Discord
        implementation("dev.kord", "kord-core", "0.7.0-SNAPSHOT")
        implementation("com.kotlindiscord.kord.extensions", "kord-extensions", "1.4.0-RC6") {
            exclude(group = "dev.kord", module = "kord-core")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.useIR = true
    }
}

project(":ext-base") {
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "maven-publish")

    dependencies {
        implementation("org.slf4j", "slf4j-simple", "1.7.19")
        implementation("io.github.microutils", "kotlin-logging", "1.12.0")

        // Config
        implementation("com.uchuhimo", "konf-core", "1.0.0")
        implementation("com.uchuhimo", "konf-toml", "1.0.0")
    }

    tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
        dokkaSourceSets {
            named("main") {
                displayName.set("KordExt BackgroundCat - Base")
                includes.from("Module.md")
            }
        }
    }
}

project(":ext-defaults") {
    apply(plugin = "maven-publish")

    dependencies {
        implementation(project(":ext-base"))

        testImplementation(project(":ext-base"))
        testImplementation(sourceSets["main"].output)
    }

    tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
        dokkaSourceSets {
            named("main") {
                displayName.set("KordExt BackgroundCat - Default Processors")
                includes.from("Module.md")
            }
        }
    }
}

group = "io.github.ytg1234"
version = "1.0.0"

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            displayName.set("KordExt BackgroundCat")
            includes.from("Module.md")
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.8.2"
    distributionType = Wrapper.DistributionType.ALL
}
