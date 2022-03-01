#include <jni.h>

/**
 * Raises an illegal argument exception in the current jni environment.
 *
 * @param env the jni environment to throw the exception in.
 * @param msg the message the use when throwing the exception.
 */
JNIEXPORT void JNICALL RaiseIllegalArgumentException(JNIEnv *, const char *);

JNIEXPORT jclass JNICALL GetTargetClass(JNIEnv *, jstring);

JNIEXPORT jfieldID JNICALL GetFieldId(JNIEnv *, jclass, jstring, jstring, bool);