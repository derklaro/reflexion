package dev.derklaro.reflexion.natives;

import dev.derklaro.reflexion.NativeHelper;

import java.lang.invoke.MethodHandles.Lookup;

public class Main {

  public static final Object WORLD = new Object();

  public static void main(String[] args) throws Exception {
    NativeHelper.loadNative("reflexion.dll");
    System.out.println(FNativeReflect.GetObjectFieldValue(Lookup.class.getName().replace('.', '/'), "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;", null));

    World world = new World("Hello from reflection based rust ;)");
    System.out.println(FNativeReflect.GetObjectFieldValue(World.class.getName().replace('.', '/'), "abc", "Ljava/lang/String;", world));
  }
}
