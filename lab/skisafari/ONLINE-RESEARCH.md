# Startup and save alternatives (2026-10-30)

## Sources reviewed

- Google: https://support.google.com/googleplay/android-developer/answer/10183279?hl=en — Automatic protection includes licensing/redistribution checks and optional native anti-tamper controls.
- Kirby1997 Morphe source (GPL-3.0): https://github.com/Kirby1997/morphe-patches/blob/main/patches/src/main/kotlin/io/github/kirby1997/patches/twitter/BypassPairipPatch.kt — Startup-path strategy and explicit downstream virtualized-method limitations. The reviewed source blob was `758d0b740d5cdceaabc133633c0fc25473257a7a`.
- Kirby1997 license-only source: https://github.com/Kirby1997/morphe-patches/blob/main/patches/src/main/kotlin/io/github/kirby1997/patches/pairip/DisablePairipLicenseCheckPatch.kt — Explicitly does not solve native VM integrity checks.
- https://github.com/ahmedmani/pairipfix and https://github.com/ymuuuu/pairipfix-zygisk — Require rooted hooking environments; excluded by the task constraints.
- https://github.com/SafaSafari/bypass_libpairipcore — The CRC script writes original CRC values for changed archive entries. This is not a valid ZIP-integrity verification and was not used.

These sources do not establish compatibility with Ski Safari. No external executable or ready-made modified APK was installed.

## Local reproduction of the startup strategy

The initial direct VM-call scan found 24 caller methods, including startup calls in `androidx.core.app.CoreComponentFactory` and `Yodo1BaseApplication`. This count does not cover dynamically initialized reflection targets.

`experiments/pairip-startup-build6.patch` preserves an app-specific guarded experiment adapted from the Kirby1997 startup strategy. Its native PairIP library was unchanged and checked by SHA-256. It skipped the native startup launcher, retained the original Application super-call, omitted the local signature gate and license provider, and retained SDK VM callbacks. No license response, billing receipt or server transaction was fabricated.

Build 6 compiled, patched and signed successfully. On SM-T870 it reached the new Application log instead of the native `SIGSEGV`, then failed in `Yodo1Application.onCreate` on a null `java.lang.reflect.Method` (`TJheaMreAW.zYJ`). The original body behind that reflection target was not found among static methods with the matching typed signature. Other SplashActivity methods use the same initialization scheme. Skipping further SDK methods without reconstructing their bodies would risk breaking necessary startup and purchase-related behavior.

The experiment is retained as a research diff, not included in the main unlock patch. The tablet currently has this failing build 6 installed; it has not reached Unity or unlocked items.

## Save route: confirmed observations, not an applied edit

`Profile.Obfuscate(string udid, byte[] data, int dataLength)` at RVA `0xBD93D0` XORs each byte with a character from the profile UDID: initial character index `3*length/4`, step two, wrap to zero. The observed routine iterates the entire byte array. The profile filename stem supplies the matching 40-character UDID.

On the PC backup, this decoded a matching UDID header, the separate coin count, and 170 integer entries encoded as BinaryReader length-prefixed UTF-8 keys plus little-endian int32 values. The next section has nine float entries. The decoded buffer has zero padding; re-obfuscation reproduces the original file exactly. This proves the obfuscation and inspected sections, not full save-schema or restore compatibility.

`Item.set_Unlocked(true)` (RVA `0xC52D74`) passes the object's name and value one to `GameState.SetInt`. Product subclasses override unlock behavior and must be distinguished from normal game items. A future save transformer must enumerate the actual item catalog, preserve all unrelated bytes/values, and validate every edited field. No unlock candidate has been written yet.

`inspect-profile.py` only reads its input and emits a structural report. It has no write or restore operation. Personal saves and decoded payloads stay outside Git. The original backup, phone, and game progress were not changed by this analysis.
