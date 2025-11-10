use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jobject, jstring};

use lazy_static::lazy_static;
use tokio::runtime::Runtime;
use ur_connect::{Recurrence, UrConnect};

lazy_static! {
    static ref RUNTIME: Runtime = Runtime::new().unwrap();
    static ref CLIENT: UrConnect = UrConnect::new().unwrap();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_ur_connect_Backend_login<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    name: JString<'local>,
    passwd: JString<'local>,
) -> jobject {
    let login_result_enum_class = env.find_class("de/ur/connect/Backend$LoginResult").unwrap();
    let name: String = env
        .get_string(&name)
        .expect("Couldn't get java string!")
        .into();
    let passwd: String = env
        .get_string(&passwd)
        .expect("Couldn't get java string!")
        .into();
    let res: String = RUNTIME.block_on(async move {
        match CLIENT.login(&name, &passwd).await {
            Ok(_) => "SUCCESS".into(),
            Err(_) => "INVALID_CREDENTIALS".into(),
        }
    });

    let output = env
        .get_static_field(
            login_result_enum_class,
            res,
            "Lde/ur/connect/Backend$LoginResult;",
        )
        .expect("Couldn't get enum value")
        .l()
        .expect("Expected object");

    output.into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_ur_connect_Backend_getTimeTableSerialized<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jstring {
    let Ok(entries) = RUNTIME.block_on(async move { CLIENT.get_timetable().await }) else {
        return std::ptr::null_mut();
    };

    let mut serialized = String::new();
    for entry in entries {
        let recurrence = match entry.recurrence {
            Some(r) => match r {
                Recurrence::Daily => 1,
                Recurrence::Weekly => 7,
                Recurrence::Monthly => 28,
                Recurrence::Yearly => 356,
                Recurrence::Custom(_) => 0,
            },
            None => 0,
        };
        let ent = format!(
            "{}§{}§{}§{}§{}\n",
            entry.date.replace("§", ""),
            entry.time.replace("§", ""),
            entry.title.replace("§", ""),
            entry.location.replace("§", ""),
            recurrence,
        );
        serialized.push_str(&ent);
    }

    let output = env
        .new_string(serialized)
        .expect("Couldn't create java string!");

    output.into_raw()
}
