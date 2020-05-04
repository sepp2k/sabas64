plugins {
    java
    kotlin("jvm") version "1.3.72"
    antlr
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    antlr("org.antlr:antlr4:4.8")
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
        arguments = arguments + listOf("-visitor", "-package", "sabas64")
    }
}