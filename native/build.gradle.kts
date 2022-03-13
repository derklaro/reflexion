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

tasks.register("buildNative", Exec::class) {
  commandLine = listOf("cargo", "build", "--release")
  doLast {
    val fileNames = getNativeLibFiles()
    copy {
      from("target/release/${fileNames.second}")
      into("../reflexion/src/main/resources/reflexion-native/${fileNames.first}")
    }
  }
}

fun getNativeLibFiles(): Pair<String, String> {
  // normalize the os name
  val osName: String = System.getProperty("os.name").toLowerCase()
  val nameFormat: Pair<String, String> = if (osName.startsWith("linux") || osName == "netbsd") {
    Pair("reflexion-linux_%s", "libreflexion.so")
  } else if (osName.startsWith("windows")) {
    Pair("reflexion-windows_%s", "reflexion.dll")
  } else if (osName.startsWith("mac")) {
    Pair("reflexion-mac_%s", "libreflexion.dylib")
  } else {
    throw IllegalStateException("Unsupported operating system, cannot copy lib")
  }

  // normalize the cpu arch
  val arch: String = System.getProperty("os.arch").toLowerCase()
  return if (arch == "amd64" || arch == "x86_64") {
    nameFormat.copy(nameFormat.first.format("x86_64"))
  } else if (arch.startsWith("arm")) {
    nameFormat.copy(nameFormat.first.format("arm"))
  } else if (arch == "aarch64") {
    nameFormat.copy(nameFormat.first.format("aarch64"))
  } else {
    throw IllegalStateException("Unsupported system arch, cannot copy lib")
  }
}
