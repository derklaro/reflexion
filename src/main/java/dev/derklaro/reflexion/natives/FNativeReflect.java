package dev.derklaro.reflexion.natives;

final class FNativeReflect {

  // read field access
  public static native Object GetObjectFieldValue(String owner, String name, String sig, Object on);

  public static native boolean GetZFieldValue(String owner, String name, Object on);

  public static native byte GetBFieldValue(String owner, String name, Object on);

  public static native char GetCFieldValue(String owner, String name, Object on);

  public static native short GetSFieldValue(String owner, String name, Object on);

  public static native int GetIFieldValue(String owner, String name, Object on);

  public static native long GetLFieldValue(String owner, String name, Object on);

  public static native float GetFFieldValue(String owner, String name, Object on);

  public static native double GetDFieldValue(String owner, String name, Object on);

  // field write access
  public static native void SetObjectFieldValue(String owner, String name, String sig, Object on, Object val);

  public static native void SetZFieldValue(String owner, String name, Object on, boolean val);

  public static native void SetBFieldValue(String owner, String name, Object on, byte val);

  public static native void SetCFieldValue(String owner, String name, Object on, char val);

  public static native void SetSFieldValue(String owner, String name, Object on, short val);

  public static native void SetIFieldValue(String owner, String name, Object on, int val);

  public static native void SetLFieldValue(String owner, String name, Object on, long val);

  public static native void SetFFieldValue(String owner, String name, Object on, float val);

  public static native void SetDFieldValue(String owner, String name, Object on, double val);
}
