package dev.derklaro.reflexion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeHelper {

  public static void loadNative(String path) throws IOException {
    InputStream nativeLib = NativeHelper.class.getClassLoader().getResourceAsStream(path);
    if (nativeLib == null) {
      System.err.println("Unable to resolve");
      return;
    }

    Path temp = Files.createTempFile("native-", path.substring(path.lastIndexOf('.')));
    Files.copy(nativeLib, temp, StandardCopyOption.REPLACE_EXISTING);

    System.load(temp.toAbsolutePath().toString());
  }
}
