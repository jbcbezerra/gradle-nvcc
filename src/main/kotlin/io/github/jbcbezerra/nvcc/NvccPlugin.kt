package io.github.jbcbezerra.nvcc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class NvccPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // Create and register nvcc task
        target.tasks.create<NvccTask>("nvcc")
    }
}