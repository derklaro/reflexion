package dev.derklaro.reflexion.natives;

import dev.derklaro.reflexion.NativeHelper;

import java.lang.invoke.MethodHandles.Lookup;

public class Main {

  public static final Object WORLD = new Object();

  public static void main(String[] args) throws Exception {
    NativeHelper.loadNative("native.dll");
    System.out.println(FNativeReflect.GetObjectFieldValue(Lookup.class.getName().replace('.', '/'), "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;", null));
    System.out.println(FNativeReflect.GetIFieldValue(Integer.class.getName().replace('.', '/'), "MAX_VALUE", null));

    Abc abc = new Abc("Hello World!");
    System.out.println(FNativeReflect.GetObjectFieldValue(Abc.class.getName().replace('.', '/'), "abc", "Ljava/lang/String;", abc));

    FNativeReflect.SetObjectFieldValue(Abc.class.getName().replace('.', '/'), "abc", "Ljava/lang/String;", abc, "World!");
    System.out.println(abc.abc());
  }
}
