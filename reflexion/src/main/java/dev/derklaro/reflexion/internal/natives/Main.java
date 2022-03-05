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

import java.lang.invoke.MethodHandles.Lookup;

public class Main {

  private static boolean BOOL = false;

  public static void main(String[] args) throws Exception {
    if (!NativeLibLoader.tryLoadNative()) {
      throw new IllegalArgumentException("WHAT?");
    }

    Lookup impl = (Lookup) FNativeReflect.GetObjectFieldValue(Lookup.class.getName().replace('.', '/'), "IMPL_LOOKUP",
      "Ljava/lang/invoke/MethodHandles$Lookup;", null);
    System.out.println(impl);

    System.out.println("-------");

    String a = World.class.getName().replace('.', '/');
    World world = new World("World", false, (byte) 1, 'g', (short) 5, 55, 555, 5555f, 55555d, new int[]{1, 2, 3});

    System.out.println(FNativeReflect.GetObjectFieldValue(a, "str", "Ljava/lang/String;", world));
    System.out.println(FNativeReflect.GetZFieldValue(a, "b", world));
    System.out.println(FNativeReflect.GetBFieldValue(a, "by", world));
    System.out.println(FNativeReflect.GetCFieldValue(a, "c", world));
    System.out.println(FNativeReflect.GetSFieldValue(a, "s", world));
    System.out.println(FNativeReflect.GetIFieldValue(a, "i", world));
    System.out.println(FNativeReflect.GetJFieldValue(a, "l", world));
    System.out.println(FNativeReflect.GetFFieldValue(a, "f", world));
    System.out.println(FNativeReflect.GetDFieldValue(a, "d", world));
    System.out.println(FNativeReflect.GetObjectFieldValue(a, "arr", "[I", world));

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
