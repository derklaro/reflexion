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

package dev.derklaro.reflexion.helper;

public final class SeedClass extends SeedSuperClass {

  private static final String WORLD = "World";
  private static final long LONG = 123456789L;

  private final int i;
  private final double d;
  private final boolean b;
  private final String str;

  private SeedClass() {
    this(234D, "");
  }

  private SeedClass(double d, String str) {
    this(123, d, true, str);
  }

  public SeedClass(int i, double d, boolean b, String str) {
    this(i, d, b, str, "");
  }

  public SeedClass(int i, double d, boolean b, String str, String s) {
    this.i = i;
    this.d = d;
    this.b = b;
    this.str = str;
  }

  public static String abc() {
    return "World";
  }

  private static String abc(String a, SeedClass b) {
    return a + " // " + b.getStr();
  }

  public int getI() {
    return this.i;
  }

  public double getD() {
    return this.d;
  }

  public boolean isB() {
    return this.b;
  }

  public String getStr() {
    return this.str;
  }

  public String appendToStr(String other) {
    return this.str + " " + other;
  }
}
