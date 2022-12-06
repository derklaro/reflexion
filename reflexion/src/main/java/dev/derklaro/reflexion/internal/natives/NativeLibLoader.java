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

import dev.derklaro.reflexion.Result;
import dev.derklaro.reflexion.internal.util.Exceptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import lombok.NonNull;

/**
 * The loader for the native library bundled with this library.
 *
 * @since 1.0
 */
final class NativeLibLoader {

  private static final boolean NATIVE_DISABLED = Boolean.getBoolean(
    Result.class.getPackage().getName() + ".native-disabled");

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

  /**
   * Tries to load the native library which is bundled with reflexion.
   *
   * @return true if the library was loaded successfully, false otherwise.
   */
  public static boolean tryLoadNative() {
    // check if we can load a native lib
    if (NATIVE_DISABLED || OS == UNSUPPORTED_OS || OS_ARCH.equals("unsupported")) {
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
        // ok well, just leave the file there then
      }
    }));

    try {
      // try to load the library
      System.load(temp.toAbsolutePath().toString());
      return true;
    } catch (Throwable throwable) {
      // unable to load, ignore
      Exceptions.rethrowIfFatal(throwable);
      return false;
    }
  }

  /**
   * Represents information about an operating system.
   *
   * @since 1.0
   */
  private static final class Os {

    private final String name;
    private final String libPrefix;
    private final String libExtension;

    /**
     * Constructs a new operating system info.
     *
     * @param name         the name of the operating system.
     * @param libPrefix    the prefix of native libraries on the operating system.
     * @param libExtension the extension of native libraries on the operating system.
     * @throws NullPointerException if the given name, lib prefix or suffix is null.
     */
    public Os(@NonNull String name, @NonNull String libPrefix, @NonNull String libExtension) {
      this.name = name;
      this.libPrefix = libPrefix;
      this.libExtension = libExtension;
    }
  }
}
