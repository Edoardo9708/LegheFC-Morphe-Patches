# Dizionario dei problemi noti — Morphe

Archivio delle problematiche già affrontate nei lavori di patching, analisi, build, installazione e ripristino dati.

**Come usarlo:** prima di tentare una correzione, individuare la voce pertinente per applicazione, versione e sintomi. Le soluzioni documentate non sono automaticamente valide per altre build: confrontare la causa e ripetere le verifiche. Distinguere le conferme tecniche da quelle fatte dall'utente e dagli esiti parziali.

## Ski Safari — versione 2.5, build 108

- **Ambito:** Ski Safari 2.5 (108), Android, APK modificato con Morphe.
- **Branch di riferimento:** `feature/skisafari-unlock-all`
- **Commit di riferimento:** `129c9f6` (`129c9f609bfc28a8fdeba53d7b1fa6016de5ae28`)
- **Stato generale:** le soluzioni qui riportate riguardano la build 108; nessuna compatibilità garantita con altre versioni.

### SKI-001 — Salvataggi privati Android non accessibili

- **Sintomo:** impossibile recuperare tutti i dati del gioco mediante `run-as` in assenza di root.
- **Causa individuata:** APK non debuggabile (`run-as` negato) e dispositivo senza accesso root.
- **Soluzione adottata:** copiati e verificati i dati esterni accessibili.
- **Risultato e limite:** backup utilizzabile per il ripristino dei dati accessibili, **non** backup completo della memoria privata Android.
- **Stato:** risultato verificato per i dati esterni; copertura parziale del backup.

### SKI-002 — APK modificato non installabile sopra quello originale

- **Sintomo:** Android impedisce l'aggiornamento diretto dell'APK originale con quello modificato.
- **Causa individuata:** firma crittografica differente.
- **Soluzione adottata:** conservazione dei backup e degli APK originali; successivamente disinstallazione espressamente autorizzata e installazione dell'APK modificato.
- **Risultato e limite:** installazione riuscita, ma la disinstallazione può eliminare dati interni non inclusi nel backup (vedere SKI-001).
- **Stato:** verificato sulla build indicata.

### SKI-003 — APK modificato non si avviava

- **Sintomo:** il gioco non completava la sequenza di avvio dopo la modifica.
- **Causa individuata:** avvio dipendente da PairIP e da inizializzazioni Java protette.
- **Soluzione adottata:** ricostruito il percorso di avvio riutilizzando codice già presente nell'APK; recuperati alcuni valori SDK mancanti.
- **Risultato e limite:** avvio ripristinato; correzione specifica per questa versione, non trasferibile automaticamente ad altri APK o build.
- **Stato:** verificato su Ski Safari 2.5 (108).

### SKI-004 — Crash durante l'inizializzazione IL2CPP

- **Sintomo:** crash quando la patch interagiva con IL2CPP durante l'avvio.
- **Causa individuata:** accesso al runtime quando non era ancora pronto.
- **Soluzione adottata:** attesa dell'inizializzazione già prevista dall'app e successiva esecuzione sul thread Unity tramite il relativo dispatcher.
- **Risultato e limite:** sequenza corretta senza accesso anticipato al runtime; verificare nuovamente gli hook a ogni cambio di versione.
- **Stato:** verificato sulla build indicata.

### SKI-005 — Controllo dello sblocco dichiarava un risultato incompleto

- **Sintomo:** un controllo generico segnalava categorie non sbloccate dopo l'operazione.
- **Causa individuata:** il controllo includeva anche categorie, come i booster, per cui il getter restituisce sempre `false`.
- **Soluzione/verifica adottata:** verificata l'esecuzione di `UnlockAllItems()`; separato questo dato dall'effettivo stato di ogni singola categoria.
- **Risultato e limite:** l'utente ha confermato il funzionamento degli oggetti di interesse. **Non** è stato dimostrato lo sblocco di tutte le categorie.
- **Stato:** conferma utente parziale; niente dichiarazioni di sblocco universale.

### SKI-006 — Profilo copiato, ma progressi assenti sul telefono

- **Sintomo:** i file trasferiti erano presenti, ma i progressi non risultavano caricati nel gioco.
- **Causa individuata:** il gioco utilizzava un nuovo identificativo del profilo.
- **Soluzione adottata:** salvato il profilo corrente; adattati identificativo interno e codifica del backup al nuovo ID; sovrascritto il file solo a gioco fermo.
- **Risultato e limite:** ripristino dei progressi confermato dall'utente all'interno del gioco.
- **Stato:** verificato dall'utente su build 108.

### SKI-007 — La sola rinomina del file di salvataggio non basta

- **Sintomo:** cambiare nome al backup non lo rende utilizzabile da un profilo con ID diverso.
- **Causa individuata:** nome del file, intestazione e codifica XOR dipendono dall'ID del profilo.
- **Soluzione adottata:** decodifica con il vecchio ID, sostituzione dell'intestazione del profilo, ricodifica con il nuovo ID.
- **Risultato e limite:** invariati gli altri byte logici dei progressi, incluse le **45.198 monete**; il contenuto codificato del file può invece cambiare.
- **Stato:** ripristino confermato dall'utente su build 108; procedura da rivalidare per altri formati di salvataggio.

### SKI-008 — ADB non rilevava il telefono

- **Sintomo:** dispositivo assente nell'elenco dei dispositivi ADB.
- **Causa individuata:** collegamento ADB wireless assente.
- **Soluzione adottata:** ripristinato il collegamento wireless.
- **Risultato e verifica:** controllati seriale e modello del dispositivo prima di eseguire ulteriori operazioni.
- **Stato:** verificato.

### SKI-009 — ADB restituiva “Accesso negato” sul PC

- **Sintomo:** comandi ADB bloccati con errore di accesso nell'ambiente Windows.
- **Causa individuata:** restrizioni dei permessi nell'ambiente di esecuzione Windows, distinte dai problemi di connessione al dispositivo.
- **Soluzione adottata:** rieseguiti i comandi con i permessi necessari.
- **Risultato e verifica:** i comandi sono tornati eseguibili.
- **Stato:** verificato.

### SKI-010 — Build Gradle bloccata da HTTP 401

- **Sintomo:** impossibile completare la build per il recupero delle dipendenze.
- **Causa individuata:** risposta HTTP 401 da GitHub Packages per alcune dipendenze.
- **Soluzione adottata:** compilazione locale con strumenti e dipendenze Morphe già disponibili nell'ambiente.
- **Risultato e limite:** APK generato e provato sui dispositivi; il workaround non implica che l'autenticazione a GitHub Packages sia stata risolta.
- **Stato:** verificato nel contesto di build utilizzato.

### Procedura consolidata per futuri ripristini Ski Safari

1. **Salvare il file di profilo attuale** e conservarne una copia integra prima di modificarlo; preservare anche backup e APK originali disponibili.
2. **Fermare completamente il gioco** prima di sostituire il file di salvataggio.
3. **Verificare gli identificativi** del profilo di origine e di quello del dispositivo di destinazione.
4. **Adattare una copia del backup:** decodificare usando il vecchio ID, cambiare l'intestazione e ricodificare usando il nuovo ID; non modificare i dati logici dei progressi.
5. **Controllare l'integrità con SHA-256** e conservare gli hash dei file originali e di quelli prodotti. **Non aspettarsi hash identici** tra due salvataggi se cambiano intestazione e codifica: il controllo serve a tracciare esattamente i file e a rilevare modifiche accidentali.
6. **Controllare i progressi nel gioco**, sul dispositivo di destinazione, prima di considerare riuscito il ripristino.

**Confine di validità:** tutto quanto sopra è documentato per **Ski Safari 2.5, build 108**. Su altre versioni occorre riesaminare formato, ID, inizializzazione, dipendenze e comportamento.

---

## Problemi trasversali e casi Yahoo Finance — 31 ottobre 2026

Queste voci provengono dal riepilogo operativo del 31 ottobre 2026. Raccoglie problemi effettivamente incontrati e rimedi provati. I percorsi locali dell'ambiente sono documentati nel file `NOTE-OPERATIVE-PC.md` sul PC, se disponibile (non è dato per presente nel repository). Ricontrollare versioni e dispositivi nelle nuove sessioni.

### PC, strumenti e Git

| Problema / sintomo | Causa riscontrata | Soluzione adottata |
| --- | --- | --- |
| `java` non trovato dalla shell | Java installato ma assente dal PATH. | Usare il percorso completo del `java.exe` già installato, indicato nelle note operative. |
| Git segnala `dubious ownership` | Il proprietario della cartella differisce dall'utente del processo. | Per il repository verificato, usare `git -c safe.directory=C:/percorso/repository ...`, senza cambiare la configurazione globale. |
| Git non può aggiornare il repository, oppure ADB/SDK non sono accessibili dalla sessione | Restrizioni del sandbox sui percorsi e sulle operazioni. | Richiedere l'esecuzione con permessi elevati tramite gli strumenti Codex per l'operazione autorizzata. Non reinstallare gli strumenti e non aggirare un rifiuto di approvazione. |
| Errore Java apparentemente relativo a D8 / classe non trovata | Nel caso osservato, il processo non poteva leggere il JAR nell'Android SDK. | Ripetere con accesso autorizzato al JAR esistente; non assumere subito che D8 sia mancante o corrotto. |
| Variabile PowerShell `$pid` non assegnabile | `$PID` è una variabile automatica di sola lettura; i nomi non distinguono maiuscole e minuscole. | Usare un nome come `$appPid`. |
| Fast-forward Git bloccato da un file non tracciato che verrebbe sovrascritto | Un esperimento locale aveva lo stesso percorso di un file aggiunto upstream. | Verificare il contenuto, conservare il file fuori dal repository, poi effettuare il fast-forward. Nel caso osservato gli hash erano identici; verificato anche il file ricreato upstream. Non usare una pulizia indiscriminata. |
| Build Gradle bloccata sul plugin `app.morphe.patches:1.3.4` | Plugin non risolvibile nell'ambiente disponibile. | Alternativa riuscita: compilatore Kotlin già incluso nella distribuzione Gradle, D8 dell'SDK, bundle `.mpp` locale e CLI del JAR Morphe installato. Il problema della build Gradle standard resta aperto: non dichiararla riuscita. |

**Nota:** quest'ultimo caso è distinto dal problema GitHub Packages HTTP 401 documentato in SKI-010: le dipendenze e i percorsi di compilazione vanno verificati separatamente.

### Android, file e installazione

| Problema / sintomo | Causa riscontrata | Soluzione adottata |
| --- | --- | --- |
| File Quick Share non trovato sul PC | Il trasferimento era arrivato sul tablet. | Cercare sul dispositivo in `/sdcard/Download/Quick Share/`, quindi copiare con `adb pull`. Verificare sempre dove è stato ricevuto il file. |
| Più dispositivi ADB collegati | Tablet USB e telefono wireless presenti insieme. | Eseguire `adb devices -l`, identificare il dispositivo e usare sempre `adb -s <seriale> ...`. Il seriale wireless va ricontrollato nelle nuove sessioni. |
| Aggiornamento APK rifiutato per firma diversa | L'APK installato e quello nuovo erano firmati con chiavi differenti. | Controllare i certificati. Per le prove successive è stata mantenuta la stessa chiave, consentendo `adb install -r`. La prima sostituzione ha richiesto rimozione della sola installazione di test, dopo autorizzazione esplicita. Una firma diversa non permette di conservare i dati con un normale aggiornamento. |
| Dubbio su base APK e split | L'originale era un archivio con base, ABI, lingua e densità; l'APK ricevuto per il test era già universale. | Ispezionare archivio, risorse e librerie prima dell'installazione. È stato verificato che l'APK universale conservasse i componenti necessari; non generalizzare che basti sempre `base.apk`. |
| Logcat troppo ampio e contenente dati estranei | Raccolta dell'intero log del dispositivo. | Raccogliere per PID/tag, conservare lo stack trace completo del crash pertinente e oscurare dati personali. Le acquisizioni generali non necessarie sono state eliminate. |

**Nota:** il problema di firma diversa ricorre anche in SKI-002; in questa voce è documentato in più il riutilizzo della medesima chiave per gli aggiornamenti successivi.

### Bytecode e autenticazione: casi applicativi riutilizzabili

#### Crash ART dopo un'iniezione Smali — risolto

**Caso Yahoo A1:** `VerifyError` durante `Application.onCreate`. Nel DEX prodotto, `if-eqz` saltava all'indirizzo `0x0d`, dentro il blocco di logging: saltava la preparazione di una stringa e passava a `Log.i` un registro di tipo `Object`. Il verifier richiedeva `String` all'indirizzo `0x0f`.

**Rimedio provato:** sostituire `addInstructions` con `addInstructionsWithLabels`; il salto risultante puntava a `0x14`, all'ingresso del percorso originale. Verificati DEX finale, tipi dei registri e stabilità sui dispositivi. La regola riutilizzabile è controllare il DEX generato, non soltanto il testo Smali della patch.

**Riferimenti locali:** `morphe-data/yahoo-finance-tests/007-A1-launch-logcat.txt` e `022-A1-A2-bytecode.txt`.

#### Accesso Google non funzionante dopo la firma dell'APK — risolto nel caso provato

**Causa osservata:** i servizi Google ufficiali rifiutavano la coppia package/certificato OAuth dopo la nuova firma. Il primo tentativo con override di `Application.bindService` non intercettava il percorso effettivo dei client Google.

**Rimedio provato:** usare il MicroG già installato, selezionare il provider credenziali Play Services incluso nell'app e instradare soltanto i due client Google Identity verso MicroG, dichiarando nei metadati il digest pubblico del certificato originale. L'utente ha confermato il login riuscito. Questa implementazione è specifica della versione analizzata: non è una soluzione da copiare alla cieca su qualsiasi app.

#### Profilo GOLD ma annunci e contenuti ancora bloccati — diagnosi parziale

**Causa verificata:** il livello mostrato nel profilo e la mappa delle autorizzazioni delle singole funzionalità sono distinti. Cambiare il livello locale non rende vere le autorizzazioni.

**Esito:** per gli annunci è servita anche la patch dedicata `AdsFree`, funzionante nella combinazione provata dall'utente. Per le analisi, la diagnostica ha osservato HTTP 200 e 35 risultati `REQUIRES_UPGRADE`: i contenuti restano bloccati. HTTP 200 da solo non dimostra che la risposta contenga il contenuto premium completo; non è stata accertata la causa precisa di ogni pulsante «Mostra altro».

**Stato:** problema delle analisi aperto, nessuno sblocco server dimostrato. Dettagli in `morphe-data/yahoo-finance-tests/039-A7-analysis-results.txt` e `040-analysis-findings.txt`.

---

## Modello per le prossime voci

Usare un codice univoco (`APP-001`), nome dell'app e versione/build, sintomo osservabile, causa (**verificata** o **ipotizzata**), soluzione/tentativi falliti, modalità di verifica, risultato con limiti, eventuale commit/branch di riferimento. Documentare anche i workaround parziali e distinguere esplicitamente il funzionamento confermato dall'utente dalla sola esecuzione tecnica di una patch.
