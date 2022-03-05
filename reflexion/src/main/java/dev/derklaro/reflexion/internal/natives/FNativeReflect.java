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

final class FNativeReflect {

  // read field access
  public static native Object GetObjectFieldValue(String owner, String name, String sig, Object on);

  public static native boolean GetZFieldValue(String owner, String name, Object on);

  public static native byte GetBFieldValue(String owner, String name, Object on);

  public static native char GetCFieldValue(String owner, String name, Object on);

  public static native short GetSFieldValue(String owner, String name, Object on);

  public static native int GetIFieldValue(String owner, String name, Object on);

  public static native long GetJFieldValue(String owner, String name, Object on);

  public static native float GetFFieldValue(String owner, String name, Object on);

  public static native double GetDFieldValue(String owner, String name, Object on);

  // field write access
  public static native void SetObjectFieldValue(String owner, String name, String sig, Object on, Object val);

  public static native void SetZFieldValue(String owner, String name, Object on, boolean val);

  public static native void SetBFieldValue(String owner, String name, Object on, byte val);

  public static native void SetCFieldValue(String owner, String name, Object on, char val);

  public static native void SetSFieldValue(String owner, String name, Object on, short val);

  public static native void SetIFieldValue(String owner, String name, Object on, int val);

  public static native void SetJFieldValue(String owner, String name, Object on, long val);

  public static native void SetFFieldValue(String owner, String name, Object on, float val);

  public static native void SetDFieldValue(String owner, String name, Object on, double val);
}
