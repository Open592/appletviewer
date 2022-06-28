import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    base
    application
    kotlin("jvm")

    alias(libs.plugins.kotlinter)
}

application {
    mainClass.set("com.open592.appletviewer.cmd.AppletViewerCommandKt")
}

kotlinter {
    experimentalRules = true
}

group = "com.open592"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.guice)

    testImplementation(kotlin("test"))
}

plugins.withType<KotlinPluginWrapper> {
    apply(plugin = "org.jmailen.kotlinter")

    kotlin {
        explicitApi()
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
