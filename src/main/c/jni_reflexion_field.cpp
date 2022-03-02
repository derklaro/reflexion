#include "jni_util.h"
#include "jni_reflexion_field.h"

JNIEXPORT jobject JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetObjectFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jstring signature,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return nullptr;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, signature, on == nullptr);
  if (field == nullptr) return nullptr;

  return on == nullptr ? env->GetStaticObjectField(clazz, field) : env->GetObjectField(on, field);
}

JNIEXPORT jboolean JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetZFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "Z", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticBooleanField(clazz, field) : env->GetBooleanField(on, field);
}

JNIEXPORT jbyte JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetBFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "B", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticByteField(clazz, field) : env->GetByteField(on, field);
}

JNIEXPORT jchar JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetCFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "C", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticCharField(clazz, field) : env->GetCharField(on, field);
}

JNIEXPORT jshort JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetSFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "S", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticShortField(clazz, field) : env->GetShortField(on, field);
}

JNIEXPORT jint JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetIFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "I", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticIntField(clazz, field) : env->GetIntField(on, field);
}

JNIEXPORT jlong JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetLFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "L", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticLongField(clazz, field) : env->GetLongField(on, field);
}

JNIEXPORT jfloat JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetFFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "F", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticFloatField(clazz, field) : env->GetFloatField(on, field);
}

JNIEXPORT jdouble JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_GetDFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return JNI_FALSE;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "D", on == nullptr);
  if (field == nullptr) return JNI_FALSE;

  return on == nullptr ? env->GetStaticDoubleField(clazz, field) : env->GetDoubleField(on, field);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetObjectFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jstring signature,
  jobject on,
  jobject newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, signature, on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticObjectField(clazz, field, newValue);
  else
    env->SetObjectField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetZFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jboolean newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "Z", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticBooleanField(clazz, field, newValue);
  else
    env->SetBooleanField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetBFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jbyte newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "B", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticByteField(clazz, field, newValue);
  else
    env->SetByteField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetCFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jchar newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "C", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticCharField(clazz, field, newValue);
  else
    env->SetCharField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetSFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jshort newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "S", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticShortField(clazz, field, newValue);
  else
    env->SetShortField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetIFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jint newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "I", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticIntField(clazz, field, newValue);
  else
    env->SetIntField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetLFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jlong newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "L", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticLongField(clazz, field, newValue);
  else
    env->SetLongField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetFFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jfloat newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "F", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticFloatField(clazz, field, newValue);
  else
    env->SetFloatField(on, field, newValue);
}

JNIEXPORT void JNICALL Java_dev_derklaro_reflexion_natives_FNativeReflect_SetDFieldValue(
  JNIEnv *env,
  jclass,
  jstring target,
  jstring name,
  jobject on,
  jdouble newValue
) {
  // get the owner type
  jclass clazz = GetTargetClass(env, target);
  if (clazz == nullptr) return;

  // get the actual field
  jfieldID field = GetFieldId(env, clazz, name, "D", on == nullptr);
  if (field == nullptr) return;

  if (on == nullptr)
    env->SetStaticDoubleField(clazz, field, newValue);
  else
    env->SetDoubleField(on, field, newValue);
}
