#!/bin/sh
cargo ndk -t arm64-v8a -t x86_64 -- build --release

cp ./target/x86_64-linux-android/release/libur_connect_jni.so ../app/src/main/jniLibs/x86_64/
cp ./target/aarch64-linux-android/release/libur_connect_jni.so ../app/src/main/jniLibs/arm64-v8a/
