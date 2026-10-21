# Leghe FC LAB — patch Morphe non ufficiali

Repository **sperimentale** dedicato a Leghe FC, indipendente dagli sviluppatori dell'app e dal progetto Morphe.

## Patch combinata: LAB e Play Store
- App: Leghe FC (package `it.quadronica.leghe`).
- Versione supportata: **13.2.1** (build 445).
- Cambia l'etichetta dell'app in **Leghe FC LAB**.
- Sostituisce con `com.android.vending` le risposte a quattro API Android usate dal codice dell'app per leggere la sorgente di installazione.
- Non modifica la sorgente reale conservata da Android, i servizi online, il login, né lo stato Premium.
- Stato: **sperimentale**, da verificare sul dispositivo con un APK reale. La patch precedente con l'etichetta LAB e la patch Morphe separata sono state testate; la combinazione singola no.

L'implementazione della modifica installer proviene da [Morphe Patches Library](https://github.com/MorpheApp/morphe-patches-library/blob/main/patch-library/src/main/kotlin/app/morphe/patches/all/misc/fix/changepackageinstaller/ChangePackageInstallerPatch.kt), utilizzata come dipendenza. Si applicano i termini GPLv3 e gli avvisi di licenza del progetto Morphe.

## Aggiunta a Morphe Manager
[Apri la sorgente in Morphe Manager](https://morphe.software/add-source?github=Edoardo9708/LegheFC-Morphe-Patches)

In alternativa: **Sources → + → Remote**, quindi incolla:
`https://github.com/Edoardo9708/LegheFC-Morphe-Patches`

Se stai usando il ramo `dev`, attiva **Pre-release patches** per la sorgente e premi **Update**. La nostra patch è disabilitata di default: attivala manualmente in **Expert mode**. Non aggiungere anche la patch universale `Change installer source`: il comportamento è già incluso.

## Elenco patch pubblicate

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.2](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases/tag/v1.0.0-dev.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 Leghe FC&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 13.2.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Leghe FC - LAB e Play Store](#leghe-fc-lab-e-play-store) | Rinomina l'app in Leghe FC LAB e simula Google Play come fonte per i controlli locali. |  |

</details>

<!-- PATCHES_END -->

## Sviluppo e pubblicazione
Il repository deriva dal [template Morphe](https://github.com/MorpheApp/morphe-patches-template).
Le modifiche si eseguono sul ramo `dev`; il workflow originale `.github/workflows/release.yml` gestisce build e release tramite semantic-release.
Non modificare manualmente `patches-bundle.json`, `patches-list.json`, `CHANGELOG.md` o il workflow di release.

## Attenzione
L'APK modificato è firmato con una firma diversa da quello ufficiale. Conserva un backup dei dati prima di qualsiasi sostituzione. Non è previsto l'aggiramento delle verifiche degli abbonamenti o dei pagamenti.

Licenza: [GPLv3](LICENSE).
