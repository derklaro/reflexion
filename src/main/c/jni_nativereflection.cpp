#include "jni_util.h"
#include "jni_nativereflection.h"

JNIEXPORT jobject JNICALL Java_com_github_derklaro_reflexion_NativeReflection_getFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jstring signature
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return nullptr;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, signature, true);
  if (field == nullptr) return nullptr;

  return env->GetStaticObjectField(clazz, field);
}

JNIEXPORT void JNICALL Java_com_github_derklaro_reflexion_NativeReflection_setFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jstring signature,
  jobject value
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field we want to write to
  jfieldID field = GetFieldId(env, clazz, name, signature, true);
  if (field == nullptr) return;

  env->SetStaticObjectField(clazz, field, value);
}
