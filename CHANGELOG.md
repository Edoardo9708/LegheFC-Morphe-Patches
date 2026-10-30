## [1.6.0-dev.6](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.6.0-dev.5...v1.6.0-dev.6) (2026-10-30)

### ✨ New Features

* **prezzibenzina:** add F4 single getPackageInfo signature trace ([10ebd71](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/10ebd71764d8539875cbd39a913ef125d532de3c))

## [1.6.0-dev.5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.6.0-dev.4...v1.6.0-dev.5) (2026-10-30)

### 🐛 Bug Fixes

* **prezzibenzina:** isolate Frida F3 startup without Java hooks ([e6ac12c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e6ac12cf8ab934739b0f1787fc6e5f711b0db92b))

## [1.6.0-dev.4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.6.0-dev.3...v1.6.0-dev.4) (2026-10-30)

### 🐛 Bug Fixes

* **prezzibenzina:** update F2 Frida Gadget for Android 17 ([5641758](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/5641758322110e4bf10b23f717219ac6f2b1e8b6))

## [1.6.0-dev.3](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.6.0-dev.2...v1.6.0-dev.3) (2026-10-30)

### ✨ New Features

* **prezzibenzina:** add read-only Frida signature trace F1 ([44bef6c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/44bef6cdb28faeffe8282a2e419f3a51a9335eef))

## [1.6.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.6.0-dev.1...v1.6.0-dev.2) (2026-10-29)

### 🐛 Bug Fixes

* **prezzibenzina:** validate original ads-level method signature correctly ([f5842f2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f5842f2a7287d366cc58e207fd39ec0dca011569))

### ✨ New Features

* **prezzibenzina:** add opt-in ads-only test A1 with Logcat marker ([98dd675](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/98dd6759daae3c3d6b7891674b9f6b97dd041750))

## [1.6.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0...v1.6.0-dev.1) (2026-10-29)

### 🐛 Bug Fixes

* **fonometro:** correct Kotlin escaping for nested ad callback class ([7493a4b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7493a4b9258a5cb3c447a66306433d7c42fd9cc5))
* **fonometro:** escape nested DEX class name in Smali injection ([cb3cddd](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cb3cddd50d99a06b9a0f709e87af311e2fdc05f0))
* **fonometro:** use English ad removal patch metadata ([997ea57](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/997ea57bf7f3dfb5784faca0427fd80729ce8f2b))
* **spotify:** avoid unsupported six-element list destructuring in event logger ([fb9e4f4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/fb9e4f4f5d8d83bf228d60d7e3dc5db996ca44c5))
* **spotify:** revert tapped-event probes to last diagnostic revision R3 ([4d3e51c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4d3e51c6075373b40c792b5951ef1478b98bc07a))
* **spotify:** use range invoke for high-index dispatcher argument ([d65938f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/d65938f8f9c245c066bcd0907021bfeec5e2c0c9))
* **spotify:** withdraw experimental previous-button UI override after playback instability ([f061582](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f061582805f3a24f4a8b0776510d8c22ed1cbc33))

### ✨ New Features

* **dnschanger:** add Pro return diagnostic logging ([82840b5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/82840b58c8edd2e494d71b7dd7ca75a7987f4a3e))
* **dnschanger:** test forced true in Pro gate with D2 Logcat marker ([e57f477](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e57f4774ad14bd981f8819a6e21cb4a8200265fe))
* **fonometro:** add ads-only patch for version 11.5 ([2b6f67d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2b6f67d98895c7af2b7863434b4342679a625c69))
* **prezzibenzina:** add read-only entitlement decision diagnostics ([599612d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/599612d7c2ac7e69f307e1f5a6a354b1bb5ad148))
* **spotify:** add ADB logcat previous-button diagnostic patch ([1d3e975](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1d3e975ca86546df113f51e52f1284d95c0641c8))
* **spotify:** add bytecode-free no-op control to isolate playback skips ([bebd2ff](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/bebd2ffd16726817c165c80ed4f9f102a9ddc7f9))
* **spotify:** add isolated previous-button UI gate test ([c285e79](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c285e79de60c9eb2e92747ecf79c7f9edf70ddfc))
* **spotify:** log UI-gate patch revision from proven controls path ([f24e05d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f24e05def9d793fb4896150c0041d7a37e1cfbcd))
* **spotify:** print diagnostic code revision on every observed player state ([c7b16d5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c7b16d5585cfbcf5fbb968a4524b55c0ac5ccc4c))
* **spotify:** trace media callbacks and player dispatch in ADB Logcat ([8dd0952](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/8dd0952cf3f4e4890aeb12a817f930069f473808))
* **spotify:** trace SkipPreviousTapped emission and handling with code ID R4 ([7fd1d47](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7fd1d47a7312748a7d11ae08f49b00ab63709a59))

## [1.5.0-dev.9](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.8...v1.5.0-dev.9) (2026-10-29)

### ✨ New Features

* **dnschanger:** test forced true in Pro gate with D2 Logcat marker ([e57f477](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e57f4774ad14bd981f8819a6e21cb4a8200265fe))

## [1.5.0-dev.8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.7...v1.5.0-dev.8) (2026-10-29)

### ✨ New Features

* **dnschanger:** add Pro return diagnostic logging ([82840b5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/82840b58c8edd2e494d71b7dd7ca75a7987f4a3e))

## [1.5.0-dev.7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.6...v1.5.0-dev.7) (2026-10-29)

### ✨ New Features

* **spotify:** add bytecode-free no-op control to isolate playback skips ([bebd2ff](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/bebd2ffd16726817c165c80ed4f9f102a9ddc7f9))

## [1.5.0-dev.6](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.5...v1.5.0-dev.6) (2026-10-28)

### 🐛 Bug Fixes

* **spotify:** revert tapped-event probes to last diagnostic revision R3 ([4d3e51c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4d3e51c6075373b40c792b5951ef1478b98bc07a))
* **spotify:** withdraw experimental previous-button UI override after playback instability ([f061582](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f061582805f3a24f4a8b0776510d8c22ed1cbc33))

## [1.5.0-dev.5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.4...v1.5.0-dev.5) (2026-10-28)

### 🐛 Bug Fixes

* **spotify:** avoid unsupported six-element list destructuring in event logger ([fb9e4f4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/fb9e4f4f5d8d83bf228d60d7e3dc5db996ca44c5))

### ✨ New Features

* **spotify:** trace SkipPreviousTapped emission and handling with code ID R4 ([7fd1d47](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7fd1d47a7312748a7d11ae08f49b00ab63709a59))

## [1.5.0-dev.4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.3...v1.5.0-dev.4) (2026-10-28)

### ✨ New Features

* **spotify:** log UI-gate patch revision from proven controls path ([f24e05d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f24e05def9d793fb4896150c0041d7a37e1cfbcd))
* **spotify:** print diagnostic code revision on every observed player state ([c7b16d5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c7b16d5585cfbcf5fbb968a4524b55c0ac5ccc4c))

## [1.5.0-dev.3](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.2...v1.5.0-dev.3) (2026-10-28)

### ✨ New Features

* **spotify:** add isolated previous-button UI gate test ([c285e79](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c285e79de60c9eb2e92747ecf79c7f9edf70ddfc))

## [1.5.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.5.0-dev.1...v1.5.0-dev.2) (2026-10-28)

### 🐛 Bug Fixes

* **spotify:** use range invoke for high-index dispatcher argument ([d65938f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/d65938f8f9c245c066bcd0907021bfeec5e2c0c9))

### ✨ New Features

* **spotify:** trace media callbacks and player dispatch in ADB Logcat ([8dd0952](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/8dd0952cf3f4e4890aeb12a817f930069f473808))

## [1.5.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.4.0...v1.5.0-dev.1) (2026-10-28)

### 🐛 Bug Fixes

* **fonometro:** correct Kotlin escaping for nested ad callback class ([7493a4b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7493a4b9258a5cb3c447a66306433d7c42fd9cc5))
* **fonometro:** escape nested DEX class name in Smali injection ([cb3cddd](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cb3cddd50d99a06b9a0f709e87af311e2fdc05f0))
* **fonometro:** use English ad removal patch metadata ([997ea57](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/997ea57bf7f3dfb5784faca0427fd80729ce8f2b))

### ✨ New Features

* **fonometro:** add ads-only patch for version 11.5 ([2b6f67d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2b6f67d98895c7af2b7863434b4342679a625c69))
* **spotify:** add ADB logcat previous-button diagnostic patch ([1d3e975](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1d3e975ca86546df113f51e52f1284d95c0641c8))

## [1.4.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.4.0-dev.1...v1.4.0-dev.2) (2026-10-28)

### 🐛 Bug Fixes

* **fonometro:** use English ad removal patch metadata ([997ea57](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/997ea57bf7f3dfb5784faca0427fd80729ce8f2b))

## [1.4.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.3.0...v1.4.0-dev.1) (2026-10-28)

### 🐛 Bug Fixes

* **fonometro:** correct Kotlin escaping for nested ad callback class ([7493a4b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7493a4b9258a5cb3c447a66306433d7c42fd9cc5))
* **fonometro:** escape nested DEX class name in Smali injection ([cb3cddd](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cb3cddd50d99a06b9a0f709e87af311e2fdc05f0))

### ✨ New Features

* **fonometro:** add ads-only patch for version 11.5 ([2b6f67d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2b6f67d98895c7af2b7863434b4342679a625c69))

## [1.3.0](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0...v1.3.0) (2026-10-28)

### 🐛 Bug Fixes

* accurately describe experimental Outdooractive behavior ([9259566](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9259566eb787f1eb74bcb963fd9aa9702f59e466))
* accurately describe experimental Twilight behavior ([973d31d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/973d31d51f6a7548a27399c0e91fcc494a19b357))
* **ci:** authenticate Gradle publish to Morphe GitHub Maven registry ([2dfb2db](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2dfb2dbed0d10cb9f3a47f0b3d2e92465cfd6d5b))
* **ci:** publish DEX-bearing Morphe bundle and repair dev.12 asset ([1c4a850](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1c4a8500a353b77855adabc6e29f590cb3006da1))
* **ci:** recover compiled dev bundle when semantic-release Git notes push fails ([4550b49](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4550b49f66db2d7f468ae4f72b985bc3f62a412e))
* **ci:** repair Morphe dev.12 bundle media type and correct fallback upload ([e5c2ddf](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5c2ddfefb69199013a7609badcf3069a1291670))
* **ci:** restore missing dev.12 channel note to publish dev.13 ([cd34c2b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cd34c2b6f9e85c1a8943a3931c10f1936d36cc91))
* **flowx:** add R7 server/cache entry and post-write entitlement diagnostics ([c69323a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c69323ae61867f935d71c7828671030f21143999))
* **flowx:** clarify local Gold test metadata without changing bytecode ([eccd1e2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/eccd1e28fa67e30281ddb3f20e6a9b8a25a89341))
* **flowx:** guard neutral test register against original write arguments ([12bb28b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/12bb28b0088ba694ff698744c0dc6c8afc86e75e))
* **flowx:** log diagnostic revision on each entitlement refresh ([0dbf92b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/0dbf92be38bc8354e03a7bfaac3d8bcb9ee1674e))
* **flowx:** log original and isolated test lists together after each load ([b694ef8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/b694ef807a4aa2978bacfead5c97d81fd132cdb7))
* **flowx:** log original entitlement list size before empty check ([f52288f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f52288fd0784f3201229a8cb694d97e8e29fdf16))
* **flowx:** print unique patch runtime diagnostic revision ([6144e22](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6144e22ea172a5f3d0fdd6ce3c73ec755723a999))
* **flowx:** R8 trace demo factory and conditional entitlement reset in b() ([3c3eae5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/3c3eae58223938eb035c6ea9557deddaf76def45))
* **flowx:** R9 move cache demo write to confirmed post-write path ([71973a7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/71973a78f9cf010f8d5a6c3e95a4a9557705c837))
* **flowx:** separate server and cache diagnostic smali blocks ([959f145](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/959f1455a366047f0a3feace87bd4758f7d18f56))
* **flowx:** use all explicit entitlement constructor arguments ([84ce609](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/84ce609c2f8e097f9bd3674c15b4bf1796eefa1d))
* **flowx:** use type-compatible demo rows in isolated list probe ([09d9802](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/09d9802ff9eb9eb501365500cddee56d57597c65))
* replace outdated Leghe FC repository documentation ([e5b1eeb](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5b1eeb8fc7950d41c7b60d067c67ddb5d249e7e))
* set MembershipDiagnosticPatch.kt patch description to Unlock Pro ([927682c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/927682c02a782e0ab20aeb076d4f0cdf8d5647b1))
* set TrialStatusExperimentPatch.kt patch description to Unlock Pro ([313ca9a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/313ca9a33119c69624e56dc66d583a5a6a59c0d0))
* test new reg values ([da49269](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/da49269aecc4c2d4934cb231991a5474d9de20d6))

### ✨ New Features

* **flowx:** add isolated Gold entitlement diagnostic test ([9097d9d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9097d9da4e1ab383035a2a4ccf5f6ca8c7ec0373))
* **flowx:** create isolated test lists after server and cache load ([e49ee2d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e49ee2d1d234acb5538d7976e62b0a918d273604))
* **flowx:** log calls to original entitlement check without altering inputs ([a0bbc54](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/a0bbc54fc60cfc16f087b7ba8bda4f9049bd9e71))
* import Outdooractive experimental patch ([f0bde4e](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f0bde4e543781f8a170e20fd2b55033cfa5a39fc))
* import Twilight experimental patch ([4a0d735](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4a0d735de2bb1244faa29d2cb1d460efaf57c877))

## [1.3.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.3.0-dev.1...v1.3.0-dev.2) (2026-10-28)

### 🐛 Bug Fixes

* **flowx:** clarify local Gold test metadata without changing bytecode ([eccd1e2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/eccd1e28fa67e30281ddb3f20e6a9b8a25a89341))

## [1.3.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0...v1.3.0-dev.1) (2026-10-28)

### 🐛 Bug Fixes

* accurately describe experimental Outdooractive behavior ([9259566](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9259566eb787f1eb74bcb963fd9aa9702f59e466))
* accurately describe experimental Twilight behavior ([973d31d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/973d31d51f6a7548a27399c0e91fcc494a19b357))
* **ci:** authenticate Gradle publish to Morphe GitHub Maven registry ([2dfb2db](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2dfb2dbed0d10cb9f3a47f0b3d2e92465cfd6d5b))
* **ci:** publish DEX-bearing Morphe bundle and repair dev.12 asset ([1c4a850](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1c4a8500a353b77855adabc6e29f590cb3006da1))
* **ci:** recover compiled dev bundle when semantic-release Git notes push fails ([4550b49](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4550b49f66db2d7f468ae4f72b985bc3f62a412e))
* **ci:** repair Morphe dev.12 bundle media type and correct fallback upload ([e5c2ddf](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5c2ddfefb69199013a7609badcf3069a1291670))
* **ci:** restore missing dev.12 channel note to publish dev.13 ([cd34c2b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cd34c2b6f9e85c1a8943a3931c10f1936d36cc91))
* **flowx:** add R7 server/cache entry and post-write entitlement diagnostics ([c69323a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c69323ae61867f935d71c7828671030f21143999))
* **flowx:** guard neutral test register against original write arguments ([12bb28b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/12bb28b0088ba694ff698744c0dc6c8afc86e75e))
* **flowx:** log diagnostic revision on each entitlement refresh ([0dbf92b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/0dbf92be38bc8354e03a7bfaac3d8bcb9ee1674e))
* **flowx:** log original and isolated test lists together after each load ([b694ef8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/b694ef807a4aa2978bacfead5c97d81fd132cdb7))
* **flowx:** log original entitlement list size before empty check ([f52288f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f52288fd0784f3201229a8cb694d97e8e29fdf16))
* **flowx:** print unique patch runtime diagnostic revision ([6144e22](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6144e22ea172a5f3d0fdd6ce3c73ec755723a999))
* **flowx:** R8 trace demo factory and conditional entitlement reset in b() ([3c3eae5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/3c3eae58223938eb035c6ea9557deddaf76def45))
* **flowx:** R9 move cache demo write to confirmed post-write path ([71973a7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/71973a78f9cf010f8d5a6c3e95a4a9557705c837))
* **flowx:** separate server and cache diagnostic smali blocks ([959f145](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/959f1455a366047f0a3feace87bd4758f7d18f56))
* **flowx:** use all explicit entitlement constructor arguments ([84ce609](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/84ce609c2f8e097f9bd3674c15b4bf1796eefa1d))
* **flowx:** use type-compatible demo rows in isolated list probe ([09d9802](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/09d9802ff9eb9eb501365500cddee56d57597c65))
* replace outdated Leghe FC repository documentation ([e5b1eeb](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5b1eeb8fc7950d41c7b60d067c67ddb5d249e7e))
* set MembershipDiagnosticPatch.kt patch description to Unlock Pro ([927682c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/927682c02a782e0ab20aeb076d4f0cdf8d5647b1))
* set TrialStatusExperimentPatch.kt patch description to Unlock Pro ([313ca9a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/313ca9a33119c69624e56dc66d583a5a6a59c0d0))
* test new reg values ([da49269](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/da49269aecc4c2d4934cb231991a5474d9de20d6))

### ✨ New Features

* **flowx:** add isolated Gold entitlement diagnostic test ([9097d9d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9097d9da4e1ab383035a2a4ccf5f6ca8c7ec0373))
* **flowx:** create isolated test lists after server and cache load ([e49ee2d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e49ee2d1d234acb5538d7976e62b0a918d273604))
* **flowx:** log calls to original entitlement check without altering inputs ([a0bbc54](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/a0bbc54fc60cfc16f087b7ba8bda4f9049bd9e71))
* import Outdooractive experimental patch ([f0bde4e](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f0bde4e543781f8a170e20fd2b55033cfa5a39fc))
* import Twilight experimental patch ([4a0d735](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4a0d735de2bb1244faa29d2cb1d460efaf57c877))

## [1.2.0](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.1.0...v1.2.0) (2026-10-28)

### ✨ New Features

* **flowx:** publish R9 Gold patch as stable ([9179d4c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9179d4c7d26ecdb8109f3db6ca5e75a6fd6b6377))

## [1.2.0-dev.13](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.12...v1.2.0-dev.13) (2026-10-28)

### 🐛 Bug Fixes

* **ci:** authenticate Gradle publish to Morphe GitHub Maven registry ([2dfb2db](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2dfb2dbed0d10cb9f3a47f0b3d2e92465cfd6d5b))
* **ci:** publish DEX-bearing Morphe bundle and repair dev.12 asset ([1c4a850](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1c4a8500a353b77855adabc6e29f590cb3006da1))
* **ci:** recover compiled dev bundle when semantic-release Git notes push fails ([4550b49](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4550b49f66db2d7f468ae4f72b985bc3f62a412e))
* **ci:** repair Morphe dev.12 bundle media type and correct fallback upload ([e5c2ddf](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5c2ddfefb69199013a7609badcf3069a1291670))
* **ci:** restore missing dev.12 channel note to publish dev.13 ([cd34c2b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cd34c2b6f9e85c1a8943a3931c10f1936d36cc91))
* **flowx:** add R7 server/cache entry and post-write entitlement diagnostics ([c69323a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/c69323ae61867f935d71c7828671030f21143999))
* **flowx:** R8 trace demo factory and conditional entitlement reset in b() ([3c3eae5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/3c3eae58223938eb035c6ea9557deddaf76def45))
* **flowx:** R9 move cache demo write to confirmed post-write path ([71973a7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/71973a78f9cf010f8d5a6c3e95a4a9557705c837))

## [1.2.0-dev.12](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.11...v1.2.0-dev.12) (2026-10-24)

### 🐛 Bug Fixes

* **flowx:** print unique patch runtime diagnostic revision ([6144e22](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6144e22ea172a5f3d0fdd6ce3c73ec755723a999))

## [1.2.0-dev.11](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.10...v1.2.0-dev.11) (2026-10-23)

### 🐛 Bug Fixes

* test new reg values ([da49269](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/da49269aecc4c2d4934cb231991a5474d9de20d6))

## [1.2.0-dev.10](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.9...v1.2.0-dev.10) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** separate server and cache diagnostic smali blocks ([959f145](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/959f1455a366047f0a3feace87bd4758f7d18f56))

## [1.2.0-dev.9](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.8...v1.2.0-dev.9) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** use type-compatible demo rows in isolated list probe ([09d9802](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/09d9802ff9eb9eb501365500cddee56d57597c65))

## [1.2.0-dev.8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.7...v1.2.0-dev.8) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** guard neutral test register against original write arguments ([12bb28b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/12bb28b0088ba694ff698744c0dc6c8afc86e75e))

## [1.2.0-dev.7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.6...v1.2.0-dev.7) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** log original and isolated test lists together after each load ([b694ef8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/b694ef807a4aa2978bacfead5c97d81fd132cdb7))

## [1.2.0-dev.6](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.5...v1.2.0-dev.6) (2026-10-23)

### ✨ New Features

* **flowx:** create isolated test lists after server and cache load ([e49ee2d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e49ee2d1d234acb5538d7976e62b0a918d273604))

## [1.2.0-dev.5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.4...v1.2.0-dev.5) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** log original entitlement list size before empty check ([f52288f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f52288fd0784f3201229a8cb694d97e8e29fdf16))

## [1.2.0-dev.4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.3...v1.2.0-dev.4) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** log diagnostic revision on each entitlement refresh ([0dbf92b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/0dbf92be38bc8354e03a7bfaac3d8bcb9ee1674e))

## [1.2.0-dev.3](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.2...v1.2.0-dev.3) (2026-10-23)

### ✨ New Features

* **flowx:** log calls to original entitlement check without altering inputs ([a0bbc54](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/a0bbc54fc60cfc16f087b7ba8bda4f9049bd9e71))

## [1.2.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.2.0-dev.1...v1.2.0-dev.2) (2026-10-23)

### 🐛 Bug Fixes

* **flowx:** use all explicit entitlement constructor arguments ([84ce609](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/84ce609c2f8e097f9bd3674c15b4bf1796eefa1d))

## [1.2.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.1.0...v1.2.0-dev.1) (2026-10-23)

### 🐛 Bug Fixes

* accurately describe experimental Outdooractive behavior ([9259566](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9259566eb787f1eb74bcb963fd9aa9702f59e466))
* accurately describe experimental Twilight behavior ([973d31d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/973d31d51f6a7548a27399c0e91fcc494a19b357))
* replace outdated Leghe FC repository documentation ([e5b1eeb](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5b1eeb8fc7950d41c7b60d067c67ddb5d249e7e))
* set MembershipDiagnosticPatch.kt patch description to Unlock Pro ([927682c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/927682c02a782e0ab20aeb076d4f0cdf8d5647b1))
* set TrialStatusExperimentPatch.kt patch description to Unlock Pro ([313ca9a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/313ca9a33119c69624e56dc66d583a5a6a59c0d0))

### ✨ New Features

* **flowx:** add isolated Gold entitlement diagnostic test ([9097d9d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9097d9da4e1ab383035a2a4ccf5f6ca8c7ec0373))
* import Outdooractive experimental patch ([f0bde4e](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f0bde4e543781f8a170e20fd2b55033cfa5a39fc))
* import Twilight experimental patch ([4a0d735](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4a0d735de2bb1244faa29d2cb1d460efaf57c877))

## [1.1.0](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.1...v1.1.0) (2026-10-23)

### ✨ New Features

* include Outdooractive Unlock Pro experimental patch ([6a21c89](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6a21c89e8fdadcef5233910d86c56cda5811b307))
* include Twilight Unlock Pro experimental patch ([740b9d4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/740b9d4fa0eabc52c650ef71070967859795f8c9))

## [1.1.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.1.0-dev.1...v1.1.0-dev.2) (2026-10-23)

### 🐛 Bug Fixes

* set MembershipDiagnosticPatch.kt patch description to Unlock Pro ([927682c](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/927682c02a782e0ab20aeb076d4f0cdf8d5647b1))
* set TrialStatusExperimentPatch.kt patch description to Unlock Pro ([313ca9a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/313ca9a33119c69624e56dc66d583a5a6a59c0d0))

## [1.1.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.1-dev.2...v1.1.0-dev.1) (2026-10-23)

### 🐛 Bug Fixes

* accurately describe experimental Outdooractive behavior ([9259566](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9259566eb787f1eb74bcb963fd9aa9702f59e466))
* accurately describe experimental Twilight behavior ([973d31d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/973d31d51f6a7548a27399c0e91fcc494a19b357))

### ✨ New Features

* import Outdooractive experimental patch ([f0bde4e](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f0bde4e543781f8a170e20fd2b55033cfa5a39fc))
* import Twilight experimental patch ([4a0d735](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/4a0d735de2bb1244faa29d2cb1d460efaf57c877))

## [1.0.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0...v1.0.1) (2026-10-23)

### 🐛 Bug Fixes

* unify Italian patch titles and descriptions ([2c95280](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2c95280fd58ec11470a0a30184c7adf0931bd1b7))

## [1.0.1-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.1-dev.1...v1.0.1-dev.2) (2026-10-23)

### 🐛 Bug Fixes

* replace outdated Leghe FC repository documentation ([e5b1eeb](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e5b1eeb8fc7950d41c7b60d067c67ddb5d249e7e))

## [1.0.1-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0...v1.0.1-dev.1) (2026-10-23)

### 🐛 Bug Fixes

* unify Italian patch titles and descriptions ([2c95280](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/2c95280fd58ec11470a0a30184c7adf0931bd1b7))

## 1.0.0 (2026-10-23)

### 🐛 Bug Fixes

* add read-only LXJX A/C constructor input logging ([223b22a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/223b22a71b5fe6b25e553ea0d8c2fc317905d0e9))
* build and inspect real Lkmpl instance in isolated diagnostic list ([df3571e](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/df3571e9627e777fb31faeaf4bc2a2811be49171))
* clean up patch menu and add isolated ArrayList logging test ([bb366c4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/bb366c4afcfad5f5277613649803a0a80fa00acc))
* improve boolean logging ([9f6b7ef](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9f6b7ef72f6bf4dba0b110ad7029723fb9fd4222))
* log LXJX collections right after inputs are loaded ([bb2bf34](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/bb2bf34fd82c7ddfcd5bafdee9fc68b8e955f6e3))
* log runtime implementation class of A list ([59832b9](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/59832b9f19ed7fc0724deb887796b7cb8638615e))
* restore README markers required for patch release ([76874b2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/76874b2b5b3d3ddb377274305552bafc0a580df7))
* return added ([1c22a1b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1c22a1be192fd9046389a5dd82315e355e1b1311))
* sintax err ([044f9d8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/044f9d81efe0fd37bbb741c8a0197fd897e1d766))
* test call ([d95629a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/d95629a080e058e44a5c6c14cdb67ef2c4534d8c))
* test pr ([a388d32](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/a388d32a75c4f0e992e5e208aa1a6653759850dd))
* trigger dev release for A list type diagnostic ([cd38c47](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cd38c47025c7f832ccbf71543cae032f30d5b630))
* update patch description and app launcher label ([e0b4e3d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e0b4e3d51a45988b6264cb61bcc960123847966e))
* withdraw faulty rewarded diagnostic patch ([ba6f8a4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/ba6f8a4c06cf2c8934cf7f6330c692c1cb3fe1df))

### ✨ New Features

* add Leghe FC 13.2.1 label test patch ([36efa84](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/36efa8486c59f01bf57a64eaa31456309d5b3d0d))
* add read-only A and C collection diagnostics ([386b923](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/386b92347a47e342f7a5cab28e0305c197f01c63))
* add safer reward completion diagnostics with post-result toast ([dda6197](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/dda61973b39a3c9d82bbebbb00f6717c3b166764))
* combine installer-source spoof with Leghe FC LAB label ([d55de74](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/d55de74c55479f4911411321db794cb3cf99e1e3))
* log original Lkqgs B state without mutation ([7c23ce7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7c23ce7012323361bca92aaf18d5d85dbd674c75))
* log recommended lineup Premium navigation branch ([95dab63](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/95dab630f8cd518a82aaa385439f9f2d251b00c2))
* replace rewarded timer diagnostic with isolated boolean iput/iget test ([fa95f0d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/fa95f0d5ccb1878be287771351f332da72d7db52))
* show actual remaining rewarded-access seconds after Y ([f89302f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f89302fcb89bc4cd58fc4f6f103649bbccaeae4b))
* show rewarded access expiry as on-device debug toast ([6f6e6da](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6f6e6da3d91d41adbf4321e8a70ea133d147c997))

## [1.0.0-dev.21](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.20...v1.0.0-dev.21) (2026-10-23)

### 🐛 Bug Fixes

* update patch description and app launcher label ([e0b4e3d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e0b4e3d51a45988b6264cb61bcc960123847966e))

## [1.0.0-dev.20](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.19...v1.0.0-dev.20) (2026-10-23)

### 🐛 Bug Fixes

* sintax err ([044f9d8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/044f9d81efe0fd37bbb741c8a0197fd897e1d766))

## [1.0.0-dev.19](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.18...v1.0.0-dev.19) (2026-10-23)

### 🐛 Bug Fixes

* test pr ([a388d32](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/a388d32a75c4f0e992e5e208aa1a6653759850dd))

## [1.0.0-dev.18](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.17...v1.0.0-dev.18) (2026-10-22)

### 🐛 Bug Fixes

* build and inspect real Lkmpl instance in isolated diagnostic list ([df3571e](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/df3571e9627e777fb31faeaf4bc2a2811be49171))

## [1.0.0-dev.17](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.16...v1.0.0-dev.17) (2026-10-22)

### 🐛 Bug Fixes

* clean up patch menu and add isolated ArrayList logging test ([bb366c4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/bb366c4afcfad5f5277613649803a0a80fa00acc))

## [1.0.0-dev.16](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.15...v1.0.0-dev.16) (2026-10-22)

### 🐛 Bug Fixes

* log runtime implementation class of A list ([59832b9](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/59832b9f19ed7fc0724deb887796b7cb8638615e))
* trigger dev release for A list type diagnostic ([cd38c47](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/cd38c47025c7f832ccbf71543cae032f30d5b630))

## [1.0.0-dev.15](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.14...v1.0.0-dev.15) (2026-10-22)

### 🐛 Bug Fixes

* log LXJX collections right after inputs are loaded ([bb2bf34](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/bb2bf34fd82c7ddfcd5bafdee9fc68b8e955f6e3))

## [1.0.0-dev.14](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.13...v1.0.0-dev.14) (2026-10-22)

### 🐛 Bug Fixes

* add read-only LXJX A/C constructor input logging ([223b22a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/223b22a71b5fe6b25e553ea0d8c2fc317905d0e9))

## [1.0.0-dev.13](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.12...v1.0.0-dev.13) (2026-10-22)

### 🐛 Bug Fixes

* test call ([d95629a](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/d95629a080e058e44a5c6c14cdb67ef2c4534d8c))

## [1.0.0-dev.12](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.11...v1.0.0-dev.12) (2026-10-22)

### 🐛 Bug Fixes

* return added ([1c22a1b](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/1c22a1be192fd9046389a5dd82315e355e1b1311))

## [1.0.0-dev.11](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.10...v1.0.0-dev.11) (2026-10-22)

### ✨ New Features

* log recommended lineup Premium navigation branch ([95dab63](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/95dab630f8cd518a82aaa385439f9f2d251b00c2))

## [1.0.0-dev.10](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.9...v1.0.0-dev.10) (2026-10-22)

### ✨ New Features

* add read-only A and C collection diagnostics ([386b923](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/386b92347a47e342f7a5cab28e0305c197f01c63))

## [1.0.0-dev.9](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.8...v1.0.0-dev.9) (2026-10-22)

### 🐛 Bug Fixes

* improve boolean logging ([9f6b7ef](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/9f6b7ef72f6bf4dba0b110ad7029723fb9fd4222))

## [1.0.0-dev.8](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.7...v1.0.0-dev.8) (2026-10-22)

### ✨ New Features

* log original Lkqgs B state without mutation ([7c23ce7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/7c23ce7012323361bca92aaf18d5d85dbd674c75))

## [1.0.0-dev.7](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.6...v1.0.0-dev.7) (2026-10-22)

### ✨ New Features

* replace rewarded timer diagnostic with isolated boolean iput/iget test ([fa95f0d](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/fa95f0d5ccb1878be287771351f332da72d7db52))

## [1.0.0-dev.6](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.5...v1.0.0-dev.6) (2026-10-21)

### ✨ New Features

* show actual remaining rewarded-access seconds after Y ([f89302f](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/f89302fcb89bc4cd58fc4f6f103649bbccaeae4b))

## [1.0.0-dev.5](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.4...v1.0.0-dev.5) (2026-10-21)

### ✨ New Features

* add safer reward completion diagnostics with post-result toast ([dda6197](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/dda61973b39a3c9d82bbebbb00f6717c3b166764))

## [1.0.0-dev.4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.3...v1.0.0-dev.4) (2026-10-21)

### 🐛 Bug Fixes

* withdraw faulty rewarded diagnostic patch ([ba6f8a4](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/ba6f8a4c06cf2c8934cf7f6330c692c1cb3fe1df))

## [1.0.0-dev.3](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.2...v1.0.0-dev.3) (2026-10-21)

### ✨ New Features

* show rewarded access expiry as on-device debug toast ([6f6e6da](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6f6e6da3d91d41adbf4321e8a70ea133d147c997))

## [1.0.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/compare/v1.0.0-dev.1...v1.0.0-dev.2) (2026-10-21)

### ✨ New Features

* combine installer-source spoof with Leghe FC LAB label ([d55de74](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/d55de74c55479f4911411321db794cb3cf99e1e3))

## 1.0.0-dev.1 (2026-10-21)

### 🐛 Bug Fixes

* restore README markers required for patch release ([76874b2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/76874b2b5b3d3ddb377274305552bafc0a580df7))

### ✨ New Features

* add Leghe FC 13.2.1 label test patch ([36efa84](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/36efa8486c59f01bf57a64eaa31456309d5b3d0d))
