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
    val defaultOutputDir: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/sources/nvcc"))

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

    private fun getCuFiles(path: String): ArrayList<String> {
        val cuFilesTemp = ArrayList<String>()
        if (!Files.isDirectory(Path.of(path)))
            throw GradleException("${path} is not a directory")

        File(path).walk().forEach {
            cuFilesTemp += it.absolutePath
        }

        val cuFiles = ArrayList<String>()
        cuFilesTemp.forEach {
            if (it.endsWith(".cu"))
                cuFiles += it
        }

        return cuFiles
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

            val allCuFiles = getCuFiles(definition.cuPath.get())
            val cuFilesToCompile = ArrayList<String>()

            if (!definition.cuFiles.get().isEmpty()) {
                println(definition.cuFiles.get())

                for (whitelistCuFile in definition.cuFiles.get()) {
                    var found = false
                    for (cuFile in allCuFiles) {
                        if (cuFile.endsWith(whitelistCuFile)) {
                            cuFilesToCompile += cuFile
                            found = true
                            break
                        }
                    }
                    if (!found)
                        throw GradleException("Can't find ${whitelistCuFile} in ${definition.cuPath}.")
                }
            }else{
                allCuFiles.forEach{
                    cuFilesToCompile += it
                }
            }

            for (cuFile in cuFilesToCompile){
                // Initialize argument list
                val arguments = ArrayList<String>()

                // set phase and .cu file (mandatory)
                arguments += "-${definition.compilationPhase.get()}"
                arguments += cuFile

                // Set output directory
                arguments += "--output-directory"
                arguments += defaultOutputDir.get().toString()

                execute("${nvccBinary} ${arguments.joinToString(" ")}")
            }
        }
    }

    fun cuPath(cuPath: String, action: Action<LibraryDefinition>) {
        val definition = project.objects.newInstance<LibraryDefinition>()
        definition.cuPath.set(cuPath)
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