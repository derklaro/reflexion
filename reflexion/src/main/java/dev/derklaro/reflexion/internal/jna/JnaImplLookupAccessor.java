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

package dev.derklaro.reflexion.internal.jna;

import com.sun.jna.FunctionMapper;
import com.sun.jna.JNIEnv;
import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * A IMPL_LOOKUP accessor which uses a misc of native jvm internals via jna and pre-generated getter code to get the
 * field value.
 *
 * @since 1.6.0
 */
final class JnaImplLookupAccessor {

  private static final int INITIAL_READ_BUFFER_SIZE = 512;

  private static final String JVM_LIB_NAME = "jvm";
  private static final String ACCESS_CLASS_INTERNAL_NAME = "java/lang/invoke/ImplLookupAccess";
  private static final String ACCESS_CLASS_JVM_NAME = ACCESS_CLASS_INTERNAL_NAME.replace('/', '.');

  private JnaImplLookupAccessor() {
    throw new UnsupportedOperationException();
  }

  /**
   * Tries to resolve the impl_lookup field. This method does not re-throw any exceptions when loading it simply returns
   * null if an error of some kinds occurs.
   *
   * @return the impl_lookup field value or null if an error occurred.
   */
  public static @Nullable Lookup tryGetImplLookup() {
    Class<?> ila;
    try {
      // load the stuff we need to access the lookup
      Jvm jvmAccess = loadJvmJnaAccess();
      byte[] accessClassBytecode = loadAccessClassByteCode();

      // define the class & get the access method
      ila = jvmAccess.JVM_DefineClass(
        JNIEnv.CURRENT,
        ACCESS_CLASS_INTERNAL_NAME,
        null,
        accessClassBytecode,
        accessClassBytecode.length,
        null);
    } catch (LinkageError error) {
      // check if this method was called twice on accident...
      try {
        // get the class if already defined - we don't care about initialization
        ila = Class.forName(ACCESS_CLASS_JVM_NAME);
      } catch (ClassNotFoundException exception) {
        return null;
      }
    } catch (Throwable throwable) {
      // something went wrong - just assume it's not available for some reason
      return null;
    }

    try {
      // get the access method & invoke it
      Method implLookupGetter = ila.getMethod("getImplLookup");
      return (Lookup) implLookupGetter.invoke(null);
    } catch (ReflectiveOperationException exception) {
      // should not happen, but fine
      return null;
    }
  }

  /**
   * Loads the bytes of the pre-generated class file to access the impl_lookup field.
   *
   * @return the bytecode of the pre-generated ImplLookupAccess class.
   * @throws IOException if an i/o error occurs while loading the file content.
   */
  private static byte[] loadAccessClassByteCode() throws IOException {
    try (ByteArrayOutputStream target = new ByteArrayOutputStream(INITIAL_READ_BUFFER_SIZE);
      InputStream in = JnaImplLookupAccessor.class.getClassLoader().getResourceAsStream("jna/ImplLookupAccess.class")) {
      // this should not happen unless the file was excluded
      if (in == null) {
        throw new IllegalStateException("LookupAccess class is missing");
      }

      // copy the content of the input stream into the output stream
      int transferredBytes;
      byte[] readBuffer = new byte[INITIAL_READ_BUFFER_SIZE];
      while ((transferredBytes = in.read(readBuffer)) != -1) {
        target.write(readBuffer, 0, transferredBytes);
      }

      // convert the stream to the byte array
      return target.toByteArray();
    }
  }

  /**
   * Loads the jvm native interface and associates it with the jvm interface to allow the native class define calls we
   * need. This method enables function mapping for Windows x32 systems as well.
   *
   * @return a bridge to the jvm native interface to allow class define calls.
   */
  private static @NonNull Jvm loadJvmJnaAccess() {
    Map<String, Object> loadOptions = new HashMap<>();
    loadOptions.put(Library.OPTION_ALLOW_OBJECTS, true);
    // enable our special function mapper for win32
    if (Platform.isWindows() && !Platform.is64Bit()) {
      loadOptions.put(Library.OPTION_FUNCTION_MAPPER, new Win32FunctionMapper());
    }

    // load the lib
    return Native.load(JVM_LIB_NAME, Jvm.class, loadOptions);
  }

  /**
   * A JNA library interface which wraps the jvm {@code JVM_DefineClass} method.
   *
   * @since 1.6.0
   */
  interface Jvm extends Library {

    /**
     * Defines a new class based on the given byte code of it.
     *
     * @param env              the jni environment.
     * @param name             the internal name of the class to define.
     * @param classLoader      the class loader to inject into, null represents the bootstrap class loader.
     * @param bytecode         the bytecode of the class to define.
     * @param length           the length of the bytecode.
     * @param protectionDomain the protection domain, null to use no specific protection domain.
     * @return the defined class.
     * @throws LastErrorException if an error occurs while injecting the class.
     */
    // CHECKSTYLE.OFF: Must match native naming
    Class<?> JVM_DefineClass(
      JNIEnv env,
      String name,
      ClassLoader classLoader,
      byte[] bytecode,
      int length,
      ProtectionDomain protectionDomain
    ) throws LastErrorException;
    // CHECKSTYLE.ON
  }

  /**
   * A function name mapper for the native {@code JVM_DefineClass} method. This is only applied on Windows x32 platforms
   * as function names are mapped differently there.
   *
   * @since 1.6.0
   */
  private static final class Win32FunctionMapper implements FunctionMapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFunctionName(@NonNull NativeLibrary library, @NonNull Method method) {
      // only look for "JVM_DefineClass" - we don't care about other methods
      if (method.getName().equals("JVM_DefineClass")) {
        return "_JVM_DefineClass@24";
      } else {
        return method.getName();
      }
    }
  }
}
