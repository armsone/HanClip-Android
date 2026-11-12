pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HanClipAndroid"
include(":app")
include(":hanai-onnx")
project(":hanai-onnx").projectDir = File("../HanAI/android/hanai-onnx")
include(":hanai-core")
project(":hanai-core").projectDir = File("../HanAI/android/hanai-core")
