val quarkusVersion: String = "1.8.0.Final"
val MaskModelVersion = "1.1.4-SNAPSHOT"
val MaskCacheVersion = "1.0.1-SNAPSHOT"
val MaskUtilVersion = "1.1.3-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.10"
    id("io.quarkus") version "1.8.0.Final"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.72"
    id("org.sonarqube") version "2.7"
    id("jacoco")
    `maven-publish`
}

group = "fr.convergence.proddoc"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}
// je mets ces 2 variables ici car je n'arrive pas à les mettre ailleurs
// (dans settings.gradle.kts par exemple)
val myMavenRepoUser = "myMavenRepo"
val myMavenRepoPassword = "mask"

repositories {
    mavenLocal()
    maven {
        url = uri("https://mymavenrepo.com/repo/OYRB63ZK3HSrWJfc2RIB/")
        credentials {
            username = myMavenRepoUser
            password = myMavenRepoPassword
        }
    }
    mavenCentral()
}

publishing {
    repositories {
        maven {
            url = uri("https://mymavenrepo.com/repo/ah37AFHxnt3Fln1mwTvi/")
            credentials {
                username = myMavenRepoUser
                password = myMavenRepoPassword
            }
        }
        mavenLocal()
    }

    publications {
        create<MavenPublication>("firecracker") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation("io.quarkus:quarkus-resteasy:$quarkusVersion")
    implementation("io.quarkus:quarkus-rest-client:$quarkusVersion")
    implementation("io.quarkus:quarkus-kafka-client:$quarkusVersion")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka:$quarkusVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    implementation("fr.convergence.proddoc.lib:mask-model:$MaskModelVersion")
    implementation("fr.convergence.proddoc.lib:mask-util:$MaskUtilVersion")
    implementation("fr.convergence.proddoc.lib:mask-cache:$MaskCacheVersion")

    testImplementation("io.quarkus:quarkus-junit5:$quarkusVersion")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.ws.rs.Path")
}
