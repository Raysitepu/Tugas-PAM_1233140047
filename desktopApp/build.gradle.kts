import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    testImplementation("org.jetbrains.compose.ui:ui-test:1.12.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.4.20")
}

tasks.test {
    systemProperty("java.awt.headless", "true")
    systemProperty("profile.screenshot.dir", rootProject.file("docs/screenshots").absolutePath)
    testLogging { events("passed", "failed", "skipped") }
}

compose.desktop {
    application {
        mainClass = "com.itera.myprofile.MainKt"
        nativeDistributions {
            packageName = "MyProfileApp"
            packageVersion = "1.0.0"
        }
    }
}
