plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
    google()
}

kotlin {

    compilerOptions { allWarningsAsErrors.set(true) }
    explicitApi()

    jvm()
    linuxArm64 {
        binaries {
            executable {
                entryPoint = "io.github.kevincianfarini.grtc.main"
            }
        }
    }
    linuxX64 {
        binaries {
            executable {
                entryPoint = "io.github.kevincianfarini.grtc.main"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.cardiologist)
            implementation(libs.clikt)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor)
            implementation(libs.mosaic)
            implementation(libs.mosaic.animation)
            implementation(libs.okio)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}