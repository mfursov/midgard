import com.moowork.gradle.node.yarn.YarnTask

plugins {
    id("com.moowork.node") version "1.2.0"
    java // to specify source/resources dirs for Intellij IDEA
}

node {
    download = true
    version = "10.5.0"
}

java.sourceSets {
    getByName("main").resources.srcDirs("resources")
    getByName("main").java.srcDirs("src")
}

tasks {
    val nodeInstall by creating(YarnTask::class) {
        args = listOf("install")
    }

    val nodeBuild by creating(YarnTask::class) {
        dependsOn(nodeInstall)
        args = listOf("run", "build")
    }

    val nodeClean by creating(YarnTask::class) {
        args = listOf("run", "clean")
    }

    "assemble" {
        dependsOn(nodeBuild)
    }

    "clean" {
        dependsOn(nodeClean)
    }
}

