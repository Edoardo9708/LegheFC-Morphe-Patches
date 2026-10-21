# Leghe FC LAB — patch Morphe non ufficiali

Repository **sperimentale** dedicato a Leghe FC, indipendente dagli sviluppatori dell'app e dal progetto Morphe.

## Prima patch: Etichetta LAB
- App: Leghe FC, package `it.quadronica.leghe`
- Versione testata staticamente: **13.2.1** (build 445)
- Effetto previsto: cambia l'etichetta dell'app in **Leghe FC LAB**
- Non modifica Premium, pagamenti, login, pubblicità o servizi online.
- La patch è **da compilare e testare**: la sola presenza del codice in GitHub non dimostra che funzioni.

## Aggiunta a Morphe Manager
Dopo la pubblicazione della prima release:
[Apri la sorgente in Morphe Manager](https://morphe.software/add-source?github=Edoardo9708/LegheFC-Morphe-Patches)

In alternativa: **Sources → + → Remote**, quindi incolla:
`https://github.com/Edoardo9708/LegheFC-Morphe-Patches`

Per le release del ramo `dev` potrebbe essere necessario attivare le **pre-release** nelle impostazioni della sorgente.


## Elenco patch pubblicate

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 Leghe FC&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 13.2.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Leghe FC - Etichetta LAB](#leghe-fc-etichetta-lab) | Rinomina l'app in Leghe FC LAB (solo test visivo). |  |

</details>

<!-- PATCHES_END -->

## Sviluppo e pubblicazione
Il repository deriva dal [template Morphe](https://github.com/MorpheApp/morphe-patches-template).
Le modifiche si eseguono sul ramo `dev`; il workflow originale `.github/workflows/release.yml` gestisce build e release tramite semantic-release.

Non modificare manualmente `patches-bundle.json`, `patches-list.json`, `CHANGELOG.md` o il workflow di release.

## Attenzione
L'APK modificato sarà firmato con una firma diversa da quello ufficiale; non tentare di sovrascrivere l'installazione originale senza aver prima fatto backup di dati e impostazioni.

Licenza del progetto: [GPLv3](LICENSE).
