plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.20.0"
}

group = "io.github.jbcbezzera"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    // Gradle test kit using JUnit
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

java{
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
            implementationClass = "io.github.jbcbezerra.NvccPlugin"
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