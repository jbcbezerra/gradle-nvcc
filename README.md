This is a Gradle plugin for integrating Cuda's [`nvcc`](https://docs.nvidia.com/cuda/cuda-compiler-driver-nvcc/index.html) compiler in the build process.
## :bulb: &nbsp; Example

Since the plugin is available on [Gradle's Plugin Portal](https://plugins.gradle.org/) it can be applied within the build script's `plugins` block.

```gradle
plugins {
  id "io.github.jbcbezerra.nvcc" version "0.1.2"
}
```

Applying the plugin adds the `nvcc` task which has to be configured by the build script.

```gradle
nvcc {
    cuFile("${project.projectDir}/src/main/resources/vec_add.cu") {
        // The compile phase
        compilationPhase = ptx
    }
}
```

The plugin will try to find `nvcc` inside `PATH` (usually in `usr/bin` directory)

## :triangular_ruler: &nbsp; Configuration Options

The `nvcc` task currently exposes the following configuration options.

|        Name        |        Type        | Required | Description                                           |
|:------------------:|:------------------:|:--------:|-------------------------------------------------------|
|      `cuFile`      | `java.lang.String` |    X     | The absolute path of the .cu file.                    |
| `compilationPhase` | `java.lang.String` |    X     | The stage up to which the input files must be compiled |
|       `...`        |       `...`        |          | ...                                                   |
|       `...`        |       `...`        |          | ...                                                   |

In the future, even further parameters are to be included accordingly. (see [here](https://helpmanual.io/help/nvcc/))

## :wrench: &nbsp; Requirements

* [OpenJDK 11](https://openjdk.org/projects/jdk/11/) - We recommend using [SDKMAN](https://sdkman.io/)! for installation
* [NVCC Compiler](https://docs.nvidia.com/cuda/cuda-compiler-driver-nvcc/index.html) - Under Ubuntu 20.04 a simple installation via `sudo apt install nvidia-cuda-toolkit` is sufficient.