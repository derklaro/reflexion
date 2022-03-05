plugins {
  id("java-library")
}

repositories {
  mavenCentral()
}

dependencies {
  // lombok
  val lombokVersion = "1.18.22"
  compileOnly("org.projectlombok", "lombok", lombokVersion)
  annotationProcessor("org.projectlombok", "lombok", lombokVersion)

  // other libs
  compileOnly("org.jetbrains:annotations:23.0.0")

  // testing
  val junitVersion = "5.8.2"
  testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_1_8.toString()
  targetCompatibility = JavaVersion.VERSION_1_8.toString()
  // options
  options.encoding = "UTF-8"
  options.isIncremental = true
}
