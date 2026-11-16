plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.7")
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("src/main/resources"))
    }
}

application {
    mainClass.set("Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}
