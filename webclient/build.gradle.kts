import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.yarn.YarnTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("platform.js")
    id("com.moowork.node") version "1.2.0"
    //id("com.liferay.node") version "4.3.3"
}

dependencies {
    compile(kotlin("stdlib-js"))
    "expectedBy"(project(":webcommon"))
}


node {
    download = true
    version = "10.5.0"
}

val mainSourceSet = the<JavaPluginConvention>().sourceSets["main"]!!
tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            outputFile = "${mainSourceSet.output.resourcesDir}/webclient.js"
            sourceMap = true
            moduleKind = "umd"
        }
    }

    val unpackKotlinJsStdlib by creating {
        group = "build"
        description = "Unpack the Kotlin JavaScript standard library"
        val outputDir = file("$buildDir/$name")
        val compileClasspath = configurations["compileClasspath"]
        inputs.property("compileClasspath", compileClasspath)
        outputs.dir(outputDir)
        doLast {
            val kotlinStdLibJar = compileClasspath.single {
                it.name.matches(Regex("kotlin-stdlib-js-.+\\.jar"))
            }
            copy {
                includeEmptyDirs = false
                from(zipTree(kotlinStdLibJar))
                into(outputDir)
                include("**/*.js")
                exclude("META-INF/**")
            }
        }
    }

    // Copies files from src/main/resources to build/dist. These resources will be served by dev server:
    val copyStaticResources by creating(Copy::class) {
        from(mainSourceSet.resources)
        into("$buildDir/dist")
    }

    val yarnInstall by creating(YarnTask::class) {
        args = listOf("install")
    }

    @Suppress("UNUSED_VARIABLE")
    val run by creating(YarnTask::class) {
        dependsOn(yarnInstall, copyStaticResources, unpackKotlinJsStdlib)
        args = listOf("run", "start")
    }

    val jsBundle by creating(NpmTask::class) {
        dependsOn(yarnInstall, copyStaticResources, unpackKotlinJsStdlib)
        setArgs(listOf("run", "bundle"))
    }

    "assemble" {
        dependsOn(jsBundle)
    }
}

