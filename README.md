# Edoardo9708 Patches — Prezzi Benzina

Patch Morphe per **Prezzi Benzina** (`org.vernazza.androidfuel`), analizzate sulla versione **3.26.10.10**. La sorgente e il repository GitHub esistenti restano invariati.

[Release](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases) · [Problemi](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/issues)

## Patch

- **Prezzi Benzina - microG token routing F13**: instrada soltanto il binding Maps `ApiTokenService` verso l'installazione esistente di `app.revanced.android.gms` e dichiara il digest pubblico originale dell'app. Richiede un GmsCore compatibile con `ApiTokenService` e con il supporto alla firma dichiarata. Non sostituisce l'API key e non modifica la firma privata dell'APK.
- **Prezzi Benzina - Remove ads A1**: restituisce `false` dai controlli per banner e interstitial. Non modifica Premium, licenze, acquisti in-app o storico prezzi.

Le patch sono facoltative. F13 e A1 sono state applicate insieme e verificate su un APK di test 3.26.10.10; il comportamento può variare con altre versioni dell'app, di Maps o di GmsCore. La prova non garantisce che ogni superficie pubblicitaria sia coperta.

## Uso

Aggiungi questa sorgente a Morphe Manager tramite l'indirizzo GitHub del repository, aggiorna l'elenco e applica le patch compatibili con la versione dell'APK. Gli APK modificati sono firmati diversamente dall'originale e possono richiedere una reinstallazione con gestione attenta dei dati.

## Elenco delle patch

<!-- PATCHES_START EXPANDED -->
> **[v1.6.0-dev.16](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases/tag/v1.6.0-dev.16)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;19 patches total
<details open>
<summary>📦 Prezzi Benzina&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 3.26.10.10 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [DNS Changer - Force Pro test D2](#dns-changer-force-pro-test-d2) | Test only: forces z() to return true and logs the forced value; does not change purchases. |  |

</details>

<details open>
<summary>📦 Flowx&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 4.650 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Flowx - Test stato Gold locale](#flowx-test-stato-gold-locale) | Inserisce dati dimostrativi Gold nella cache locale dell'app per verificarne il comportamento; non modifica l'abbonamento lato server. |  |

</details>

<details open>
<summary>📦 Fonometro&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 11.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Fonometro - Remove ads](#fonometro-remove-ads) | Removes banner and interstitial ads without modifying Premium status, licenses, or in-app purchases. |  |

</details>

<details open>
<summary>📦 Leghe FC&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 13.2.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Leghe FC - Avvio e nome 3 campo](#leghe-fc-avvio-e-nome-3-campo) | Corregge l'avvio dell'app e la rinomina in 3 campo. |  |
| [Leghe FC - Sblocca formazione suggerita e comparatore](#leghe-fc-sblocca-formazione-suggerita-e-comparatore) | Formazione suggerita e confronto fra giocatori. |  |
| [Leghe FC - Sblocca percentuali](#leghe-fc-sblocca-percentuali) | Mostra le percentuali utili per scegliere chi schierare. |  |

</details>

<details open>
<summary>📦 Outdooractive&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 3.23.20 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Outdooractive - Test getter abbonamento](#outdooractive-test-getter-abbonamento) | Unlock Pro |  |

</details>

<details open>
<summary>📦 Prezzi Benzina&nbsp;&nbsp;•&nbsp;&nbsp;9 patches</summary>
<br>

**🎯 Supported versions:**

| 3.26.10.10 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Prezzi Benzina - Custom signer response F6 (ARM64)](#prezzi-benzina-custom-signer-response-f6-arm64) | Experimental: use a user-supplied public X.509 DER/Base64 certificate in the locally returned PackageInfo. Does not re-sign APK or alter server verification. Do not combine with F5. | • Custom signing certificate DER (Base64) |
| [Prezzi Benzina - Entitlement diagnostics D1](#prezzi-benzina-entitlement-diagnostics-d1) | Logs original Premium, price history and ads decisions without modifying purchases or feature access. |  |
| [Prezzi Benzina - Gadget only F7 (ARM64)](#prezzi-benzina-gadget-only-f7-arm64) | Experimental stability test: load Frida Gadget without Java bridge, Java hooks or certificate override. Does not fix Google Maps. Do not combine with F5/F6. |  |
| [Prezzi Benzina - Java bridge only F8 (ARM64)](#prezzi-benzina-java-bridge-only-f8-arm64) | Experimental stability test: load Frida Gadget and run Java.perform only, without Java hooks or certificate override. Does not fix Maps. Do not combine with F5/F6/F7. |  |
| [Prezzi Benzina - Method lookup only F10 (ARM64)](#prezzi-benzina-method-lookup-only-f10-arm64) | Experimental: resolve getPackageInfo(String,int) with Java.use, without installing any hook or modifying signatures. Do not combine with F5/F6/F7/F8/F9. |  |
| [Prezzi Benzina - No-op PackageInfo hook F9 (ARM64)](#prezzi-benzina-no-op-packageinfo-hook-f9-arm64) | Experimental stability test: intercept getPackageInfo(String,int) but return original PackageInfo unchanged. No certificate modification. Does not fix Maps. Use F9 alone, not with F5-F8. |  |
| [Prezzi Benzina - Personal Google Maps key M1](#prezzi-benzina-personal-google-maps-key-m1) | Replace the Maps SDK manifest API key with your own Google Cloud key. Enter it in patch options; no certificate spoofing. | • Your Google Maps API key |
| [Prezzi Benzina - Remove ads A1](#prezzi-benzina-remove-ads-a1) | Test: disables banner and interstitial ads without changing Premium, licenses or in-app purchases. |  |
| [Prezzi Benzina - Signature trace F5 (ARM64)](#prezzi-benzina-signature-trace-f5-arm64) | Experimental read-only inspection of returned PackageInfo signing certificates (SHA-1/SHA-256) and call stacks. No signature changes. |  |

</details>

<details open>
<summary>📦 Spotify&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 9.1.90.2270 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Spotify - No-op control](#spotify-no-op-control) | Repackages the original APK without changing app code or resources. Use alone to isolate playback regressions. |  |
| [Spotify - Previous button diagnostics](#spotify-previous-button-diagnostics) | Logs previous-track restrictions and command creation to ADB Logcat without changing playback. |  |

</details>

<details open>
<summary>📦 Twilight&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 14.32 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Twilight - Test stato trial](#twilight-test-stato-trial) | Unlock Pro |  |

</details>

<!-- PATCHES_END -->

## Sviluppo

Il codice delle patch si trova in `patches/src/main/kotlin/lab/prezzibenzina/patches`. La pipeline GitHub compila il bundle Morphe e pubblica le release stabili da `main`; le prove sperimentali vanno sviluppate su `dev`.
