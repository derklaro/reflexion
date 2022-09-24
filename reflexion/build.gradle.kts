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
  id("jacoco")
  id("checkstyle")
  id("me.champeau.jmh") version "0.6.7"
  id("org.cadixdev.licenser") version "0.6.1"
}

repositories {
  mavenCentral()
}

dependencies {
  // lombok
  val lombokVersion = "1.18.24"
  compileOnly("org.projectlombok", "lombok", lombokVersion)
  annotationProcessor("org.projectlombok", "lombok", lombokVersion)

  // jna (optional - only used if present at runtime)
  val jnaVersion = "5.12.1"
  compileOnly("net.java.dev.jna", "jna", jnaVersion)
  testRuntimeOnly("net.java.dev.jna", "jna", jnaVersion)

  // other libs
  val annotationsVersion = "23.0.0"
  compileOnly("org.jetbrains", "annotations", annotationsVersion)

  // testing
  val junitVersion = "5.9.0"
  testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
  testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion)
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)

  // jmh
  val jmhVersion = "1.35"
  jmh("org.openjdk.jmh", "jmh-core", jmhVersion)
  jmh("org.openjdk.jmh", "jmh-generator-annprocess", jmhVersion)
}

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_1_8.toString()
  targetCompatibility = JavaVersion.VERSION_1_8.toString()
  // options
  options.encoding = "UTF-8"
  options.isIncremental = true
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events("started", "passed", "skipped", "failed")
  }

  finalizedBy(tasks.getByName("jacocoTestReport"))
}

tasks.withType<JacocoReport> {
  reports {
    // disable xml & csv reports
    xml.required.set(false)
    csv.required.set(false)

    // enable html reporting
    html.required.set(true)
    html.outputLocation.set(layout.buildDirectory.dir("jacocoReport"))
  }
}

tasks.withType<Checkstyle> {
  maxErrors = 0
  maxWarnings = 0
  configFile = rootProject.file("checkstyle.xml")
}

tasks.withType<Javadoc> {
  val options = options as? StandardJavadocDocletOptions ?: return@withType

  // options
  options.encoding = "UTF-8"
  options.memberLevel = JavadocMemberLevel.PRIVATE
  options.addStringOption("-html5")
}

extensions.configure<JacocoPluginExtension> {
  toolVersion = "0.8.8"
}

extensions.configure<CheckstyleExtension> {
  toolVersion = "10.3.3"
}

extensions.configure<org.cadixdev.gradle.licenser.LicenseExtension> {
  include("**/*.java")
  header(rootProject.file("license_header.txt"))
}
