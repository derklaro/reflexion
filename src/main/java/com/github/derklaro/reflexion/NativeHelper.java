package com.github.derklaro.reflexion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeHelper {

  public static void loadNative(String path) throws IOException {
    var nativeLib = NativeHelper.class.getClassLoader().getResourceAsStream(path);
    if (nativeLib == null) {
      System.err.println("Unable to resolve");
      return;
    }

    var temp = Files.createTempFile("native-", path.substring(path.lastIndexOf('.')));
    Files.copy(nativeLib, temp, StandardCopyOption.REPLACE_EXISTING);

    System.load(temp.toAbsolutePath().toString());
  }
}
