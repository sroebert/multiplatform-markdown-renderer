import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish") version "0.25.3"
}

android {
    namespace = "com.mikepenz.markdown"
    compileSdk = Versions.androidCompileSdk

    defaultConfig {
        minSdk = Versions.androidMinSdk
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"

        kotlinOptions {
            if (project.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir.absolutePath}/compose_compiler"
                )
            }
            if (project.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir.absolutePath}/compose_compiler"
                )
            }
        }
    }
}

kotlin {
    targets.all {
        compilations.all {
            compilerOptions.configure {
                languageVersion.set(KOTLIN_1_8)
                apiVersion.set(KOTLIN_1_8)
            }
        }
    }

    androidTarget {
        publishLibraryVariants("release")
    }
}

dependencies {
    commonMainApi(Deps.Markdown.core)

    commonMainCompileOnly(compose.runtime)
    commonMainCompileOnly(compose.ui)
    commonMainCompileOnly(compose.foundation)
    commonMainCompileOnly(compose.material)

    "androidMainImplementation"(Deps.Compose.coilCompose)
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        configureEach {
            noAndroidSdkLink.set(false)
        }
    }
}

tasks.create<Jar>("javadocJar") {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
}

publishing {
    repositories {
        maven {
            name = "installLocally"
            setUrl("${rootProject.buildDir}/localMaven")
        }
    }
}