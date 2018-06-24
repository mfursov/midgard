import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}

//TODO: tasks {
//    withType<Test> {
//        testLogging.debug.events(STARTED, PASSED, FAILED, STANDARD_OUT, STANDARD_ERROR)
//    }
//}