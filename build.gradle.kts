import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    application
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.ktlint.gradle)
}

application {
    applicationDefaultJvmArgs = listOf(
        "-Dcom.jagex.debug=${System.getProperty("com.jagex.debug")}",
        "-Dcom.open592.disableDebugConsole=${System.getProperty("com.open592.disableDebugConsole")}",
        "-Dcom.open592.debugConsoleLogToSystemStream=${
            System.getProperty("com.open592.debugConsoleLogToSystemStream")
        }",
        "-Dcom.jagex.config=${System.getProperty("com.jagex.config")}",
        "-Dcom.open592.launcherDirectoryOverride=${System.getProperty("com.open592.launcherDirectoryOverride")}",
        "-Dcom.open592.fakeThawtePublicKey=${System.getProperty("com.open592.fakeThawtePublicKey")}",
        "-Dcom.open592.fakeJagexPublicKey=${System.getProperty("com.open592.fakeJagexPublicKey")}",
    )

    mainClass.set("com.open592.appletviewer.cmd.Main")
}

group = "com.open592"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.guice)

    implementation(libs.kotlin.coroutines.core)
    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.memoryfilesystem)
    testImplementation(libs.mockk)
    testImplementation("com.squareup.okhttp3:mockwebserver")
}

plugins.withType<KotlinPluginWrapper> {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    kotlin {
        explicitApi()
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.50.0")
    enableExperimentalRules.set(true)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
