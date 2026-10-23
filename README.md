# Patch per Leghe Fantacalcio (Leghe FC) su Android

Repository indipendente di **patch per Leghe FC**, compatibili con **Morphe Manager**. Contiene un bundle di patch non ufficiali per personalizzare l'app **Leghe Fantacalcio** su Android.

**Versione supportata:** Leghe FC **13.2.1** (build 445) · **Pacchetto Android:** `it.quadronica.leghe`

[**Aggiungi la sorgente a Morphe Manager**](https://morphe.software/add-source?github=Edoardo9708/LegheFC-Morphe-Patches) · [Release disponibili](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases) · [Segnala un problema](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/issues)

## Patch incluse

| Nome in Morphe | Descrizione |
| --- | --- |
| **Leghe FC - Avvio e nome 3 campo** | Rinomina l'app in **3 campo** e adatta il controllo locale sulla sorgente d'installazione. |
| **Leghe FC - Sblocca percentuali** | Interviene sull'indicatore utilizzato per mostrare le percentuali dei giocatori. |
| **Leghe FC - Sblocca formazione suggerita e comparatore** | Modifica i dati passati alla funzione interessata usando una lista dimostrativa e registra informazioni diagnostiche. |

**Stato dei test:** le patch sono sperimentali. Il bundle può essere compilato e distribuito, ma il nome delle patch **non garantisce** l'effettivo sblocco delle funzionalità a pagamento. In particolare, la patch per formazione suggerita e comparatore usa ancora **dati dimostrativi** e non equivale alla verifica completa del comportamento nell'app.

## Installazione

1. Installa **Morphe Manager** sul dispositivo Android.
2. Tocca [**Aggiungi la sorgente**](https://morphe.software/add-source?github=Edoardo9708/LegheFC-Morphe-Patches), oppure apri **Sources → + → Remote** e inserisci l'URL del repository:
   `https://github.com/Edoardo9708/LegheFC-Morphe-Patches`
3. Aggiorna la sorgente e seleziona **Leghe FC 13.2.1**.
4. Abilita **Expert mode** se necessario, scegli consapevolmente le patch desiderate e applicale alla tua copia dell'APK.

Per le versioni stabili lascia disattivato **Pre-release patches**; attivalo solo se vuoi provare versioni di sviluppo.

## Compatibilità e avvertenze

- Le patch sono destinate alla versione specifica indicata; nuove versioni di Leghe FC possono richiedere modifiche.
- Alcune modifiche sono sperimentali e possono causare errori o crash. Conserva un backup dei dati prima di sostituire l'app.
- La firma dell'APK modificato può differire da quella dell'APK ufficiale.
- Il bundle non sostituisce eventuali verifiche remote di licenze, acquisti o abbonamenti.

## Elenco automatico delle patch pubblicate

<!-- PATCHES_START EXPANDED -->
> **[v1.0.1](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/releases/tag/v1.0.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;3 patches total
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

<!-- PATCHES_END -->

## Sviluppo e licenze

Lo sviluppo avviene su `dev`; le release stabili sono pubblicate dal ramo `main` tramite il workflow esistente. L'elenco qui sopra viene aggiornato automaticamente durante la pubblicazione: non modificare a mano `patches-list.json`, `patches-bundle.json` e `CHANGELOG.md`.

Il repository deriva dal [template di Morphe](https://github.com/MorpheApp/morphe-patches-template). La modifica relativa alla sorgente d'installazione utilizza la [Morphe Patches Library](https://github.com/MorpheApp/morphe-patches-library) come dipendenza. Sono conservati i relativi termini e avvisi di licenza.

**Progetto indipendente:** non affiliato né approvato dagli sviluppatori di Leghe Fantacalcio o dal progetto Morphe. Il nome Morphe indica esclusivamente la compatibilità tecnica.

Licenza: [GPLv3](LICENSE). Vedi anche [NOTICE](NOTICE).
