plugins {
    kotlin("jvm") version "1.3.70"
}

group = "np.com.madanpokharel"
version = "1.0-SNAPSHOT"


allprojects {
   apply(plugin = "kotlin")
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("com.typesafe.akka", "akka-actor_2.13", "2.6.3")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }

        test {
            useJUnitPlatform()
        }
    }
}

