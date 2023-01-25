export ANDROID_HOME=/Path/to/Sdk
export ANDROID_NDK_HOME=/Path/to/Sdk/ndk

bazel build -c opt --config=android_arm64 mediapipe/examples/android/src/java/com/google/mediapipe/apps/posetrackinggpu:posetrackinggpu --verbose_failures

adb install bazel-bin/mediapipe/examples/android/src/java/com/google/mediapipe/apps/posetrackinggpu/posetrackinggpu.apk