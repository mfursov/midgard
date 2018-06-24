plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.koin:koin-core:0.9.3")

    compile(project(":actions"))
    compile(project(":core"))
    compile(project(":db"))
    compile(project(":events"))
    compile(project(":programs"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}
