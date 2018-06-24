plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))

    compile(project(":core"))
    compile(project(":events"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}
