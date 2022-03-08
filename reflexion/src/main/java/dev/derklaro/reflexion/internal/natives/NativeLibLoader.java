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

package dev.derklaro.reflexion.internal.natives;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import lombok.NonNull;

final class NativeLibLoader {

  private static final Os UNSUPPORTED_OS = new Os("unsupported", "", "");
  private static final String NATIVE_LIB_FILE_FORMAT = "reflexion-native/reflexion-%s_%s/%sreflexion.%s";

  private static final Os OS;
  private static final String OS_ARCH;

  static {
    // normalize the os name
    String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    if (osName.startsWith("linux") || osName.equals("netbsd")) {
      OS = new Os("linux", "lib", "so");
    } else if (osName.startsWith("windows")) {
      OS = new Os("windows", "", "dll");
    } else if (osName.startsWith("mac")) {
      OS = new Os("mac", "lib", "dylib");
    } else {
      OS = UNSUPPORTED_OS;
    }

    // normalize the cpu arch
    String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
    if (arch.equals("amd64") || arch.equals("x86_64")) {
      OS_ARCH = "x86_64";
    } else if (arch.startsWith("arm")) {
      OS_ARCH = "arm";
    } else if (arch.equals("aarch64")) {
      OS_ARCH = "aarch64";
    } else {
      OS_ARCH = "unsupported";
    }
  }

  private NativeLibLoader() {
    throw new UnsupportedOperationException();
  }

  public static boolean tryLoadNative() {
    // check if we can load a native lib
    if (OS == UNSUPPORTED_OS || OS_ARCH.equals("unsupported")) {
      return false;
    }

    // get the full name to the folder and file we should search for the lib, then try to load it
    String file = String.format(NATIVE_LIB_FILE_FORMAT, OS.name, OS_ARCH, OS.libPrefix, OS.libExtension);
    InputStream nativeLibStream = NativeLibLoader.class.getClassLoader().getResourceAsStream(file);

    // check if the lib for the environment was found
    if (nativeLibStream == null) {
      return false;
    }

    Path temp;
    try {
      // create a temp file at the target
      temp = Files.createTempFile("reflexion-", file.substring(file.lastIndexOf('.')));
      Files.copy(nativeLibStream, temp, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      // unable to create the temp file / write to it...
      return false;
    }

    // try to not leave crap on the file system
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        Files.deleteIfExists(temp);
      } catch (IOException exception) {
        // ok well, then live with it
      }
    }));

    try {
      // try to load the library
      System.load(temp.toAbsolutePath().toString());
      return true;
    } catch (Exception exception) {
      // unable to load, ignore
      return false;
    }
  }

  private static final class Os {

    private final String name;
    private final String libPrefix;
    private final String libExtension;

    public Os(@NonNull String name, @NonNull String libPrefix, @NonNull String libExtension) {
      this.name = name;
      this.libPrefix = libPrefix;
      this.libExtension = libExtension;
    }
  }
}
