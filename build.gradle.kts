plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.1-jre")

    implementation ("io.socket:socket.io-client:2.0.1") {
        exclude ("org.json', module: 'json")
    }

    implementation ("com.google.code.gson:gson:2.8.9")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

configure<SourceSetContainer>
{
    named("main")
    {
        java.srcDir("src")
    }
}