/*
 * This file is part of reflexion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022 Pasqual K., Aldin S. and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

plugins {
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

allprojects {
  version = "1.4.0"
  group = "dev.derklaro.reflexion"
}

subprojects {
  apply(plugin = "signing")
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")

  tasks.register<org.gradle.jvm.tasks.Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.getByName("javadoc"))
  }

  tasks.register<org.gradle.jvm.tasks.Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(project.the<SourceSetContainer>()["main"].allJava)
  }

  extensions.configure<PublishingExtension> {
    publications.apply {
      create("maven", MavenPublication::class.java).apply {
        from(components.getByName("java"))

        artifact(tasks.getByName("sourcesJar"))
        artifact(tasks.getByName("javadocJar"))

        pom.apply {
          name.set(project.name)
          url.set("https://github.com/derklaro/reflexion")
          description.set("The fluent reflection access library of (yo)ur dreams")

          developers {
            developer {
              id.set("derklaro")
              email.set("git@derklaro.dev")
              timezone.set("Europe/Berlin")
            }
          }

          licenses {
            license {
              name.set("MIT")
              url.set("https://opensource.org/licenses/MIT")
            }
          }

          scm {
            tag.set("HEAD")
            url.set("git@github.com:derklaro/reflexion.git")
            connection.set("scm:git:git@github.com:derklaro/reflexion.git")
            developerConnection.set("scm:git:git@github.com:derklaro/reflexion.git")
          }

          issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/derklaro/reflexion/issues")
          }

          ciManagement {
            system.set("GitHub Actions")
            url.set("https://github.com/derklaro/reflexion/actions")
          }

          withXml {
            val repositories = asNode().appendNode("repositories")
            project.repositories.forEach {
              if (it is MavenArtifactRepository && it.url.toString().startsWith("https://")) {
                val repo = repositories.appendNode("repository")
                repo.appendNode("id", it.name)
                repo.appendNode("url", it.url.toString())
              }
            }
          }
        }
      }
    }
  }

  extensions.configure<SigningExtension> {
    useGpgCmd()
    sign(extensions.getByType(PublishingExtension::class.java).publications.getByName("maven"))
  }

  tasks.withType<Sign> {
    onlyIf {
      !rootProject.version.toString().endsWith("-SNAPSHOT")
    }
  }
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

      username.set(project.findProperty("ossrhUsername") as? String ?: "")
      password.set(project.findProperty("ossrhPassword") as? String ?: "")
    }
  }

  useStaging.set(!project.version.toString().endsWith("-SNAPSHOT"))
}
