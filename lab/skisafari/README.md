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
- A different APK signature can prevent updating the Play-installed original; keep the original app and its data untouched until a safe test plan exists.
- Standard Gradle compilation stops during plugin resolution: the existing GitHub credential receives HTTP 401 from GitHub Packages. Repository access is available; package-registry access is separate.

## Local Desktop build

The installed Morphe Desktop fat JAR provides the patcher API. `build-local.ps1` uses the installed Kotlin compiler, Java compiler and Android D8 to compile the new patch and merge it into an existing local bundle. Existing JVM classes and resources are preserved; D8 merges the patch and extension DEX files. This incremental local build does not replace verification of the complete repository with Gradle.

```powershell
.\lab\skisafari\build-local.ps1 -DesktopJar ..\morphe-desktop-1.20.0-all.jar -BaselineBundle ..\morphe-test-f13\patches-f13-local.mpp -OutputDirectory ..\morphe-test-skisafari-build2
```

For APK sets, `MergeApk.java` invokes the merger bundled in Morphe Desktop on a copy. The CLI patch command expects the resulting merged APK, not the ASPK container. All output stays on the PC; the scripts do not install applications.

Local verification completed with Morphe Desktop 1.20.0:

- Kotlin, Java and both DEX merges compiled successfully.
- Morphe listed the patch with package `com.yodo1.skisafarigp`, version `2.5`.
- The corrected resource loader resolves the helper through the patch bundle's classloader.
- Patching, rebuilding and signing the merged APK succeeded. Android apksigner verified APK Signature Scheme v2.
- `VerifyApk.java` found exactly one `onCreate` hook and the runtime extension in the final APK.
- ZIP CRC validation and SHA-256 checks confirmed unchanged original IL2CPP/metadata and the correct helper.
- Original bundle JVM classes and resources were preserved; only the combined DEX files and bundle manifest changed.

Output directory on this PC: `morphe-test-skisafari-build2` (outside the repository). The test APK SHA-256 is `b1ab4624e372b63f56cda9591ea9e42b2f5de9ccfc2c2ea360011bfd3976fd3e`. No runtime execution or persistent save success has been established.
