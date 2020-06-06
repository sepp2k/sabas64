plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.70"
    antlr
    application
    id("org.sonarqube") version "3.0"
    jacoco
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    antlr("org.antlr:antlr4:4.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

application {
    mainClassName = "org.sabas64.MainKt"
}

sonarqube {
  properties {
    property("sonar.projectKey", "sepp2k_sabas64")
    property("sonar.organization", "sepp2k")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        mustRunAfter("generateGrammarSource")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    generateGrammarSource {
        arguments = arguments + listOf("-visitor", "-package", "org.sabas64")
    }
    test {
        useJUnitPlatform()
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.isEnabled = false
    }
}

tasks.sonarqube {
    dependsOn(tasks.jacocoTestReport)
}
