#include <jni.h>
#include "jni_util.h"

JNIEXPORT jfieldID JNICALL GetFieldId(JNIEnv *env, jclass owner, jstring name, jstring signature, bool staticField) {
  const char *field_name = env->GetStringUTFChars(name, nullptr);
  const char *field_signature = env->GetStringUTFChars(signature, nullptr);

  jfieldID fieldId;
  if (staticField) {
    fieldId = env->GetStaticFieldID(owner, field_name, field_signature);
  } else {
    fieldId = env->GetFieldID(owner, field_name, field_signature);
  }

  env->ReleaseStringUTFChars(name, field_name);
  env->ReleaseStringUTFChars(signature, field_signature);

  // ensure we got the field
  if (fieldId == nullptr) {
    RaiseIllegalArgumentException(env, "illegal field given");
    return nullptr;
  }

  return fieldId;
}

JNIEXPORT jclass JNICALL GetTargetClass(JNIEnv *env, jstring name) {
  const char *owner = env->GetStringUTFChars(name, 0);

  jclass clazz = env->FindClass(owner);
  env->ReleaseStringUTFChars(name, owner);

  // ensure we actually got the class
  if (clazz == nullptr) {
    RaiseIllegalArgumentException(env, "unknown target class given");
    return nullptr;
  }

  return clazz;
}

JNIEXPORT void JNICALL RaiseIllegalArgumentException(JNIEnv *env, const char *msg) {
  jclass clazz = env->FindClass("java/lang/IllegalArgumentException");
  env->ThrowNew(clazz, msg);
}
