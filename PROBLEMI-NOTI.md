# Dizionario dei problemi noti — Morphe

Archivio delle problematiche già affrontate nei lavori di patching, analisi, build, installazione e ripristino dati.

**Come usarlo:** prima di tentare una correzione, individuare la voce pertinente per applicazione, versione e sintomi. Le soluzioni documentate non sono automaticamente valide per altre build: confrontare la causa e ripetere le verifiche. Distinguere le conferme tecniche da quelle fatte dall'utente e dagli esiti parziali.

## Ski Safari — versione 2.5, build 108

- **Ambito:** Ski Safari 2.5 (108), Android, APK modificato con Morphe.
- **Branch di riferimento:** `feature/skisafari-unlock-all`
- **Commit di riferimento:** `129c9f6` (`129c9f609bfc28a8fdeba53d7b1fa6016de5ae28`)
- **Stato generale:** le soluzioni qui riportate riguardano la build 108; nessuna compatibilità garantita con altre versioni.
- **Dispositivi documentati:** tablet Samsung SM-T870 (Android 13) e Galaxy S23+ (Android 17), secondo i singoli test. La build locale 18 e l'APK successivamente ripatchato dall'utente sul telefono non sono automaticamente lo stesso artefatto.

### SKI-001 — Backup ADB limitato ai dati esterni accessibili

- **Ambito:** Ski Safari 2.5 (108); Galaxy S23+, Android 17, ADB senza root.
- **Sintomo/causa:** `run-as: package not debuggable`; la build non consente accesso ADB ai dati privati dell'app.
- **Tentativo fallito:** `run-as`. Non è stato tentato di ottenere privilegi aggiuntivi.
- **Soluzione:** backup dei file esterni accessibili, inventario, confronto SHA-256 fra telefono e PC e controllo che i dati fossero stabili durante la copia; conservazione degli APK originali.
- **Verifica:** 44 file accessibili, 17.146.650 byte; hash corrispondenti. Profilo controllato strutturalmente e tramite ricodifica XOR identica all'originale.
- **Stato:** PARZIALMENTE RISOLTO. Non è un backup della memoria privata Android: preferenze, credenziali/autenticazioni e altri dati interni non sono garantiti. `allowBackup=true` non dimostra da solo la disponibilità di un backup completo.
- **Riferimenti:** report locale del backup; `lab/skisafari/inspect-profile.py`, `lab/skisafari/README.md`.

### SKI-002 — APK modificato non installabile sopra quello originale

- **Sintomo:** Android impedisce l'aggiornamento diretto dell'APK originale con quello modificato.
- **Causa individuata:** firma crittografica differente.
- **Soluzione adottata:** conservazione dei backup e degli APK originali; successivamente disinstallazione espressamente autorizzata e installazione dell'APK modificato.
- **Risultato e limite:** installazione riuscita, ma la disinstallazione può eliminare dati interni non inclusi nel backup (vedere SKI-001).
- **Stato:** verificato sulla build indicata.

### SKI-003 — Crash PairIP dopo la rifirma dell'APK

- **Ambito:** Ski Safari 2.5 (108), ARM64; Samsung SM-T870, Android 13; successivo gameplay osservato su Galaxy S23+.
- **Sintomo:** messaggio Google Play «Download this game from Play», quindi uscita con `SIGSEGV / SEGV_ACCERR` in `libpairipcore.so` (offset PC `0x5ECC4`), prima dei log `SkiSafari-Unlock`.
- **Causa verificata/parzialmente ignota:** crash riproducibile anche rifirmando l'originale senza patch di sblocco. La correlazione con la rifirma è verificata, **non** la precisa condizione interna di PairIP.
- **Tentativi falliti:** saltare soltanto l'inizializzazione Java della licenza, APK originale unificato e rifirmato, oppure base/split mantenuti separati e rifirmati. Il merger e la funzione di sblocco non sono necessari per riprodurre il crash.
- **Workaround:** ricostruzione del percorso Java con componenti già nell'APK (`SplashActivity1`, `Yodo1LagacyUnityActivity`, inizializzazioni di `Yodo1Application1.onCreate()`); adeguamento locale dei punti di inizializzazione protetta e del costruttore Unity. **Nessuna modifica a `libpairipcore.so`.**
- **Verifica:** build locale 18 fino all'interfaccia Unity, log del profilo pronto e chiamata allo sblocco; gameplay osservato in seguito su Galaxy S23+.
- **Stato:** WORKAROUND, non soluzione generale per PairIP. Non verificati tutti i percorsi di autenticazione, annunci, acquisti e SDK virtualizzati.
- **Riferimenti:** `feature/skisafari-unlock-all`, `129c9f6`, `patches/src/main/kotlin/lab/skisafari/patches/UnlockAllItemsPatch.kt`, `lab/skisafari/README.md`, log `morphe-test-skisafari-build3/*runtime.log` e `morphe-test-skisafari-build18/runtime.log`.

### SKI-004 — Accesso prematuro al runtime IL2CPP

- **Ambito:** Ski Safari 2.5 (108) ARM64, helper JNI.
- **Sintomo:** build 15: segnale 11 dopo l'accesso al dispatcher Unity, ma prima del messaggio «profilo pronto».
- **Causa:** ottenere il dominio con `il2cpp_domain_get` e collegare un thread poteva intervenire prima che IL2CPP fosse inizializzato. Libreria caricata ≠ runtime pronto.
- **Tentativo fallito:** uso anticipato di `il2cpp_domain_get` / thread attach, basato sulla sola presenza della libreria.
- **Soluzione:** esecuzione tramite `UnityPlayer.queueGLThreadEvent(Runnable)` e controllo del thread Unity effettivo; recupero della libreria già caricata con `RTLD_NOLOAD`; attesa di `il2cpp_get_corlib` e di un `il2cpp_thread_current` già valido, senza creare il dominio né forzare l'attach. Attesa ulteriore del profilo caricato e del catalogo non vuoto.
- **Verifica:** dalla build 16 superato il crash; build 18: log in sequenza di runtime inizializzato, profilo pronto e chiamata a `ShopItemHelper.UnlockAllItems()`.
- **Stato:** RISOLTO **per il crash osservato**, non validazione generale del codice nativo. RVA, strutture e ipotesi sul thread devono essere controllati in altre build.
- **Riferimenti:** `129c9f6`, `patches/src/main/resources/skisafari/native/skisafariunlock.c`, log locali build 15/16/18.

### SKI-005 — Postcondizione di UnlockAllItems incompatibile con alcune categorie

- **Ambito:** Ski Safari 2.5 (108), catalogo e marcatore persistente della patch.
- **Sintomo:** il log conferma la chiamata a `ShopItemHelper.UnlockAllItems()`, ma poi indica «catalogo non completamente sbloccato; nessun marcatore persistente».
- **Causa:** controllo di tutti gli ID del catalogo tramite `IsItemUnlocked()` non valido per ogni categoria. Verificato che `Item_Booster.get_Unlocked` restituisce sempre `false` e che il setter è un semplice `RET`.
- **Tentativo non valido:** usare l'esito aggregato «tutte le categorie vere» come prova universale. Il successivo messaggio «target IL2CPP non pronto o non corrispondente» è eccessivamente generico per questa postcondizione.
- **Esito adottato:** nessun falso marcatore di successo quando la postcondizione fallisce. **Non** è stato corretto definitivamente il controllo per categoria.
- **Verifica:** esecuzione e ritorno del metodo nel Logcat; utente ha confermato gli oggetti di suo interesse. L'analisi statica mostra che `UnlockAllItems()` chiama `GameState.Save()` dopo il ciclo, anche senza variazioni ai flag.
- **Stato:** PARZIALMENTE RISOLTO. Non dimostrati sblocco universale e persistenza completa dopo riavvio; senza marcatore la chiamata può ripetersi in processi successivi (one-shot tra avvii incompleto).
- **Riferimenti:** `skisafariunlock.c`, README Ski Safari (sezione build 18), `morphe-test-skisafari-build18/runtime.log`.

### SKI-006 — Profilo originale ignorato dopo la reinstallazione

- **Ambito:** Ski Safari 2.5 (108), Galaxy S23+; APK infine ripatchato dall'utente sul telefono.
- **Sintomo:** copia iniziale eseguita ma progressi assenti; al controllo successivo rimaneva un profilo con ID differente, mentre il vecchio file non era più nella directory esterna.
- **Causa verificata:** il gioco usava un nuovo ID di profilo, presente nell'intestazione e utilizzato nella codifica XOR. **Non è nota la causa precisa della scomparsa del vecchio file.**
- **Tentativo fallito:** ripristinare il file con il vecchio nome. La sola rinomina non basta (SKI-007).
- **Soluzione:** salvare il profilo attivo, arrestare il gioco e adattare **una copia** del backup originale all'ID corrente (dettagli in SKI-007), poi sovrascrivere il profilo attivo con autorizzazione.
- **Verifica:** controlli strutturali e di hash del salvataggio trasferito; ripristino confermato nel gioco dall'utente («Ok funziona»). **Nessuna verifica separata documentata dopo un ulteriore riavvio.**
- **Stato:** RISOLTO **nel caso osservato**. Non identificare automaticamente l'APK finale ripatchato dall'utente con la build locale 18.
- **Riferimenti:** `inspect-profile.py`, `ONLINE-RESEARCH.md`, copie locali del profilo prima/dopo, conferma dell'utente.

### SKI-007 — Rinomina del salvataggio insufficiente: intestazione e XOR legati all'ID

- **Sintomo:** il profilo copiato non è caricato usando un ID nuovo: nome, intestazione e codifica dipendono dall'ID.
- **Soluzione verificata su Ski Safari 2.5 (108):** conservare gli originali; decodificare la copia con l'ID vecchio; sostituire **soltanto i 40 byte iniziali** dell'intestazione con il nuovo ID; ricodificare l'intero buffer con l'ID nuovo; eseguire la sostituzione del file a gioco fermo.
- **Dettaglio algoritmo osservato:** XOR con i caratteri dell'ID; indice iniziale `3 * lunghezza / 4`, incremento di 2 e ritorno a 0 al raggiungimento della lunghezza. Riferimento `Profile.Obfuscate()`, RVA `0xBD93D0`.
- **Verifiche:** corrispondenza di ogni byte **decodificato** dopo l'intestazione (compresi i progressi e le 45.198 monete); struttura parziale e ricodifica verificate; SHA-256 del file adattato sul PC uguale a quello trasferito sul telefono. Gli hash del vecchio e nuovo file **non** devono coincidere quando cambia la codifica.
- **Stato:** RISOLTO nel ripristino osservato (SKI-006); formato completo del salvataggio non decodificato. `lab/skisafari/inspect-profile.py` è **solo di lettura**, non implementa l'adattamento: per questo è stato usato uno script locale ad hoc.
- **Limite:** procedura non garantita per altre versioni o formati.

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

### SKI-010 — Build Gradle bloccata da HTTP 401 di GitHub Packages

- **Ambito:** repository Morphe, PC Windows.
- **Sintomo:** Gradle si interrompe risolvendo le dipendenze/plugin con `HTTP 401`.
- **Causa osservata:** il registro GitHub Packages non era accessibile con le credenziali utilizzate; accesso Git al repository ≠ accesso al registro pacchetti.
- **Tentativo fallito:** build Gradle standard nella configurazione disponibile.
- **Workaround:** build locale con le API del fat JAR Morphe Desktop installato, compilatore Kotlin, Java e D8 disponibili, inserendo la patch in un bundle locale di riferimento.
- **Verifica:** compilazione Kotlin/Java, merge DEX, rilevamento patch compatibile con 2.5, applicazione all'APK, firma v2 e controllo delle classi/risorse del bundle di riferimento.
- **Stato:** WORKAROUND. **La build Gradle completa resta non verificata** e la pipeline incrementale locale non equivale alla pipeline ufficiale.
- **Riferimenti:** `lab/skisafari/build-local.ps1`, `MergeApk.java`, `VerifyApk.java`, README Ski Safari. Distinguere dal problema di risoluzione del plugin `app.morphe.patches:1.3.4` documentato nella sezione PC/Git.


### SKI-011 — Valori SDK mancanti nei percorsi Java alternativi

- **Ambito:** Ski Safari 2.5 (108); Google Games, WorkManager, OkHttp, AppLovin 13.3.1.
- **Sintomi:** errori a cascata dopo il workaround PairIP durante creazione di thread, database e componenti SDK, talvolta solo a un successivo avvio.
- **Causa individuata:** mancavano valori necessari in percorsi Java raggiunti, inclusi descrittore Binder, costanti SQL, nomi colonne, `:method` HTTP/2 e impostazioni SDK.
- **Tentativo insufficiente:** correggere solo il primo errore; la semplice sopravvivenza del processo non dimostra la correttezza di tutte le inizializzazioni.
- **Workaround:** recupero puntuale da codice equivalente nell'APK o dalla medesima versione delle dipendenze: Binder Google Games dalla factory; schema `WorkName`, pragma foreign key e colonne WorkManager; `:method` dal sorgente OkHttp; impostazioni e prefissi thread dall'AAR ufficiale AppLovin 13.3.1. Aggiunto fallback limitato al nome nullo di un `HandlerThread` e ripristinati prefissi diagnostici.
- **Verifica:** build 18 avviata con superamento dei blocchi osservati; non è stata verificata la totalità degli SDK. Nessuno SDK completo sostituito e nessuna ricevuta o risposta di pagamento inventata.
- **Stato:** WORKAROUND specifico di versioni e classi analizzate.
- **Riferimenti:** `129c9f6`, `UnlockAllItemsPatch.kt`, `lab/skisafari/ONLINE-RESEARCH.md`, log build 13–18.

### SKI-012 — ResourcePatchContext.get consuma risorse native durante rebuild completo

- **Ambito:** Morphe Desktop 1.20.0; patch Ski Safari con risorse complete.
- **Sintomo:** file nativi invariati venivano consumati due volte dal rebuild quando la patch li leggeva tramite `ResourcePatchContext.get`.
- **Causa osservata:** interazione tra lettura delle risorse originali e gestione dei file nel rebuild completo del patcher.
- **Tentativo fallito:** usare `ResourcePatchContext.get` per il controllo d'identità delle librerie originali nel percorso interessato.
- **Workaround:** lettura diretta dello ZIP dell'APK sorgente per calcolare hash; accesso al file tramite i campi **privati** `config.apkFile` del contesto; arresto esplicito quando la struttura interna del patcher non coincide con quella attesa.
- **Verifica:** patch applicata, rebuild e firma completati; verificata offline l'identità dei file IL2CPP/metadati originali e la presenza dell'helper previsto.
- **Stato:** WORKAROUND fragile, legato ai dettagli privati di Morphe Desktop 1.20.0. Compatibilità con Morphe Manager o altre versioni non verificata.
- **Riferimenti:** `UnlockAllItemsPatch.kt`, README Ski Safari, artefatti locali build 18.

### Procedura consolidata per futuri ripristini Ski Safari

1. **Salvare il file di profilo attuale** e conservarne una copia integra prima di modificarlo; preservare anche backup e APK originali disponibili.
2. **Fermare completamente il gioco** prima di sostituire il file di salvataggio.
3. **Verificare gli identificativi** del profilo di origine e di quello del dispositivo di destinazione.
4. **Adattare una copia del backup:** decodificare usando il vecchio ID, cambiare l'intestazione e ricodificare usando il nuovo ID; non modificare i dati logici dei progressi.
5. **Controllare l'integrità con SHA-256** e conservare gli hash dei file originali e di quelli prodotti. **Non aspettarsi hash identici** tra due salvataggi se cambiano intestazione e codifica: il controllo serve a tracciare esattamente i file e a rilevare modifiche accidentali.
6. **Controllare i progressi nel gioco**, sul dispositivo di destinazione, prima di considerare riuscito il ripristino.


### Verifiche trasversali e questioni ancora aperte (build 18)

**Distinguere sempre le prove:** build riuscita, APK patchato e firmato, avvio Unity, esecuzione di `UnlockAllItems()`, stato degli oggetti e persistenza dopo riavvio sono verifiche differenti. Un processo vivo o uno schermo nero su dispositivo bloccato **non** dimostrano un avvio riuscito. Per analisi native, verificare librerie e metadati tramite identità binaria e istruzioni: gli RVA isolati non bastano.

**Aperto o non dimostrato:** postcondizione per categorie e one-shot tra avvii (SKI-005); persistenza dello sblocco con confronto dedicato dopo riavvio; backup e ripristino completi dei dati privati (SKI-001); build Gradle standard (SKI-010); compatibilità del workaround risorse con altri host (SKI-012); copertura completa dei percorsi SDK/virtualizzati (SKI-003, SKI-011). Non sono stati effettuati root, modifiche alle protezioni di sistema o simulazioni di transazioni/ricevute.

**Cronologia:** `129c9f6` è il commit storico documentato per la sessione/build 18. È presente anche il commit successivo `7fe0e28` relativo alla patch Ski Safari, ma **non** dedurre dallo storico che sia già incorporato nel branch o nella build del dispositivo. Il README contiene anche sezioni superate: per gli esiti sul dispositivo considerare le prove della build locale 18 e il successivo ripristino confermato sul telefono.

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
