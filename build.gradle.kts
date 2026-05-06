import dev.kordex.gradle.plugins.kordex.DataCollection
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    alias(libs.plugins.kotlin)
	alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.git.hooks)
	alias(libs.plugins.kord.extensions.plugin)
}

group = "io.github.nocomment1105.onidassistant"
version = "0.2.1"

val className = "io.github.nocomment1105.onidassistant.OnidAssistantKt"
val javaVersion = 21

repositories {
	mavenCentral()

	maven {
		name = "Kord Extensions (Releases)"
		url = uri("https://releases-repo.kordex.dev")
	}

	maven {
		name = "Kord Extensions (Snapshots)"
		url = uri("https://snapshots-repo.kordex.dev")
	}

	maven {
		name = "Kord Snapshots"
		url = uri("https://repo.kordex.dev/snapshots")
	}

	maven {
		name = "Kord Mirror"
		url = uri("https://mirror-repo.kordex.dev")
	}
}

dependencies {
    detektPlugins(libs.detekt)

    // Kord Extensions
    implementation(libs.kord.extensions.core)
    implementation(libs.kord.extensions.unsafe)

    implementation(libs.kotlin.stdlib)

    // Logging Deps
	implementation(libs.logback)
	implementation(libs.logging)

	// Database
	implementation(libs.mongodb)
	implementation(libs.mongodb.driverkx)
	implementation(libs.bsonkx)

	implementation(libs.ktor.auth)
}

distributions {
	main {
		distributionBaseName = project.name

		contents {
			from("LICENSE")
			exclude("README.md")
		}
	}
}

kordEx {
	addDependencies = false
	addRepositories = false
	kordExVersion = libs.versions.kord.extensions
	ignoreIncompatibleKotlinVersion = true

	bot {
		dataCollection(DataCollection.None)
	}
}


application {
    mainClass.set(className)
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
            languageVersion.set(KotlinVersion.fromVersion(libs.plugins.kotlin.get().version.requiredVersion.substringBeforeLast(".")))
            incremental = true
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

	java {  // Should match the Kotlin compiler options ideally
		sourceCompatibility = JavaVersion.toVersion(javaVersion)
		targetCompatibility = JavaVersion.toVersion(javaVersion)
	}

    jar {
        manifest {
            attributes("Main-Class" to className)
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.BIN
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/detekt.yml")

    autoCorrect = true
}
