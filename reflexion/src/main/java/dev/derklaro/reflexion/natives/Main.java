package dev.derklaro.reflexion.natives;

import dev.derklaro.reflexion.NativeHelper;

import java.lang.invoke.MethodHandles.Lookup;

public class Main {

  private static  boolean BOOL = false;

  public static void main(String[] args) throws Exception {
    NativeHelper.loadNative("reflexion.dll");

    Lookup impl = (Lookup) FNativeReflect.GetObjectFieldValue(Lookup.class.getName().replace('.', '/'), "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;", null);
    System.out.println(impl);

    System.out.println("-------");

    String a = World.class.getName().replace('.', '/');
    World world = new World("World", false, (byte) 1, 'g', (short) 5, 55, 555, 5555f, 55555d);

    System.out.println(FNativeReflect.GetObjectFieldValue(a, "str", "Ljava/lang/String;", world));
    System.out.println(FNativeReflect.GetZFieldValue(a, "b", world));
    System.out.println(FNativeReflect.GetBFieldValue(a, "by", world));
    System.out.println(FNativeReflect.GetCFieldValue(a, "c", world));
    System.out.println(FNativeReflect.GetSFieldValue(a, "s", world));
    System.out.println(FNativeReflect.GetIFieldValue(a, "i", world));
    System.out.println(FNativeReflect.GetJFieldValue(a, "l", world));
    System.out.println(FNativeReflect.GetFFieldValue(a, "f", world));
    System.out.println(FNativeReflect.GetDFieldValue(a, "d", world));

    // System.out.println("-------");

    FNativeReflect.SetObjectFieldValue(a, "str", "Ljava/lang/String;", world, "Hello World!");
    FNativeReflect.SetZFieldValue(a, "b", world, true);
    FNativeReflect.SetBFieldValue(a, "by", world, (byte) 2);
    FNativeReflect.SetCFieldValue(a, "c", world, 'a');
    FNativeReflect.SetSFieldValue(a, "s", world, (short) 7);
    FNativeReflect.SetIFieldValue(a, "i", world, 77);
    FNativeReflect.SetJFieldValue(a, "l", world, 777);
    FNativeReflect.SetFFieldValue(a, "f", world, 7777f);
    FNativeReflect.SetDFieldValue(a, "d", world, 77777d);

    System.out.println("-------");

    System.out.println(FNativeReflect.GetObjectFieldValue(a, "str", "Ljava/lang/String;", world));
    System.out.println(FNativeReflect.GetZFieldValue(a, "b", world));
    System.out.println(FNativeReflect.GetBFieldValue(a, "by", world));
    System.out.println(FNativeReflect.GetCFieldValue(a, "c", world));
    System.out.println(FNativeReflect.GetSFieldValue(a, "s", world));
    System.out.println(FNativeReflect.GetIFieldValue(a, "i", world));
    System.out.println(FNativeReflect.GetJFieldValue(a, "l", world));
    System.out.println(FNativeReflect.GetFFieldValue(a, "f", world));
    System.out.println(FNativeReflect.GetDFieldValue(a, "d", world));
  }
}
