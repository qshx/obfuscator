plugins {
    `java`
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.24.2")
    implementation("org.apache.logging.log4j:log4j-api:2.24.2")


    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

application {
    mainClass.set("com.example.obfuscator.Main")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("encoding", "UTF-8")
    (options as StandardJavadocDocletOptions).addStringOption("charSet", "UTF-8")
}

tasks.test {
    useJUnitPlatform()
}
