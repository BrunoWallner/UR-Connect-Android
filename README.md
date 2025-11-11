# UR-Connect
Android client for [UR-Connect](https://github.com/julian-baumann/ur-connect)

At the moment, only a simple timetable view is supported.

## Building
Prerequisites: 
- Rust (1.85 or higher) with `rust-ndk` (obtainable through `binstall`)
- Android SDK with NDK (needs to be installed additionally from SDK Tools in Android Studio SDK Manager)
- Android Studio Otter

### Rust Setup
If you don't have `binstall` installed already, install it using `cargo install cargo-binstall`. Then install `rust-ndk` by doing `cargo binstall rust-ndk`.

You might also have to do `rustup target add aarch64-linux-android` and `rustup target add x86_64-linux-android` if you have never compiled Rust for Android before.

### Rust Backend Build
Before building the Android app, you need to build the Rust binary for UR-Connect. You can do this by running the following commands:
```bash
cd ur-connect-jni
./compile.sh
```

### Main App Build
After you built the Rust binary, open the main project in Android Studio and compile it.
