plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    application
}

// Allows running from command line using  ./gradlew :main-project:run
application {
    mainClass.set("academy.kt.MainKt")
}

// Makes generated code visible to IDE
kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
    )
}

dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
}
