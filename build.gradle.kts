plugins {
    // Apply the Java Gradle Plugin Development plugin
    id("java-gradle-plugin")

    // Apply the Kotlin DSL Plugin for enhanced IDE support
    `kotlin-dsl`

    // Apply the Plugin Publishing Plugin to publish plugins to the Gradle Plugins Portal
    id("com.gradle.plugin-publish") version "0.20.0"
    id("maven-publish")
}

group = "io.github.jbcbezerra"
version = "0.1.4"

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(kotlin("bom")))

    // Use the Kotlin JDK 8 standard library
    implementation(kotlin("stdlib-jdk8"))

    // Gradle test kit using JUnit
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

java{
    // explicitly target Java 11
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin{
    target{
        compilations.configureEach{
            kotlinOptions{
                jvmTarget = "11"
            }
        }
    }
}

gradlePlugin {
    plugins {
        create("nvcc") {
            id = "io.github.jbcbezerra.nvcc"
            displayName = "nvcc gradle plugin"
            description = "Integrates nvcc with the Gradle build system"
            implementationClass = "io.github.jbcbezerra.nvcc.NvccPlugin"
        }
    }
}

pluginBundle{
    website = "https://github.com/jbcbezerra/gradle-nvcc"
    vcsUrl = "https://github.com/jbcbezerra/gradle-nvcc.git"
    tags = listOf("native","cuda","nvcc")
}

tasks.withType<Wrapper> {
    gradleVersion = "7.5"
}

tasks.withType<Test>().configureEach{
    useJUnitPlatform()
}