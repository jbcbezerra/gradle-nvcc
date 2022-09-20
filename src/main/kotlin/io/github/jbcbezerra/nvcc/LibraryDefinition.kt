package io.github.jbcbezerra.nvcc

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

abstract class LibraryDefinition {

    /**
     * The stage up to which the input files must be compiled
     */
    @get:Input
    abstract val compilationPhase: Property<String>

    /**
     * The absolute path of the .cu file.
     */
    @get:Input
    abstract val cuFile: Property<String>
}