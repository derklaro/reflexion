plugins {
  id("java-library")
}

group = "com.github.derklaro"
version = "1.0-SNAPSHOT"

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_17.toString()
  targetCompatibility = JavaVersion.VERSION_17.toString()
  // options
  options.encoding = "UTF-8"
  options.isIncremental = true
}
