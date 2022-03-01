package com.github.derklaro.reflexion;

final class NativeReflection {

  public static native Object getFieldValue(String className, String name, String signature);

  public static native void setFieldValue(String className, String name, String signature, Object val);
}
