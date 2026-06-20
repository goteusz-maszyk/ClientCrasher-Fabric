plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val minecraft_version: String by project

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("${minecraft_version}.build.+")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion(minecraft_version)
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf("version" to version, "minecraft_version" to minecraft_version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
