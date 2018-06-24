plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))
    compile(project(":core"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}
