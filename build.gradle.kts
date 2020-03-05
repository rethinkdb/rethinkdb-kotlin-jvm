import java.util.Properties
import java.io.File
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask

plugins {
    kotlin("jvm") version "1.3.70"
    maven
    `maven-publish`
    signing
    id("com.jfrog.bintray") version "1.8.4"
}

version = "2.4.1.1"
group = "com.rethinkdb"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    compile("com.rethinkdb:rethinkdb-driver:2.4.1")
}

file("confidential.properties").takeIf(File::exists)?.let {
    val properties = Properties()
    it.inputStream().use(properties::load)
    allprojects { properties.forEach { name, value -> extra.set(name.toString(), value) } }
}

gradle.taskGraph.whenReady {
    val hasUploadArchives = hasTask(":uploadArchives")
    val hasBintrayUpload = hasTask(":bintrayUpload")
    val hasDoSigning = hasTask(":doSigning")
    signing.isRequired = hasBintrayUpload || hasUploadArchives || hasDoSigning
}

fun findProperty(s: String) = project.findProperty(s) as String?
tasks {
    val doSigning by creating {
        dependsOn("signArchives")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    val sourcesJar by creating(Jar::class) {
        group = "build"
        description = "Generates a jar with the sources"
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by creating(Jar::class) {
        group = "build"
        description = "Generates a jar with the javadoc"
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }

    getByName<Upload>("uploadArchives") {
        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    beforeDeployment { signing.signPom(this) }

                    withGroovyBuilder {
                        "repository"("url" to uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")) {
                            "authentication"(
                                "userName" to findProperty("ossrhUsername"),
                                "password" to findProperty("ossrhPassword")
                            )
                        }
                        "snapshotRepository"("url" to uri("https://oss.sonatype.org/content/repositories/snapshots/")) {
                            "authentication"(
                                "userName" to findProperty("ossrhUsername"),
                                "password" to findProperty("ossrhPassword")
                            )
                        }
                    }

                    pom.project {
                        withGroovyBuilder {
                            "name"("RethinkDB Kotlin JVM extensions")
                            "packaging"("jar")
                            "description"("Kotlin extensions for the RethinkDB Java driver\n")
                            "url"("http://rethinkdb.com")

                            "scm" {
                                "connection"("scm:git:https://github.com/rethinkdb/rethinkdb-kotlin-jvm")
                                "developerConnection"("scm:git:https://github.com/rethinkdb/rethinkdb-kotlin-jvm")
                                "url"("https://github.com/rethinkdb/rethinkdb-kotlin-jvm")
                            }

                            "licenses" {
                                "license" {
                                    "name"("The Apache License, Version 2.0")
                                    "url"("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                }
                            }

                            "developers" {
                                "developer" {
                                    "id"("adriantodt")
                                    "name"("Adrian Todt")
                                    "email"("adriantodt.ms@gmail.com")
                                }
                                "developer" {
                                    "id"("gabor-boros")
                                    "name"("Gábor Boros")
                                    "email"("gabor@rethinkdb.com")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    publishing {
        publications.create("mavenJava", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(project.components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom.withXml {
                val root = asNode()
                root.appendNode("name", "RethinkDB Kotlin JVM extensions")
                root.appendNode("packaging", "jar")
                root.appendNode("description", "Kotlin extensions for the RethinkDB Java driver\n")
                root.appendNode("url", "http://rethinkdb.com")

                val scm = root.appendNode("scm")
                scm.appendNode("connection","scm:git:https://github.com/rethinkdb/rethinkdb-kotlin-jvm")
                scm.appendNode("developerConnection","scm:git:https://github.com/rethinkdb/rethinkdb-kotlin-jvm")
                scm.appendNode("url", "https://github.com/rethinkdb/rethinkdb-kotlin-jvm")

                val license = root.appendNode("licenses").appendNode("license")
                license.appendNode("name","The Apache License, Version 2.0")
                license.appendNode("url","http://www.apache.org/licenses/LICENSE-2.0.txt")

                val developers = root.appendNode("developers")

                val dev1 = developers.appendNode("developer")
                dev1.appendNode("id","adriantodt")
                dev1.appendNode("name","Adrian Todt")
                dev1.appendNode("email","adriantodt.ms@gmail.com")

                val dev2 = developers.appendNode("developer")
                dev2.appendNode("id","gabor-boros")
                dev2.appendNode("name","Gábor Boros")
                dev2.appendNode("email","gabor@rethinkdb.com")
            }
        }
    }

    withType<BintrayUploadTask> {
        dependsOn("assemble", "publishToMavenLocal")
    }
}

signing {
    // Don't sign unless this is a release version
    sign(configurations.archives.get())
    sign(publishing.publications.get("mavenJava"))
}

bintray {
    user = findProperty("bintray.user")
    key = findProperty("bintray.key")
    publish = true
    setPublications("mavenJava")

    filesSpec(delegateClosureOf<RecordingCopyTask> {
        into("com/rethinkdb/${project.name}/${project.version}/")

        from("${buildDir}/libs/") {
            include("*.jar.asc")
        }

        from("${buildDir}/publications/mavenJava/") {
            include("pom-default.xml.asc")
            rename("pom-default.xml.asc", "${project.name}-${project.version}.pom.asc")
        }
    })

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = project.name
        userOrg = "rethinkdb"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/rethinkdb/rethinkdb-kotlin-jvm.git"
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            mavenCentralSync(delegateClosureOf<BintrayExtension.MavenCentralSyncConfig> {
                user = findProperty("ossrhUsername")
                password = findProperty("ossrhPassword")
                sync = !user.isNullOrBlank() && !password.isNullOrBlank()
            })
        })
    })
}
