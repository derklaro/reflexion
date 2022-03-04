use jni::{JNIEnv, objects::{JClass, JString, JObject, JValue}};

#[no_mangle]
pub extern "system" fn Java_dev_derklaro_reflexion_natives_FNativeReflect_GetObjectFieldValue<'a>(env: JNIEnv<'a>, _ctx: JClass, target: JString, name: JString, signature: JString, on: JObject<'a>) -> JObject<'a> {
  let field_signature: String = env.get_string(signature).unwrap().into();
  let field_value = get_field_value(env, target, name, field_signature, on);

  field_value.l().expect("Not an object...")
}

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
  field_value.expect("unable to retreive field value")
}
