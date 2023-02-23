//
// This file is part of reflexion, licensed under the MIT License (MIT).
//
// Copyright (c) 2022-2023 Pasqual K., Aldin S. and contributors
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

use jni::{JNIEnv, objects::{JObject, JClass}};

/// Returns the value of the IMPL_LOOKUP field, throwing an exception if any error occurs 
/// rather than killing the jvm.
///
/// # Arguments
///
/// * `env` - the current jni environment (pointer to the current running jvm).
#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_internal_natives_FNativeReflect_GetImplLookup<'a>(mut env: JNIEnv<'a>, _ctx: JClass) -> JObject<'a> {
  // try to find the Lookup class
  let target_class = env.find_class("java/lang/invoke/MethodHandles$Lookup");
  if target_class.is_err() {
    throw_exception(env, "unable to find Lookup class");
    return JObject::null();
  }

  // try to get the IMPL_LOOKUP field value
  let field_value = env.get_static_field(target_class.unwrap(), "IMPL_LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;");
  if field_value.is_err() {
    throw_exception(env, "unable to get IMPL_LOOKUP field value");
    return JObject::null();
  }

  // ensure that the field is an object (which is always the case by default)
  let impl_lookup = field_value.unwrap().l();
  if impl_lookup.is_err() {
    throw_exception(env, "IMPL_LOOKUP field value is not an object");
    return JObject::null();
  }

  // return the field value
  impl_lookup.unwrap()
}

/// Clears all exceptions on the given environment and throws a ReflexionException
/// with the given message.
/// 
/// # Arguments
///
/// * `env`     - the current jni environment (pointer to the current running jvm).
/// * `message` - the message to pass to the exception.
pub fn throw_exception(mut env: JNIEnv, message: &str) {
  env.exception_clear()
    .and_then(|_| env.throw_new("dev/derklaro/reflexion/ReflexionException", message))
    .expect("unable to clear current and throw new exception");
}
