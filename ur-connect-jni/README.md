# UR-Connect JNI bindings
## compiling for NDK
- `rustup target add aarch64-linux-android x86_64-linux-android`
- set `ANDROID_NDK_HOME`
- `cargo ndk -t arm64-v8a -t x86_64 -- build --release`
