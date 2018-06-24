import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))
    compile(project(":actions"))
    compile(project(":core"))
    compile(project(":events"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}
