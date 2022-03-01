# general settings
cmake_minimum_required(VERSION 3.20)
project(NativeReflection)

# compiler options
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_BUILD_TYPE Release)

# require jni
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# sets the source files
set(SOURCE_FILES src/main/c/jni_nativereflection.cpp src/main/c/jni_util.cpp)
add_library(native.dll SHARED ${SOURCE_FILES})
