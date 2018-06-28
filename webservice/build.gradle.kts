import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm")
    war
}

kotlin {
    experimental {
        coroutines = Coroutines.ENABLE
    }
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.23.3")

    compile("io.ktor:ktor-server-core:0.9.3-alpha-5")
    compile("io.ktor:ktor-server-servlet:0.9.3-alpha-5")
    compile("io.ktor:ktor-websockets:0.9.3-alpha-5")

    compile("org.koin:koin-ktor:0.9.3")

    compile(project(":instance"))
    compile(project(":webcommon"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}

