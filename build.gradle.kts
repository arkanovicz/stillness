@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
// import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.dokka)
    signing
    `maven-publish`
    alias(libs.plugins.nexusPublish)
    alias(libs.plugins.versions)
}

group = "com.republicate"
version = "2.0-SNAPSHOT"
description = "Stillness reverse templating engine"

repositories {
    mavenCentral()
    mavenLocal()
}

buildscript {
    dependencies {
        classpath("com.strumenta:antlr-kotlin-gradle-plugin:1.0.5")
    }
}

val isRelease = project.hasProperty("release")
signing {
    isRequired = isRelease
    if (isRelease) {
        useGpgCmd()
        sign(publishing.publications)
    }
}

tasks {
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/arkanovicz/stillness")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("cbrisson")
                        name.set("Claude Brisson")
                        email.set("claude.brisson@gmail.com")
                        organization.set("republicate.com")
                        organizationUrl.set("https://republicate.com")
                    }
                }
                scm {
                    connection.set("scm:git@github.com/arkanovicz/stillness.git")
                    url.set("https://github.com/arkanovicz/stillness")
                }
            }

            artifact(tasks["dokkaJar"])
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            useStaging.set(true)
        }
    }
}

kotlin {
    // Simplified hierarchy - JVM only for now
    // applyDefaultHierarchyTemplate {
    //     common {
    //         group("commonJs") {
    //             withJs()
    //         }
    //     }
    // }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    // JS target disabled for now due to compiler issues
    // TODO: Re-enable once dependencies stabilize
    // js {
    //     nodejs()
    // }

    // Native targets disabled for now due to essential-kson ABI version mismatch
    // TODO: Re-enable once Kotlin versions align
    // linuxX64()
    // macosX64()
    // macosArm64()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    sourceSets {
        all {
            languageSettings.apply {
                languageVersion = "2.0"
                apiVersion = "2.0"
            }
        }

        commonMain {
            dependencies {
                api(libs.antlr.kotlin.runtime)
                api(libs.kson)
                implementation(libs.kotlin.logging)
                implementation(libs.coroutines.core)
            }
            kotlin.srcDir("build/generated-src/commonMain/kotlin")
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.core)
            }
        }
        jvmMain {
            dependencies {
                // Kotlin scripting for expression evaluation
                implementation(libs.kotlin.scripting.common)
                implementation(libs.kotlin.scripting.jvm)
                implementation(libs.kotlin.scripting.jvm.host)
                // Headless browser for JS-rendered pages
                implementation(libs.playwright)
                // Logging
                runtimeOnly(libs.slf4j.simple)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    // Native test resources handling - disabled until native targets re-enabled
    // val nativeTestResourcesPath = "${layout.buildDirectory.get()}/processedResources/native/test"
    // val copyNativeTestResources = project.tasks.register<Copy>("copyNativeTestResources") {
    //     from("src/commonTest/resources")
    //     into(nativeTestResourcesPath)
    // }
    // tasks.withType<KotlinNativeTest>().configureEach {
    //     dependsOn(copyNativeTestResources)
    //     workingDir = nativeTestResourcesPath
    // }
}

// ANTLR Kotlin code generation
val generateKotlinGrammarSource =
    tasks.register<com.strumenta.antlrkotlin.gradle.AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
        antlrClasspath = configurations.detachedConfiguration(
            project.dependencies.create("com.strumenta:antlr-kotlin-target:1.0.5")
        )
        packageName = "com.republicate.stillness.parser"

        arguments = listOf(
            "-Dlanguage=Kotlin", "-no-visitor", "-no-listener", "-encoding", "UTF-8"
        )
        source = project.objects.sourceDirectorySet("antlr", "antlr").srcDir("src/commonMain/antlr").apply {
            include("*.g4")
        }
        outputDirectory = File("build/generated-src/commonMain/kotlin")
        group = "code generation"
    }

val signingTasks = tasks.withType<Sign>()

tasks {
    // Tasks depending on code generation
    withType<KotlinCompilationTask<*>> {
        dependsOn(generateKotlinGrammarSource)
    }
    named<DokkaTask>("dokkaHtml") {
        dependsOn(generateKotlinGrammarSource)
    }

    // Tasks depending on signing
    withType<AbstractPublishToMaven>().configureEach {
        dependsOn(signingTasks)
    }

    // CLI task for Google Play stats
    register<JavaExec>("gpstats") {
        group = "application"
        description = "Scrape Google Play app stats"
        mainClass.set("com.republicate.stillness.cli.GooglePlayStats")
        classpath = kotlin.jvm().compilations["main"].runtimeDependencyFiles +
                    kotlin.jvm().compilations["main"].output.allOutputs
        args = project.findProperty("args")?.toString()?.split(" ") ?: listOf()
        dependsOn("jvmMainClasses")
    }

    // CLI task for Google Play search
    register<JavaExec>("gpsearch") {
        group = "application"
        description = "Search Google Play and list apps with stats"
        mainClass.set("com.republicate.stillness.cli.GooglePlaySearch")
        classpath = kotlin.jvm().compilations["main"].runtimeDependencyFiles +
                    kotlin.jvm().compilations["main"].output.allOutputs
        args = project.findProperty("args")?.toString()?.split(" ") ?: listOf()
        dependsOn("jvmMainClasses")
    }
}
