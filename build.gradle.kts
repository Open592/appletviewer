import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    base
    application
    kotlin("jvm")

    alias(libs.plugins.kotlinter)
}

application {
    applicationDefaultJvmArgs = listOf(
        "-Dcom.jagex.debug=${System.getProperty("com.jagex.debug")}",
        "-Dcom.open592.debugConsoleLogToSystemStream=${System.getProperty("com.open592.debugConsoleLogToSystemStream")}"
    )

    mainClass.set("com.open592.appletviewer.cmd.Main")
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

    implementation(libs.kotlin.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.mockk)
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
