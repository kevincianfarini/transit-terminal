plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {

    compilerOptions { allWarningsAsErrors.set(true) }
    explicitApi()

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
            implementation(libs.clikt)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
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