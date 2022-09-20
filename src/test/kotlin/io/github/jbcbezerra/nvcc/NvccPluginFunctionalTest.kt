package io.github.jbcbezerra.nvcc

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class NvccPluginFunctionalTest {
    @TempDir
    private lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle").apply {
            writeText("""
                rootProject.name = 'nvcc'
            """.trimIndent())
        }
        buildFile = File(testProjectDir, "build.gradle").apply {
            writeText("""
            plugins {
                id 'io.github.jbcbezerra.nvcc'
            }
            nvcc{
                cuFile("/home/joao/IdeaProjects/gradle-nvcc/src/test/resources/test.cu"){
                    compilationPhase = "ptx"
                }
            }
            """.trimIndent())
        }
    }

    @Test
    fun `test whether nvcc task succeeds`() {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("nvcc")
            .withPluginClasspath()
            .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":nvcc")?.outcome)
    }
}