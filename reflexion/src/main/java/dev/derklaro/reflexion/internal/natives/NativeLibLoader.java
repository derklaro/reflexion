package dev.derklaro.reflexion.internal.natives;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

final class NativeLibLoader {

  private static final Os UNSUPPORTED_OS = new Os("unsupported", "", "");

  private static final Os OS;
  private static final String OS_ARCH;

  static {
    // normalize the os name
    String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    if (osName.startsWith("linux")) {
      OS = new Os("linux", "lib", "so");
    } else if (osName.equals("netbsd")) {
      OS = new Os("netbsd", "lib", "so");
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

  public static boolean tryLoadNative() throws IOException {
    // check if we can load a native lib
    if (OS == UNSUPPORTED_OS || OS_ARCH.equals("unsupported")) {
      return false;
    }

    // get the full name to the folder and file we should search for the lib, then try to load it
    String file = String.format("reflexion-%s_%s/%sreflexion.%s", OS.name, OS_ARCH, OS.libPrefix, OS.libExtension);
    InputStream nativeLibStream = NativeLibLoader.class.getClassLoader().getResourceAsStream(file);

    // check if the lib for the environment was found
    if (nativeLibStream == null) {
      return false;
    }

    // create a temp file at the target
    Path temp = Files.createTempFile("reflexion-", file.substring(file.lastIndexOf('.')));
    Files.copy(nativeLibStream, temp, StandardCopyOption.REPLACE_EXISTING);

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
