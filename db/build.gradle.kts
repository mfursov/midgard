plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib"))

    compile("com.github.mprops:mprops:1.0.0")

    compile(project(":actions"))
    compile(project(":core"))
    compile(project(":events"))
    compile(project(":json"))

    testCompile(kotlin("test"))
    testCompile("junit:junit:4.12")
}
