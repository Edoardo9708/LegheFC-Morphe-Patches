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
| Fast-forward Git bloccato da un file non tracciato che verrebbe sovrascritto | Nel worktree `morphe-f11-source`, `MicrogTokenF13Patch.kt` era presente come esperimento locale non tracciato, ma lo stesso percorso era stato aggiunto upstream. | Confrontati i blob (identici), conservata la copia in `commit-check/preserved-MicrogTokenF13Patch.kt`, eseguito il fast-forward e ricontrollato il file upstream. Gli altri file non tracciati sono rimasti intatti. Se i contenuti differiscono serve riconciliazione esplicita: **mai usare pulizie indiscriminate**. Stato: RISOLTO. |
| Build Gradle bloccata sul plugin `app.morphe.patches:1.3.4` | Il plugin non era risolvibile nell'ambiente locale; configurazione/accesso al repository non corretti in modo definitivo. | WORKAROUND verificato con Yahoo Finance 26.21.2 e Morphe Desktop 1.20.0: `K2JVMCompiler` già distribuito con Gradle (classpath JAR Morphe), D8 dell'SDK, inserimento classi in bundle `.mpp` locale e applicazione delle sole patch selezionate tramite CLI Morphe. Quattro sorgenti Yahoo compilate e applicate; firma e installazione testate. Un errore D8 nella procedura dipendeva da **lettura negata del JAR esistente**, risolta con accesso autorizzato. La **build Gradle standard resta aperta**. |

**Nota:** quest'ultimo caso è distinto dal problema GitHub Packages HTTP 401 documentato in SKI-010: le dipendenze e i percorsi di compilazione vanno verificati separatamente.

### Android, file e installazione

| Problema / sintomo | Causa riscontrata | Soluzione adottata |
| --- | --- | --- |
| File Quick Share non trovato sul PC | Il trasferimento era arrivato sul tablet. | Cercare sul dispositivo in `/sdcard/Download/Quick Share/`, quindi copiare con `adb pull`. Verificare sempre dove è stato ricevuto il file. |
| Più dispositivi ADB collegati | Tablet USB e telefono wireless presenti insieme. | Eseguire `adb devices -l`, identificare il dispositivo e usare sempre `adb -s <seriale> ...`. Il seriale wireless va ricontrollato nelle nuove sessioni. |
| Aggiornamento APK rifiutato per firma diversa | In Yahoo Finance A1 e A2 erano state usate chiavi diverse; la chiave privata di A1 non era disponibile. | Confrontati i certificati; verificata la natura temporanea della sola installazione di test; rimossa **solo dopo autorizzazione dell'utente**. Le build successive hanno riutilizzato una chiave di test costante e `adb install -r` ha preservato dati e login. Non è una procedura che permette di aggiornare direttamente l'app firmata da Yahoo o di conservare normalmente i dati tra firme incompatibili. Log: `012-A2-tablet-install.txt`, `031-A3-GOLD-phone-install.txt`, `034-A5-phone-install.txt`, `035-A6-phone-install.txt`, `038-A7-phone-install.txt`. |
| Dubbio su base APK e split | L'originale era un archivio con base, ABI, lingua e densità; l'APK ricevuto per il test era già universale. | Ispezionare archivio, risorse e librerie prima dell'installazione. È stato verificato che l'APK universale conservasse i componenti necessari; non generalizzare che basti sempre `base.apk`. |
| Logcat troppo ampio e contenente dati estranei | Raccolta dell'intero log del dispositivo. | Raccogliere per PID/tag, conservare lo stack trace completo del crash pertinente e oscurare dati personali. Le acquisizioni generali non necessarie sono state eliminate. |

**Nota:** il problema di firma diversa ricorre anche in SKI-002; in questa voce è documentato in più il riutilizzo della medesima chiave per gli aggiornamenti successivi.

### Yahoo Finance 26.21.2 (1360038759) — casi di bytecode, login e autorizzazioni

Le verifiche si riferiscono a **Morphe Desktop 1.20.0**, branch `dev`, commit storico `1d0bf53`. I log citati sono relativi a `morphe-data/yahoo-finance-tests/` sul PC, non necessariamente presenti in Git.

#### YF-001 — Crash ART A1: salto Smali errato — RISOLTO

- **Sintomo:** crash in `Application.onCreate`; `VerifyError` per `com.yahoo.mobile.client.android.finance.subscription.a0.p(SubscriptionFeature)`: a `0x0f` il registro `v0` risultava `Object`, mentre `Log.i(String,String)` richiedeva `String`.
- **Causa verificata nel DEX:** `if-eqz` introdotto con `addInstructions` saltava a `0x0d`, dentro il logging, anziché al confronto originale; così bypassava la preparazione della stringa. Il metodo aveva due registri coincidenti con i parametri: il riutilizzo era compatibile con il percorso originale, purché la destinazione fosse corretta.
- **Tentativo fallito:** A1 installabile ma rifiutata dal verifier ART; compilare e installare **non** dimostra la correttezza dei tipi dopo i salti.
- **Rimedio:** A2 usa `addInstructionsWithLabels` e salto del ramo non `AdsFree` a `0x14`, verso il controllo originale `HAS_ACCESS`. Solo `SubscriptionFeature$AdsFree` registra il marcatore e restituisce `true`. La patch verifica preventivamente registri, opcode e riferimenti, interrompendosi se la struttura originale è cambiata.
- **Verifica:** crash A1 riprodotto su tablet; confronto bytecode A1/A2; test A2 su avvio, Home, Mercati, titolo e navigazione, con marcatori `YF-RemoveAds` e senza nuovi `VerifyError` nei log acquisiti. Successiva conferma utente della combinazione con MicroG.
- **Limiti e riferimenti:** correzione per questa versione, **non** difetto generale di `addInstructions`. `YahooFinanceRemoveAdsPatch.kt`; log `007-A1-launch-logcat.txt`, `020-A2-app-process-logcat-final.txt`, `022-A1-A2-bytecode.txt`.

#### YF-002 — Login Google dopo rifirma: routing MicroG A5 — WORKAROUND

- **Ambito/sintomo:** Yahoo Finance 26.21.2, telefono con ReVanced GmsCore `app.revanced.android.gms` 7.3.0; autenticazione non completata perché i servizi Google ufficiali rifiutavano la coppia package/certificato OAuth dell'APK rifirmato. Il provider credenziali della piattaforma non offriva il percorso MicroG richiesto.
- **Tentativo fallito:** A4 selezionava un provider alternativo e sovrascriveva `Application.bindService`, ma non intercettava il binding effettivo dei client Identity (marcatori assenti).
- **Rimedio specifico A5:** in `androidx.credentials.j.a(...)`, selezione locale del provider `CredentialProviderPlayServicesImpl` incluso nell'app usando il valore API 33; nei client `com.google.android.gms.internal.auth-api.h` e `.n`, routing delle action Identity signin/authorization verso `app.revanced.android.gms` senza dynamic lookup; in `com.google.android.gms.common.internal.g.r(int,IInterface)`, sostituzione di cinque scelte del package tramite `yfIdentityServicePackage`, limitando il routing ai due client Identity. Nel manifest, visibilità del package e metadato `app.revanced.android.gms.SPOOFED_PACKAGE_SIGNATURE` con digest **pubblico** del certificato originale verificato.
- **Verifica:** compilazione, applicazione Morphe, firma e installazione; **login riuscito confermato dall'utente**, non coperto da test automatico di autenticazione.
- **Limiti e riferimenti:** specifico di questo bytecode e dell'ambiente MicroG; non replicare automaticamente. `YahooFinanceMicrogLoginA5Patch.kt`; prototipo A4 `YahooFinanceMicrogLoginPatch.kt` **escluso dal commit**; directory locali `microg-a4/`, `microg-a5/`; log `034-A5-phone-install.txt`.

#### YF-003 — Profilo GOLD ma funzionalità bloccate — PARZIALMENTE RISOLTO

- **Sintomo:** GOLD visualizzato tramite A3, ma annunci e analisi ancora bloccati; alcuni pulsanti «Mostra altro» non aprivano i contenuti.
- **Causa verificata nel codice:** `subscription.a0.h(BESubscriptionFeature)` consulta la mappa delle autorizzazioni `SubscriptionState.f`, distinta dal livello `SubscriptionState.b`. A3 agiva sulle assegnazioni del livello nei costruttori di `subscription.y`, non sulla mappa; stato locale GOLD **non** implica accesso alle funzionalità. Può risultare `REQUIRES_UPGRADE`.
- **Soluzione parziale:** per gli annunci ha funzionato la combinazione **A2 + A3 + A5 (build A6)**, confermata dall'utente. La diagnostica **passiva** A7 registra stati HTTP e valori originali di `a0.h` senza modificarli.
- **Verifica A7:** abbonamenti HTTP 200; `quoteSummary/TSLA` 3 risposte HTTP 200; `finance/premium/insights/recent` HTTP 200; **35** risposte di controllo `REQUIRES_UPGRADE` dopo il recupero dell'abbonamento. Non osservati HTTP 401/403 o errori fatali nella raccolta. Non acquisiti payload, token, cookie o query string.
- **Stato e limiti:** annunci rimossi nella combinazione testata; analisi ancora bloccate. HTTP 200 **non dimostra** contenuto Premium completo; non sono stati correlati i singoli pulsanti alle funzionalità e ai payload, né identificate le cause precise di ogni «Mostra altro». Non è dimostrato alcuno sblocco lato server.
- **Riferimenti:** `YahooFinanceGoldTestPatch.kt`, `YahooFinanceAnalysisDiagnosticPatch.kt`; log `039-A7-analysis-results.txt` e `040-analysis-findings.txt`.

**Verifiche ancora aperte Yahoo Finance:** accesso alle analisi e singoli «Mostra altro»; associazione tra pulsante, funzionalità e contenuto risposta; build Gradle standard del plugin Morphe; portabilità di A2/A3/A5/A7 ad altre versioni Yahoo o installazioni MicroG. Le risposte HTTP 200 escludono 401/403 soltanto nelle richieste osservate, non provano contenuti completi.

**Riferimenti alle altre voci:** l'installazione bloccata dalla firma differente e il workaround di compilazione Morphe sono già registrati nelle sezioni «Android, file e installazione» e «PC, strumenti e Git», come pure il fast-forward Git bloccato da un file non tracciato. Non duplicare queste casistiche.

---

## Prezzi Benzina — 3.26.10.10 (versionCode 312610100)

- **Ambito dei test:** `org.vernazza.androidfuel`, Galaxy S23+ ARM64, Android 17.
- **Versioni e riferimenti:** prove F10–F13 e A1, Frida Gadget 17.24.3, `frida-java-bridge` 7.0.13; commit [`e207587`](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/e207587c26168629896d99acea67d81b643b4f89), release `v1.6.0`.
- **Nota:** i percorsi `morphe-test-*/` indicano artefatti/log locali, non necessariamente presenti nel repository. Non assimilare l'assenza di crash in una sessione a una prova di stabilità generale.

### PB-001 — Crash ART intermittenti all'attivazione degli hook Java Frida — WORKAROUND

- **Sintomo:** alcuni avvii F11/F12 terminano con `SIGSEGV`. Log F11 con `attempting to detach while still running code`, `GetOatMethodQuickCode`, `art_quick_to_interpreter_bridge+4`. Un processo fallisce prima di `METHOD LOOKUP OK`, un altro durante l'installazione dell'hook, senza callback osservate.
- **Causa:** **non dimostrata**. I log circoscrivono il problema all'interazione nativa con ART durante avvio del Java bridge/attivazione hook; non dimostrano difetti nella funzione originale, conversione package o modifica di `PackageInfo`.
- **Tentativi falliti:** F11: solo hook pass-through su `ApplicationPackageManager.getPackageInfo(String,int)`, logging nativo e nessuna sostituzione del risultato; crash ancora presenti. F12: inizializzazione anticipata tramite `PBApplication.<clinit>` e `Java.performNow`; ancora due crash.
- **Workaround:** F13 ricostruita **dall'APK originale**, senza Gadget o agenti Frida di prove precedenti; modifica statica del percorso richiesta token Maps. Questo evita la dipendenza dagli hook Frida, **non ne risolve la causa**.
- **Verifiche:** F11 ha avuto anche un avvio stabile: hook installato, callback terminate e heartbeat dopo 60 secondi con **76 chiamate** (conferma dell'utente), dunque il problema è intermittente. In F13 verificati assenza di `pbfrida` e mantenimento di opcode/riferimenti originali di `<clinit>` e `onCreate`. Nei log esaminati di F13 e F13+A1 nessun nuovo errore fatale.
- **Limiti:** non è dimostrata un'incompatibilità generale Frida/Android 17; non effettuati confronti controllati tra release del Gadget/bridge o serie ripetute di avvii. Un test stabile F10/F11 non basta a escludere crash successivi.
- **Riferimenti:** `morphe-test-f11/runtime-f11.log`, `crash-buffer.log`, `runtime-f11-latest.log`; `morphe-test-f12/crash-buffer-redacted.log`; `morphe-test-f13/no-frida-verification.txt`, `bytecode-verification.txt`.

### PB-002 — Sostituzione locale del certificato insufficiente per Maps — WORKAROUND

- **Sintomo:** `Google Android Maps SDK Authorization failure` persiste anche quando la diagnostica F12 stampa package e impronta SHA-1 del certificato originale.
- **Causa verificata, ma non localizzata completamente:** modificare la risposta locale di `ApplicationPackageManager.getPackageInfo(String,int)` non basta nel flusso testato. La stampa del certificato osservata **dopo** il rifiuto non dimostra quale identità sia stata inviata nella richiesta; punto preciso del fallimento F12 ignoto.
- **Tentativo fallito:** F12 anticipava l'hook e sostituiva `signatures` e/o `signingInfo` per il package target, con `GET_SIGNATURES` e `GET_SIGNING_CERTIFICATES`. L'ultimo avvio rimaneva attivo, ma Maps continuava a negare l'autorizzazione.
- **Workaround F13:** `PBApplication.bindService(Intent,ServiceConnection,int)` riconosce **soltanto** il componente `com.google.android.gms/com.google.android.gms.maps.auth.ApiTokenService`; copia l'Intent e instrada il binding a `app.revanced.android.gms` preservando nome della classe, `ServiceConnection` e flag. Nel manifest: visibilità del package e `app.revanced.android.gms.SPOOFED_PACKAGE_SIGNATURE` con digest pubblico del certificato originale. **Renderer Maps originale**, libreria GmsCore non modificata.
- **Verifiche:** `TOKEN BIND ROUTED`, `TOKEN BIND ACCEPTED`, `ApiTokenService.onBind`, `requestApiToken`, `requestApiToken success`. Processo attivo e nessun `Authorization failure` nei log F13 raccolti. Utente: «sembra funzionare». Uno screenshot disponibile mostrava un'altra app: **non è prova visiva della mappa**.
- **Limiti:** valido per la versione indicata con ReVanced GmsCore `app.revanced.android.gms` **7.2.1** installato; firma effettiva dell'APK ancora quella di test. Non è dimostrato che altri interventi su F12 fossero impossibili, né la portabilità del workaround. Necessarie prove di nuova emissione token e uso mappa/marker/zoom/spostamento.
- **Riferimenti:** `patches/src/main/kotlin/lab/prezzibenzina/patches/MicrogTokenF13Patch.kt`; `morphe-test-f12/maps-test-redacted.log`, `current-process-redacted.log`; `morphe-test-f13/runtime-redacted.log`, `manifest-verification.txt`; commit `e207587`.

### PB-003 — Rimozione banner/interstitial con F13+A1 — PARZIALMENTE RISOLTO

- **Obiettivo e sintomo:** disabilitare banner e interstitial mantenendo funzionante il percorso Maps modificato.
- **Causa/controlli individuati:** `it.prezzibenzina.pb3.general.MySettings.showBannerAds(Context)` e `showInterstitialAds(Context)`. Non è dimostrato che **tutte** le superfici pubblicitarie dipendano da questi due metodi.
- **Tentativi falliti:** non risultano tentativi falliti significativi della combinazione. Diagnostica D1 **non selezionata** assieme ad A1 perché modifica gli stessi metodi e ne richiede la forma originale.
- **Soluzione A1:** verificare la forma attesa di entrambi i metodi, quindi inserire un ritorno anticipato `false` con helper diagnostici separati. Applicare **solo F13 e A1 all'APK originale** per non includere esperimenti precedenti.
- **Verifiche:** patching, firma, bytecode e aggiornamento ADB riusciti. Log di entrambi i marcatori `FORCED RETURN = false` e inizializzazione Maps; esito riferito positivo dall'utente. **Nessuna nuova emissione token osservata** nella sessione F13+A1: dati mantenuti permettono l'uso della cache, quindi tale sessione non dimostra il rinnovo.
- **Limiti:** dimostrata l'esecuzione dei due controlli modificati, non copertura visiva sistematica di ogni banner/interstitial. A1 non modifica acquisti, licenze, entitlement Premium o storico prezzi; nessuno sblocco Premium dimostrato.
- **Riferimenti:** `patches/src/main/kotlin/lab/prezzibenzina/patches/RemoveAdsPatch.kt`, commit `e207587`; `morphe-test-f13-a1/runtime-redacted.log`, `runtime-errors.txt`, `a1-bytecode-verification.txt`, `f13-bytecode-verification.txt`, `installation-result.txt`.

### Regole riutilizzabili e verifiche mancanti

- Distinguere mediante marcatori **installazione hook, ingresso callback e chiamata originale**: un crash prima della callback non ne attribuisce la causa al corpo.
- Un certificato stampato localmente non prova l'identità trasmessa al server. Distinguere inoltre APK verificato, comportamento runtime ed esperienza riferita dall'utente.
- Per test combinati, partire dall'APK originale e selezionare esplicitamente le sole patch previste; non trascinare residui di esperimenti precedenti.
- Il routing selettivo di `ApiTokenService` verso GmsCore è specifico di questa app/versione, non soluzione universale.
- **Ancora aperto:** causa radice Frida/ART; ragione precisa dell'inefficacia del certificato F12; test ripetuti d'avvio e rinnovo token; utilizzo completo Maps (mappa, marker, zoom, spostamento); copertura di tutti gli annunci in F13+A1. Nessun accesso Premium dimostrato.

---

## Modello per le prossime voci

Usare un codice univoco (`APP-001`), nome dell'app e versione/build, sintomo osservabile, causa (**verificata** o **ipotizzata**), soluzione/tentativi falliti, modalità di verifica, risultato con limiti, eventuale commit/branch di riferimento. Documentare anche i workaround parziali e distinguere esplicitamente il funzionamento confermato dall'utente dalla sola esecuzione tecnica di una patch.
