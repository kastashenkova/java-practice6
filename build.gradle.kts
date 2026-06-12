plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.2")

    testImplementation("org.junit.platform:junit-platform-suite-engine:1.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.0.2")
}

tasks.test {
    useJUnitPlatform()
}