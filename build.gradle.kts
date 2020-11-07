import kotlinx.validation.ApiValidationExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintCheckTask
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

plugins {
    kotlin("jvm") version "1.3.72"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.jetbrains.dokka") version "1.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
    idea
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.2.3")
    }
}
apply(plugin = "binary-compatibility-validator")
configure<ApiValidationExtension> {}

group = "lt.petuska"
version = "0.0.6"

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven("https://dl.bintray.com/mpetuska/lt.petuska")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
}

dependencies {
    api(kotlin("gradle-plugin", "1.4.10"))
    testImplementation("io.kotest:kotest-runner-junit5:4.1.0")
}

gradlePlugin {
    plugins {
        create(project.name) {
            id = "lt.petuska.npm.publish"
            displayName = "NPM package publishing to NPM repositories"
            description =
                """
              A maven-publish alternative for NPM package publishing.      
              Integrates with kotlin JS/MPP plugins (if applied) to automatically 
              setup publishing to NPM repositories for all JS targets.
                """.trimIndent()
            implementationClass = "lt.petuska.npm.publish.NpmPublishPlugin"
        }
    }
}

pluginBundle {
    website = "https://gitlab.com/${project.group}/${project.name}/-/wikis/home"
    vcsUrl = "https://gitlab.com/${project.group}/${project.name}"
    tags = listOf("npm", "publishing", "kotlin", "node")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    withType<KtlintCheckTask> {
        dependsOn("ktlintFormat")
    }
    test {
        useJUnitPlatform()
    }
    val functionalTest by registering(Test::class) {
        testClassesDirs = functionalTestSourceSet.output.classesDirs
        classpath = functionalTestSourceSet.runtimeClasspath
        useJUnitPlatform()
        group = "verification"
    }
    named("check") {
        dependsOn(functionalTest)
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

val gitCommitHash by lazy {
    ByteArrayOutputStream().use { os ->
        exec {
            commandLine("git", "rev-parse", "HEAD")
            standardOutput = os
        }
        os.toString().trim()
    }
}

publishing {
    fun checkAnyTrue(vararg props: String) = props.any {
        "true".equals(project.properties[it] as String?, true)
    }

    fun checkNone(vararg props: String) = props.none {
        project.hasProperty(it)
    }
    publications {
        repositories {
            fun repository(name: String, config: MavenArtifactRepository.() -> Unit) {
                if (checkAnyTrue("publish.all", "publish.$name") || checkNone("publish.$name.skip")) {
                    maven {
                        this.name = name
                        config()
                    }
                }
            }
            repository("GitLab") {
                url = uri(
                    "https://gitlab.com/api/v4/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven"
                )
                credentials(HttpHeaderCredentials::class) {
                    val jobToken = System.getenv("CI_JOB_TOKEN")
                    if (jobToken != null) {
                        // GitLab CI
                        name = "Job-Token"
                        value = jobToken
                    } else {
                        name = "Private-Token"
                        value = System.getenv("PRIVATE_TOKEN")
                    }
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
            repository("Bintray") {
                url = uri(
                    "https://api.bintray.com/maven/${System.getenv("BINTRAY_USER")}/${project.group}/${project.name}/" +
                        ";publish=${if ("true".equals(project.properties["publish"] as? String?, true)) 1 else 0}" +
                        ";override=${if ("true".equals(project.properties["override"] as? String?, true)) 1 else 0}"
                )
                credentials {
                    username = System.getenv("BINTRAY_USER")
                    password = System.getenv("BINTRAY_KEY")
                }
            }
        }
    }
}

afterEvaluate {
    tasks {
        withType<Wrapper> {
            gradleVersion = "6.6.1"
            distributionType = Wrapper.DistributionType.ALL
        }
        withType<Jar> {
            manifest {
                attributes += sortedMapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Build-Jdk" to System.getProperty("java.version"),
                    "Implementation-Version" to project.version,
                    "Created-By" to org.gradle.util.GradleVersion.current(),
                    "Created-From" to gitCommitHash
                )
            }
        }
        val lib = project
        val publish by getting

        register("gitLabRelease") {
            group = publish.group!!

            doFirst {
                fun buildPackageLink(prj: Project) =
                    """
          {
            "name": "${prj.name}",
            "url": "https://bintray.com/${System.getenv("BINTRAY_USER")!!}/${prj.group}/${prj.name}/${prj.version}",
            "link_type": "package"
          }
                    """.trimIndent()

                val url = URL("https://gitlab.com/api/v4/projects/${System.getenv("CI_PROJECT_ID")!!}/releases")
                val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                con.setRequestProperty("Content-Type", "application/json")
                con.setRequestProperty("Authorization", "Bearer ${System.getenv("PRIVATE_TOKEN")!!}")
                con.requestMethod = "POST"
                con.doOutput = true

                val changelog = projectDir.resolve("CHANGELOG.MD")
                    .readText()
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                con.outputStream.use {
                    it.write(
                        """
            {
              "name": "Release v${lib.version}",
              "tag_name": "v${lib.version}",
              "ref": "$gitCommitHash",
              "assets": {
                  "links": [
                      ${setOf(lib).joinToString(",", transform = ::buildPackageLink)}
                  ]
              },
              "description": "$changelog"
            }
                        """.trimIndent().toByteArray()
                    )
                }
                val responseBody = BufferedReader(
                    InputStreamReader(con.inputStream, "utf-8")
                ).use { br ->
                    val response = StringBuilder()
                    var responseLine: String?
                    while (br.readLine().also { responseLine = it } != null) {
                        response.append(responseLine!!.trim { it <= ' ' })
                    }
                    println(response.toString())
                }
                val responseStatus = con.responseCode
                println(responseStatus)
                println(responseBody)
                con.disconnect()
                if (con.responseCode >= 400) throw GradleException("Invalid GitLab response. StatusCode: $responseStatus, message: $responseBody")
            }
        }
    }
}
