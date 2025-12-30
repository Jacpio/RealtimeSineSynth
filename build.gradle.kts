plugins {
    id("java")
    application
}

group = "pl.jacpio"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.formdev:flatlaf:3.1")
}
java {
    toolchain{
        languageVersion = JavaLanguageVersion.of(17)
    }
}
application {
    mainClass.set("pl.jacpio.RealtimeSineSynth")
}
tasks.test {
    useJUnitPlatform()
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "pl.jacpio.RealtimeSineSynth"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}