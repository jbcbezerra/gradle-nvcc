package io.github.jbcbezerra.nvcc

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.newInstance
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class NvccTask : DefaultTask(){

    /** The output directory in which the generated code will be placed. */
    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/sources/nvcc"))

    //TODO: default look for .cu files in resource and compile with ptx (v0.1.1)

    @get:Nested
    val definitions = ArrayList<LibraryDefinition>()

    init {
        group = "build"
    }

    private fun findExecutable(): Path {

        // Select appropriate executable for operating system
        val operatingSystem = OperatingSystem.current()
        if (operatingSystem.isWindows)
            throw GradleException("nvcc-plugin does currently not support Windows")
        val executable = "nvcc"

        // Search for nvcc in PATH
        val envPath = System.getenv(ENV_PATH)
        val pathExecutable = envPath
            .split(File.pathSeparator)
            .map { path -> Paths.get(path, executable) }
            .filter { path -> Files.exists(path) }

        try {
            return pathExecutable.first()
        } catch (exception: NoSuchElementException) {
            throw GradleException("nvcc binary could not be found in PATH")
        }
    }

    @TaskAction
    fun action(){
        val nvccBinary = findExecutable()
        if (Files.isDirectory(nvccBinary)) {
            throw GradleException("${nvccBinary} is not a regular file but a directory")
        }

        if (!Files.isExecutable(nvccBinary)) {
            throw GradleException("${nvccBinary} is not executable")
        }

        for (definition in definitions){

            // Initialize argument list
            val arguments = ArrayList<String>()

            // set phase and .cu file (mandatory)
            arguments += "-${definition.compilationPhase.get()}"
            arguments += definition.cuFile.get()

            // Set output directory
            arguments += "--output-directory"
            arguments += outputDir.get().toString()

            execute("${nvccBinary} ${arguments.joinToString(" ")}")
        }
    }

    fun cuFile(cuFile: String, action: Action<LibraryDefinition>) {
        val definition = project.objects.newInstance<LibraryDefinition>()
        definition.cuFile.set(cuFile)
        action.execute(definition)
        definitions += definition
    }

    private companion object {
        const val ENV_PATH = "PATH"

        private fun execute(command: String) {
            // Create buffers for stdout and stderr streams
            val stdout = StringBuffer()
            val stderr = StringBuffer()
            val result = Runtime.getRuntime().exec(command)

            // Wait until the process finishes and check if it succeeded
            result.await(stdout, stderr)
            if (result.exitValue() != 0) {
                throw GradleException(
                    "Invoking nvcc failed.\n\n" +
                            " command: ${command}\n stdout: ${stdout}\n stderr: ${stderr}"
                )
            }
        }

        fun Process.await(output: Appendable?, error: Appendable?) {
            val out = ProcessGroovyMethods.consumeProcessOutputStream(this, output)
            val err = ProcessGroovyMethods.consumeProcessErrorStream(this, error)
            try {
                try {
                    out.join()
                    err.join()
                    waitFor()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            } finally {
                ProcessGroovyMethods.closeStreams(this)
            }
        }
    }
}