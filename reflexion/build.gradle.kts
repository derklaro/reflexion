plugins {
  id("java-library")
}

repositories {
  mavenCentral()
}

dependencies {
  // lombok
  compileOnly("org.projectlombok:lombok:1.18.22")
  annotationProcessor("org.projectlombok:lombok:1.18.22")

  // other libs
  compileOnly("org.jetbrains:annotations:23.0.0")
}

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_1_8.toString()
  targetCompatibility = JavaVersion.VERSION_1_8.toString()
  // options
  options.encoding = "UTF-8"
  options.isIncremental = true
}
