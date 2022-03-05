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
