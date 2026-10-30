# Edoardo9708 Patches — Morphe

Repository unico di patch indipendenti per piu applicazioni Android, compatibile con **Morphe Manager**.

**Questo repository mantiene esattamente lo stesso indirizzo di Leghe FC**, gia presente nel catalogo Morphe. La sorgente originale non va rimossa e non e necessario aggiungerne una nuova.

[**Aggiungi la sorgente a Morphe**](https://morphe.software/add-source?github=Edoardo9708/LegheFC-Morphe-Patches) · [Release](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases) · [Problemi](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/issues)

## Applicazioni e patch

| App | Pacchetto Android | Versione analizzata | Patch |
| --- | --- | --- | --- |
| **Leghe FC** | `it.quadronica.leghe` | 13.2.1 (445) | Avvio e nome **3 campo**, percentuali, formazione suggerita e comparatore |
| **Twilight** | `com.urbandroid.lux` | 14.32 (APKM) | Test sperimentale su `TrialFilter.isTrial()` e Logcat |
| **Outdooractive** | `com.outdooractive.Outdooractive` | 3.23.20 | Diagnostica e test sperimentale dei getter di `Membership` |

### Stato dei test

- **Leghe FC:** le tre patch esistenti rimangono invariate; la patch per la formazione suggerita usa ancora dati dimostrativi. I nomi delle patch non garantiscono lo sblocco completo delle funzionalita.
- **Twilight:** patch importata dal ramo `dev` del [repository originario](https://github.com/Edoardo9708/Twilight-Morphe-Patches). Il codice imposta a `false` il valore di ritorno di `isTrial()` (`const/4 v0, 0x0`) e registra il test in Logcat. Non e stato verificato il comportamento complessivo dell'app.
- **Outdooractive:** patch importata dal ramo `dev` del [repository originario](https://github.com/Edoardo9708/Outdooractive-Morphe-Patches). Il codice registra i valori originali ma **restituisce valori dimostrativi** (`1` per `isProUser`, `2` per `getLevel`). La pubblicazione sul canale stabile non certifica il corretto comportamento della patch nell'app.

## Installazione

1. Apri Morphe Manager e aggiungi la [sorgente GitHub esistente](https://morphe.software/add-source?github=Edoardo9708/LegheFC-Morphe-Patches), se non presente.
2. Aggiorna la sorgente e seleziona l'app desiderata (Leghe FC, Twilight o Outdooractive). Se avevi gia aggiunto solo Leghe FC, potrebbe essere necessario abilitare le altre app nelle impostazioni della sorgente.
3. Per le versioni di sviluppo abilita **Pre-release patches**. La compilazione del bundle viene verificata su GitHub; il comportamento delle patch sperimentali nell'app va verificato separatamente.
4. Se necessario abilita **Expert mode** e scegli le patch da applicare alla tua copia dell'APK.

## Compatibilita

Le patch sono legate alle versioni dell'app indicate. Possono causare errori, crash o comportamenti imprevisti. Conserva i dati originali; gli APK modificati possono avere una firma diversa da quella ufficiale. Il bundle non sostituisce eventuali verifiche remote di licenze o abbonamenti.

## Struttura del progetto

Il codice delle diverse app e separato per package sotto `patches/src/main/kotlin`. Si usa un solo build Gradle e una sola pipeline di release. Il link di questa sorgente resta invariato.

## Elenco automatico delle patch pubblicate

<!-- PATCHES_START EXPANDED -->
> **[v1.6.0-dev.14](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases/tag/v1.6.0-dev.14)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;17 patches total
<details open>
<summary>📦 DNS Changer&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 3.0.0 |
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
<summary>📦 Prezzi Benzina&nbsp;&nbsp;•&nbsp;&nbsp;7 patches</summary>
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

## Sviluppo e licenze

Lo sviluppo avviene su `dev`; le release stabili sono pubblicate dal ramo `main` tramite il workflow esistente. L'elenco qui sopra viene aggiornato automaticamente durante la pubblicazione: non modificare a mano `patches-list.json`, `patches-bundle.json` e `CHANGELOG.md`.

Il repository deriva dal [template di Morphe](https://github.com/MorpheApp/morphe-patches-template). La modifica relativa alla sorgente d'installazione utilizza la [Morphe Patches Library](https://github.com/MorpheApp/morphe-patches-library) come dipendenza. Sono conservati i relativi termini e avvisi di licenza.

**Progetto indipendente:** non affiliato né approvato dagli sviluppatori delle applicazioni interessate o dal progetto Morphe. Il nome Morphe indica esclusivamente la compatibilità tecnica.

Licenza: [GPLv3](LICENSE). Vedi anche [NOTICE](NOTICE).
