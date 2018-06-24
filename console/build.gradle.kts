plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))

    compile(project(":instance"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}
