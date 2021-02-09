pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        mavenCentral()
    }
}

rootProject.name = "backgroundcat-kotlin"

include("base", "defaults", "test")
