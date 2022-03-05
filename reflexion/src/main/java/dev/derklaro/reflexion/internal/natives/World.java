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

public class World {

  private final String str;
  private final boolean b;
  private final byte by;
  private final char c;
  private final short s;
  private final int i;
  private final long l;
  private final float f;
  private final double d;
  private final int[] arr;

  public World(String str, boolean b, byte by, char c, short s, int i, long l, float f, double d, int[] arr) {
    this.str = str;
    this.b = b;
    this.by = by;
    this.c = c;
    this.s = s;
    this.i = i;
    this.l = l;
    this.f = f;
    this.d = d;
    this.arr = arr;
  }

  public String getStr() {
    return this.str;
  }

  public boolean isB() {
    return this.b;
  }

  public byte getBy() {
    return this.by;
  }

  public char getC() {
    return this.c;
  }

  public short getS() {
    return this.s;
  }

  public int getI() {
    return this.i;
  }

  public long getL() {
    return this.l;
  }

  public float getF() {
    return this.f;
  }

  public double getD() {
    return this.d;
  }
}
