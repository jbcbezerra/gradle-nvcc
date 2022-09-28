package io.github.jbcbezerra.nvcc

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class LibraryDefinition {

    /**
     * The absolute path where the .cu files to compile are located
     */
    @get:Input
    abstract val cuPath: Property<String>

    /**
     * A list of .cu files to include into the compilation. At deafult all
     * cufiles defined in {@code cuPath} will be compiled
     */
    @get:Optional
    @get:Input
    abstract val cuFiles: ListProperty<String>

    /**
     * The stage up to which the input files must be compiled
     */
    @get:Input
    abstract val compilationPhase: Property<String>

}