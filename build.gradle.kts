plugins {
    base
    kotlin("jvm") version "1.2.50" apply false
}

allprojects {

    group = "midgard"

    version = "1.0"

    repositories {
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/ktor")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-js-wrappers")
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}
