package com.github.derklaro.reflexion;

import java.lang.invoke.MethodHandles.Lookup;

public class Main {

  public static final Object WORLD = new Object();

  public static void main(String[] args) throws Exception {
    NativeHelper.loadNative("native.dll");
    System.out.println(NativeReflection.getFieldValue(Lookup.class.getName().replace('.', '/'), "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;"));
  }
}
