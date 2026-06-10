plugins {
    java
}

group = "work.siamnet"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // Gson ships with the server at runtime; declared here only for compilation.
    compileOnly("com.google.code.gson:gson:2.11.0")
    // PlaceholderAPI is optional at runtime (softdepend); only needed to compile the expansion.
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    filteringCharset = "UTF-8"
}
