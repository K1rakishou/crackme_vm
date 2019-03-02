plugins {
    kotlin("multiplatform") version "1.3.21"
}

repositories {
    mavenCentral()
}

kotlin {
    // For ARM, preset function should be changed to iosArm32() or iosArm64()
    // For Linux, preset function should be changed to e.g. linuxX64()
    // For MacOS, preset function should be changed to e.g. macosX64()
    mingwX64("crackme") {
        binaries {
            // Comment the next section to generate Kotlin/Native library (KLIB) instead of executable file:
            executable("crackme") {
                // Change to specify fully qualified name of your application's entry point:
                entryPoint = "crackme.main"
            }
        }
    }
}

// Use the following Gradle tasks to run your application:
// :runHelloWorldAppReleaseExecutableHelloWorld - without debug symbols
// :runHelloWorldAppDebugExecutableHelloWorld - with debug symbols
