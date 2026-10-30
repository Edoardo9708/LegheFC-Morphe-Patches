# Ski Safari – Unlock All Items (lab)

Target: `com.yodo1.skisafarigp`, version 2.5 / versionCode 108, ARM64.

## Verified native code

- `ShopItemHelper.UnlockAllItems()` RVA `0xC8DC94`, file offset `0xC89C94`.
- `ShopItemHelper.TestUnlockAll()` RVA `0xC8FB90`, file offset `0xC8BB90`; its only instruction is `B 0xC8DC94`.
- The `UnlockAllItems` loop checks each catalog item's `Unlocked` property, sets locked entries to unlocked, clears their `IsNew` flag, and calls `GameState.Save()` (`0xBC23F0`) when changes were made.
- `GameState.Save()` calls `Profile.Save(true)`; `Profile.Save(bool)` serializes/encrypts the profile and completes its local persistence path before returning.
- The profile class includes the saved coin count and stats separately; the unlock routine does not call coin setters or replace the Profile object.

These observations come from the exact original base APK and ARM64 split copied read-only from the Galaxy S23+. Il2CppDumper reported a protection warning, so the dump is treated as partial evidence; no protection bypass or runtime memory dump was used.

The base manifest reports `allowBackup=true` and `extractNativeLibs=true`; no explicit backup-rule XML was present in the APK. The ARM64 split includes `libpairipcore.so` (SHA-256 `b4b4cc04d323507ff577d86fffa809106ff22de033a783cf3d37752e1da19c0b`). This patch does not modify or bypass PairIP, and its behavior after Morphe re-signing is unknown.

## Runtime approach

The experimental Morphe patch adds `libskisafariunlock.so` and a small Java extension. A handler on Android's UI thread waits for the IL2CPP domain, attaches that thread through the exported `il2cpp_thread_attach` API, then polls until both `GameState.PersistentProfile` and `ShopItemHelper.Instance` are non-null before calling the verified native method once. Static analysis shows the game method calls `GameState.Save()` when it changes unlock flags. A dedicated SharedPreferences marker is committed after the native method returns, preventing repeat calls on later launches. This marker is separate from the game profile.

The UI-thread call is not proven safe: the Unity game loop may run on another thread, and direct native IL2CPP invocation could race game state or crash. The marker records that the method returned; it does not independently verify that changed items persisted. Do not use this build on the only copy of a live profile until a separate installation and a restart-based save check are available.

The method walks the complete `ShopItem` catalog. Static analysis confirms it changes local `Unlocked` flags and does not make billing calls or write purchase receipts. The catalog was not fully resolved, so this patch should be treated as potentially enabling locally catalogued purchase-associated items; it does not simulate or record a purchase.

## Strict build identity

`libil2cpp.so` SHA-256: `b24154995803633ab95eeea21836f67d74699b305a2ed631a2600c59dfdd639a`

`global-metadata.dat` SHA-256: `b8d2786c3be10261c7c666ce8155f4d1d6d98ec994586e16334219a5fc0f5144`

The patch fails closed if either file differs. The original native library is not replaced. Run the read-only verifier before patching:

```powershell
python .\lab\skisafari\verify-originals.py path\to\base.apk path\to\split_config.arm64_v8a.apk
```

Build the JNI helper on Windows with the installed Android NDK:

```powershell
powershell -ExecutionPolicy Bypass -File .\lab\skisafari\build-native.ps1
```

## Current limitations

- The Android runtime call has not been exercised on-device. Unity thread safety, PairIP/license behavior after Morphe re-signing, save visibility after process exit, and the precise item catalog still require a disposable test installation.
- No modified APK has been built or installed. A different APK signature can prevent updating the Play-installed original; keep the original app and its data untouched until a safe test plan exists.
- Morphe Gradle compilation currently stops during plugin resolution: `app.morphe.patches:1.3.4` is hosted on GitHub Packages and this PC has no configured `gpr.user`/`gpr.key` or `GITHUB_TOKEN`. The official Morphe Patcher setup documentation requires GitHub Packages authentication even for public packages.
