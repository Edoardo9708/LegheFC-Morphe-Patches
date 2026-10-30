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
> **[v1.6.0](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases/tag/v1.6.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;2 patches total
<details open>
<summary>📦 Prezzi Benzina&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 3.26.10.10 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Prezzi Benzina - Remove ads A1](#prezzi-benzina-remove-ads-a1) | Test: disables banner and interstitial ads without changing Premium, licenses or in-app purchases. |  |
| [Prezzi Benzina - microG token routing F13](#prezzi-benzina-microg-token-routing-f13) | Local test: route only Maps ApiTokenService to the existing app.revanced.android.gms and declare the original public certificate digest. No Frida; use alone, not with F5-F12. |  |

</details>

<!-- PATCHES_END -->

## Sviluppo

Il codice delle patch si trova in `patches/src/main/kotlin/lab/prezzibenzina/patches`. La pipeline GitHub compila il bundle Morphe e pubblica le release stabili da `main`; le prove sperimentali vanno sviluppate su `dev`.
