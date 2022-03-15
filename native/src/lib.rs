//
// This file is part of reflexion, licensed under the MIT License (MIT).
//
// Copyright (c) 2022 Pasqual K., Aldin S. and contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

use jni::{JNIEnv, objects::{JClass, JString, JObject, JValue}, sys::{jboolean, jbyte, jchar, jshort, jint, jlong, jfloat, jdouble}};

// --------------------------------
// signature types
// --------------------------------

const INT_TYPE: &str = "I";
const BOOL_TYPE: &str = "Z";
const BYTE_TYPE: &str = "B";
const CHAR_TYPE: &str = "C";
const LONG_TYPE: &str = "J";
const SHORT_TYPE: &str = "S";
const FLOAT_TYPE: &str = "F";
const DOUBLE_TYPE: &str = "D";

// --------------------------------
// field get access
// --------------------------------

/// Returns the value of the given field. This method does not support getting 
/// a field with a primitive type, use the other functions provided in this lib
/// for that purpose instead.
///
/// # Arguments
///
/// * `env`       - the current jni environment (pointer to the current running jvm).
/// * `_ctx`      - the contextual static class which called the method.
/// * `target`    - the fully qualified name of the class the field to get is located in.
/// * `name`      - the name of the field to get.
/// * `signature` - the object field sigature, for example: `Ljava/lang/String;`.
/// * `on`        - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is representing a primitive type like int rather than an object.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetObjectFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, signature: JString, on: JObject<'a>) -> JObject<'a> {
  let field_signature: String = env.get_string(signature).unwrap().into();
  let field_value = get_field_value(env, target, name, field_signature, on);

  field_value.l().expect("expected an object")
}

/// Returns the boolean value of the given field. This method only
/// supports getting the value of a boolean field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a boolean.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetZFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> bool {
  let field_value = get_field_value(env, target, name, BOOL_TYPE.to_string(), on);
  field_value.z().expect("expected a boolean")
}

/// Returns the byte value of the given field. This method only
/// supports getting the value of a byte field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a byte.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetBFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jbyte {
  let field_value = get_field_value(env, target, name, BYTE_TYPE.to_string(), on);
  field_value.b().expect("expected a byte")
}

/// Returns the char value of the given field. This method only
/// supports getting the value of a char field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a char.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetCFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jchar {
  let field_value = get_field_value(env, target, name, CHAR_TYPE.to_string(), on);
  field_value.c().expect("expected a char")
}

/// Returns the short value of the given field. This method only
/// supports getting the value of a short field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a short.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetSFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jshort {
  let field_value = get_field_value(env, target, name, SHORT_TYPE.to_string(), on);
  field_value.s().expect("expected a short")
}

/// Returns the int value of the given field. This method only
/// supports getting the value of an int field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing an int.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetIFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jint {
  let field_value = get_field_value(env, target, name, INT_TYPE.to_string(), on);
  field_value.i().expect("expected an int")
}

/// Returns the long value of the given field. This method only
/// supports getting the value of a long field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a long.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetJFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jlong {
  let field_value = get_field_value(env, target, name, LONG_TYPE.to_string(), on);
  field_value.j().expect("expected a long")
}

/// Returns the float value of the given field. This method only
/// supports getting the value of a float field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a float.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetFFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jfloat {
  let field_value = get_field_value(env, target, name, FLOAT_TYPE.to_string(), on);
  field_value.f().expect("expected a float")
}

/// Returns the double value of the given field. This method only
/// supports getting the value of a double field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a double.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetDFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>) -> jdouble {
  let field_value = get_field_value(env, target, name, DOUBLE_TYPE.to_string(), on);
  field_value.d().expect("expected a double")
}

// --------------------------------
// field set access
// --------------------------------

/// Sets the value of the given field. This method does not support setting
/// a field with a primitive type, use the other functions provided in this lib
/// for that purpose instead.
///
/// # Arguments
///
/// * `env`       - the current jni environment (pointer to the current running jvm).
/// * `_ctx`      - the contextual static class which called the method.
/// * `target`    - the fully qualified name of the class the field to get is located in.
/// * `name`      - the name of the field to get.
/// * `signature` - the signature of the field to update.
/// * `on`        - the instance of the object to get the field for, null if getting a static field.
/// * `val`       - the new value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing an object.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetObjectFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, signature: JString, on: JObject<'a>, val: JObject<'a>) {
  let field_signature: String = env.get_string(signature).unwrap().into();
  set_field_value(env, target, name, field_signature, on, JValue::Object(val));
}

/// Sets the boolean value of the given field. This method only
/// supports setting the value of a boolean field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new boolean value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a boolean.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetZFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jboolean) {
  set_field_value(env, target, name, BOOL_TYPE.to_string(), on, JValue::Bool(val));
}

/// Sets the byte value of the given field. This method only
/// supports setting the value of a byte field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new byte value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a byte.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetBFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jbyte) {
  set_field_value(env, target, name, BYTE_TYPE.to_string(), on, JValue::Byte(val));
}

/// Sets the char value of the given field. This method only
/// supports setting the value of a char field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new char value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a char.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetCFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jchar) {
  set_field_value(env, target, name, CHAR_TYPE.to_string(), on, JValue::Char(val));
}

/// Sets the short value of the given field. This method only
/// supports setting the value of a short field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new short value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a short.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetSFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jshort) {
  set_field_value(env, target, name, SHORT_TYPE.to_string(), on, JValue::Short(val));
}

/// Sets the int value of the given field. This method only
/// supports setting the value of an int field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new int value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing an int.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetIFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jint) {
  set_field_value(env, target, name, INT_TYPE.to_string(), on, JValue::Int(val));
}

/// Sets the long value of the given field. This method only
/// supports setting the value of a long field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new long value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a long.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetJFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jlong) {
  set_field_value(env, target, name, LONG_TYPE.to_string(), on, JValue::Long(val));
}

/// Sets the float value of the given field. This method only
/// supports setting the value of a float field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new float value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a float.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetFFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jfloat) {
  set_field_value(env, target, name, FLOAT_TYPE.to_string(), on, JValue::Float(val));
}

/// Sets the double value of the given field. This method only
/// supports setting the value of a double field.
///
/// # Arguments
///
/// * `env`    - the current jni environment (pointer to the current running jvm).
/// * `_ctx`   - the contextual static class which called the method.
/// * `target` - the fully qualified name of the class the field to get is located in.
/// * `name`   - the name of the field to get.
/// * `on`     - the instance of the object to get the field for, null if getting a static field.
/// * `val`    - the new double value of the field to set.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///  * if the given field is not representing a double.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_SetDFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, on: JObject<'a>, val: jdouble) {
  set_field_value(env, target, name, DOUBLE_TYPE.to_string(), on, JValue::Double(val));
}

// --------------------------------
// internal helper methods
// --------------------------------

/// Returns the value of the given field.
///
/// # Arguments
///
/// * `env`       - the current jni environment (pointer to the current running jvm).
/// * `target`    - the fully qualified name of the class the field to get is located in.
/// * `name`      - the name of the field to get.
/// * `signature` - the signature of the field to get.
/// * `on`        - the instance of the object to get the field for, null if getting a static field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
pub fn get_field_value<'a>(env: JNIEnv<'a>, target: JString, name: JString, signature: String, on: JObject<'a>) -> JValue<'a> {
  // convert given field information from java into rust types
  let field_name: String = env.get_string(name).unwrap().into();
  let field_owner: String = env.get_string(target).unwrap().into();

  // get the field information from the provided information
  let field_value = if on.is_null() {
    let target_class = env.find_class(field_owner).expect("invalid target class given");
    env.get_static_field(target_class, field_name, signature)
  } else {
    env.get_field(on, field_name, signature)
  };

  // ensure that we got the field
  field_value.expect("unable to retreive field value")
}

/// Sets the value of the given field.
///
/// # Arguments
///
/// * `env`       - the current jni environment (pointer to the current running jvm).
/// * `target`    - the fully qualified name of the class the field to get is located in.
/// * `name`      - the name of the field to get.
/// * `signature` - the signature of the field to get.
/// * `on`        - the instance of the object to get the field for, null if getting a static field.
/// * `value`     - the new value of the given field.
///
/// # Panics
///
/// This function panics if it is either
///  * unable to convert the given jstrings into their rust representation.
///  * unable to resolve the given target class.
///  * if no field with the given name and/or signature was found.
///
/// # Safety
///
/// This method shouldn't be called directly. The wrapping java api will ensure that the method
/// parameters supplied to the method will not let it panic and crash your jvm. Calling directly
/// is **very** unsupported.
pub fn set_field_value<'a>(env: JNIEnv<'a>, target: JString, name: JString, signature: String, on: JObject<'a>, value: JValue<'a>) {
    // convert given field information from java into rust types
    let field_name: String = env.get_string(name).unwrap().into();
    let field_owner: String = env.get_string(target).unwrap().into();

    // set the provided field based on the given information
    let result = if on.is_null() {
      let target_class = env.find_class(field_owner).expect("invalid target class given");
      let field_id = env.get_static_field_id(target_class, field_name, signature).unwrap();
      env.set_static_field(target_class, field_id, value)
    } else {
      env.set_field(on, field_name, signature, value)
    };

    // ensure that we were able to do the field change
    result.expect("unable to set field value");
}
