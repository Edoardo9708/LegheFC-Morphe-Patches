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

## DNS Changer e gestione aggiornamenti Morphe — 29–31 ottobre 2026

### DNS-001 — Controllo locale Pro `Lsi2;->z()Z` — WORKAROUND

- **Ambito:** DNS Changer 3.0.0 (versionCode 64), `com.appplanex.dnschanger`, APK `DNS Changer-64-3.0.0.aspk`; patch D1 e D2.
- **Sintomo:** la diagnostica D1 su `Lsi2;->z()Z` mostrava solo `false` in molte chiamate. **Questo non prova un errore nel controllo originale**: può riflettere correttamente l'assenza di un'autorizzazione Pro.
- **Causa/struttura verificata:** metodo equivalente a `return B() || x();`, con `B()Z` associato a `is_subscribed` e `x()Z` a `is_in_app_done`. Verificati due registri, due `return v1` e una chiamata a ciascun metodo. `z()` viene realmente eseguito, ma non è dimostrato che governi da solo tutte le funzioni Pro.
- **Diagnostica D1:** helper `morpheLogZReturn(Z)V` prima dei due ritorni originali senza alterarne il risultato. Riscontro: **25 chiamate in 24,446 secondi**, tutte `false`, marcatore `z() RETURN = false [DC-20261029-D1]`. Non è un tentativo fallito: era una prova passiva.
- **Workaround D2:** ritorno immediato `true` all'ingresso di `z()`, con logging `morpheLogZReturn(true)`; non viene più eseguita la logica originale del metodo. Smali:
  
  ```smali
  const/4 v0, 0x1
  invoke-static {v0}, Lsi2;->morpheLogZReturn(Z)V
  return v0
  ```
  
  La patch verifica registri, opcode e riferimenti del metodo originale e interrompe l'applicazione se la struttura differisce.
- **Verifica:** struttura DEX analizzata; D1 verificata via Logcat; utente dopo D2: «A me sembra ok» e richiesta di pubblicazione stabile. **Non disponibile nella sessione un Logcat D2 con `z() FORCED RETURN = true [DC-20261029-D2]` né un inventario sistematico delle funzioni testate.** Release stabile `v1.5.0` include D2 secondo `patches-list.json`: la pubblicazione, da sola, non prova il comportamento.
- **Stato e limiti:** WORKAROUND specifico della build 64. Non altera direttamente Google Play Billing, SharedPreferences, acquisti persistenti o autorizzazioni server. Non provate persistenza dopo riavvio, copertura completa Pro o regressioni automatiche. Altre build richiedono nuova analisi DEX.
- **Riferimenti:** `patches/src/main/kotlin/lab/dnschanger/patches/DnsProReturnDiagnosticPatch.kt`; commit D1 `51bb26d`, `82840b5`; D2 `e57f477` su `dev`; pubblicazione selettiva `e3d914e` su `main`; release `v1.5.0`; log `logcat-d-vtime-sDNS-Pro-Diag_I'__S'.txt`; marcatori `DC-20261029-D1` / `DC-20261029-D2`.
- **Comando di diagnostica (aShell con accesso Logcat):** `logcat -d -v time -s DNS-Pro-Diag:I '*:S'`.

### MOR-001 — Avvisi di aggiornamento per app senza patch modificate — APERTO

- **Ambito:** Morphe Manager 2.2.0; repository multipatch `Edoardo9708/LegheFC-Morphe-Patches`, branch `main` / `dev`. Analizzato il sorgente ufficiale al tag `v2.2.0`; **non** ricontrollata in questa analisi la versione effettivamente installata.
- **Sintomo riportato:** modificando una sola patch nel bundle condiviso, Morphe indica aggiornamenti anche per altre applicazioni. Non è dimostrata alcuna alterazione del codice delle altre app.
- **Causa concreta:** **non individuata**. La lettura del sorgente mostra: versione `.mpp` comune; `HomeApps.checkInstalledAppsForUpdates()` confronta la versione patchata con quella disponibile; `ChangelogParser.hasChangesFor()` cerca modifiche pertinenti all'app. Esiste un fallback che segnala l'aggiornamento quando il changelog non è recuperabile o il nome app è indisponibile per il confronto. Inoltre vengono esaminate **tutte le release successive** a quella utilizzata per patchare l'app: modifiche storiche o commit privi di scope applicativo esplicito possono influire. Questi sono **meccanismi verificati nel codice, non una diagnosi del caso osservato**.
- **Tentativi/prove:** nessuna correzione eseguita e nessuna prova differenziale completata. Era stata suggerita la disattivazione delle pre-release per confronto, ma non risulta eseguita.
- **Mitigazione documentata, non soluzione:** commit di release con scope esplicito `dnschanger: publish stable Force Pro test D2 without Spotify`; workflow semantic-release continua a produrre un bundle con versione globale, non versioni per singola app. Scope coerenti (`feat(dnschanger)`, `fix(flowx)`, ecc.) facilitano il parsing, ma non assicurano l'eliminazione degli avvisi.
- **Verifiche:** utente con pre-release abilitate; changelog `dev` visualizzato in Morphe con `dnschanger / test forced true in Pro gate with D2 Logcat marker`, riconducibile a `v1.5.0-dev.9`. **La visualizzazione manuale del changelog non certifica che il recupero sia riuscito anche durante il controllo automatico degli aggiornamenti.** Nessun log della funzione di controllo, confronto per-app delle versioni registrate o esito con pre-release disattivate.
- **Stato/limiti:** APERTO. Le pre-release abilitate non sono, da sole, prova di bug. Da verificare versioni bundle memorizzate per app, intervallo di release analizzato, eventuali errori di rete/fallback, storico del changelog e confronto con pre-release disattivate.
- **Riferimenti:** sorgenti ufficiali Morphe Manager v2.2.0: `HomeApps.kt`, `ChangelogParser.kt`, `RemotePatchBundle.kt`, `MorpheAPI.kt`; test `ChangelogSubjectTest.kt`; nel repository personale `.releaserc`, `CHANGELOG.md`, `patches-bundle.json`, `patches-list.json`; release `v1.5.0` e `v1.5.0-dev.9`. [Documentazione aggiornamenti Morphe](https://github.com/MorpheApp/morphe-manager/blob/main/docs/updating-patched-apps.md).

### Regole riutilizzabili dalle due indagini

- Separare **diagnostica passiva, override funzionale, risultato osservato nell'app e persistenza**: sono prove diverse; identificare ogni revisione con marcatori differenti.
- Iniezioni Smali solo dopo controllo di fingerprint, registri, opcode e riferimenti, con abort se il metodo non coincide.
- Distinguere **versione globale del bundle** da modifiche realmente applicabili alla singola app. Usare scope applicativi chiari nei commit e riesaminare lo storico in caso di avvisi inattesi.
- **Pubblicazioni selettive:** quando `dev` contiene esperimenti non pronti per `main`, trasferire esclusivamente i sorgenti previsti. Nella pubblicazione DNS erano presenti due file Spotify sperimentali; il trasferimento selettivo li ha esclusi dalla stabile. **È una precauzione, non un incidente di pubblicazione avvenuto.**
- **Ancora aperto:** per DNS, Logcat D2 e controlli Pro sistematici/persistenza; per Morphe, causa concreta degli avvisi e confronto fra condizioni di aggiornamento. Nessun crash o errore di build DNS documentato in questa sessione.

---

## Leghe FC — migrazione 13.2.1 (445) → 13.2.2 (446), 28–31 ottobre 2026

- **Applicazione:** `it.quadronica.leghe`. Confronto statico di `leghe-fg-13-2-1.xapk` e `Leghe-Fantacalcio-Serie-A_13.2.2_apkcube.apks` (sette DEX per archivio).
- **Perimetro:** le osservazioni riguardano i metodi analizzati, non l'intera applicazione. Nessuna patch aggiornata per 13.2.2, build, installazione o prova runtime documentata in questa sessione.

### LFC-001 — Nuova offuscazione: i nomi della 13.2.1 individuano metodi diversi nella 13.2.2 — PARZIALMENTE RISOLTO

- **Sintomo:** la patch 13.2.1 si basa su classi offuscate che non corrispondono più alle stesse funzioni in 13.2.2; cercando `LXJX.invokeSuspend` nella nuova versione si trova un metodo con **3 registri**, non gli **8** attesi. L'`AppTarget` della patch esistente limita inoltre la compatibilità a 13.2.1; **non risulta un tentativo di patching 13.2.2**.
- **Causa verificata:** i DEX originali presentano corrispondenze strutturali tra il vecchio metodo `LXJX.invokeSuspend` e il nuovo `LWzU.invokeSuspend`, entrambi con **8 registri, 2 registri di parametri e 70 unità DEX**. Le **36 istruzioni su 36** conservano opcode e uso dei registri; **49 unità su 70** sono identiche e le **21 diverse** riguardano gli operandi di riferimento. Il costruttore dello stato mantiene i parametri `List, boolean, Set, String`; il costruttore dell'elemento mantiene `(JLjava/lang/String;Ljava/lang/String;JLjava/lang/String;)V`.
- **Corrispondenze individuate:** `LXJX.invokeSuspend` → `LWzU.invokeSuspend`; classe stato `Lkqgs` → `LjoEq`; classe elemento lista `Lkmpl` → `LjlPY`. I cambiamenti sono **compatibili con una nuova offuscazione**, non dimostrano nuove protezioni o una modifica della logica Premium.
- **Tentativo/approccio non valido:** cercare il vecchio nome `LXJX` restituisce il metodo sbagliato; modificare soltanto il numero dei registri attesi non risolverebbe l'errore di identificazione. Nessuna modifica o tentativo applicativo eseguito durante il confronto.
- **Procedura proposta, non implementata:** cercare metodi con fingerprint strutturali, firma, opcode e flusso; riverificare registri, parametri, riferimenti e costruttori; individuare le classi equivalenti, adattare i riferimenti Smali e `AppTarget`; interrompere il patching se la struttura non coincide.
- **Verifica:** confronto statico dei due APK originali e dei rispettivi costruttori. **Non** verificati tutti i campi di `LjoEq` (in particolare il booleano che prima si modificava in `Lkqgs.B`), né Premium, percentuali, formazione consigliata/comparatore o persistenza su 13.2.2.
- **Stato/limiti:** PARZIALMENTE RISOLTO **solo come identificazione del codice equivalente**. Compatibilità runtime non ripristinata né dimostrata; nessuna garanzia per versioni successive.
- **Riferimenti:** `patches/src/main/kotlin/lab/leghefc/patches/StateInputDiagnosticPatch.kt`, `RewardedFlowDiagnosticPatch.kt`; branch consultato `dev`; nessun nuovo commit per questo confronto.

### LFC-002 — Diagnostica A/C modifica la lista A originale — APERTO

- **Ambito:** Leghe FC 13.2.1 (445), `StateInputDiagnosticPatch.kt` sul branch `dev`, metodo `LXJX.invokeSuspend`, tag `LegheFC-LXJX`.
- **Sintomo:** i commenti descrivono un'analisi diagnostica delle collezioni A/C con lista dimostrativa separata, ma il codice **sostituisce** il valore usato nella costruzione dello stato originale.
- **Causa verificata staticamente:** l'helper `patch_logLkmplDemo()` crea e restituisce una `ArrayList` con un elemento dimostrativo `Lkmpl`. La sequenza inserita
  ```smali
  invoke-static {}, LXJX;->patch_logLkmplDemo()Ljava/util/List;
  move-result-object v1
  ```
  sovrascrive il registro `v1`, che prima conteneva la lista A originale, **prima** della costruzione di `Lkqgs`. Non è quindi una diagnostica passiva.
- **Tentativi falliti:** nessun tentativo di correzione effettuato nella sessione.
- **Correzione proposta, non applicata:** preservare il riferimento originale di `v1`; eseguire l'helper senza alterare la lista destinata al costruttore, eliminando `move-result-object v1` o utilizzando un temporaneo realmente sicuro **dopo verifica della liveness e della validità bytecode**. Controllare che le collezioni passate al costruttore restino identiche alle originali.
- **Verifica:** riscontrata nel sorgente la sovrascrittura del registro; **non** eseguito test runtime specifico per misurarne gli effetti. La deviazione del flusso dei dati è dimostrata, eventuali crash o alterazioni percepibili **non** lo sono.
- **Stato/limiti:** APERTO. Occorre chiarire se la modifica dello stato fosse intenzionale, altrimenti ripristinare la natura passiva della diagnostica, quindi testare runtime e assenza di regressioni.
- **Riferimenti:** `StateInputDiagnosticPatch.kt`, `patch_logLkmplDemo()`, `LXJX.invokeSuspend`, registro `v1`; branch `dev`, nessun nuovo commit.


**Aggiornamento storico (23/10/2026, Leghe FC 13.2.1):** l'helper `patch_logLkmplDemo()` usa registri propri e crea una `ArrayList` con un elemento dimostrativo `Lkmpl`. La restituisce con `return-object v0`; il chiamante acquisisce il riferimento con `move-result-object`. **I registri dei due metodi non sono condivisi**: l'helper può restituire `v0` e il chiamante memorizzare in un registro diverso. Nel sorgente pubblicato su `dev` il risultato è assegnato a `v1`, usato come argomento `List` in `Lkqgs.<init>`: confermata la sostituzione della lista A (non diagnostica passiva). La vecchia firma `()V` non sarebbe compatibile con l'uso di `move-result-object`, ma non è documentata una prova runtime di questa combinazione. La correzione sintattica LFC-003 **non risolve** la sostituzione dello stato. **Stato invariato: APERTO.** Non testati separatamente formazione consigliata, comparatore o persistenza. Commit storici `e0b4e3d`, `2c95280`.


### LFC-003 — Opcode Smali errato `move -result-object` — RISOLTO (parser)

- **Ambito:** Leghe FC `it.quadronica.leghe`, versione **13.2.1 build 445**; Morphe Manager 2.0.0, Modificatore 2.0.0, Android 17 (API 37), bundle storico `Leghe FC LAB Patches 1.0.0-dev.19`. Sorgente `StateInputDiagnosticPatch.kt`.
- **Sintomo:** l'applicazione della patch `Leghe FC - Diagnostica ingresso LXJX` si interrompe con `PatchException: Encountered 1 parser syntax errors and 0 lexer syntax errors!`. Stack: `InlineSmaliCompiler.compile(InlineSmaliCompiler.kt:59)` → `InstructionExtensions.addInstructions(InstructionExtensions.kt:146)` → `StateInputDiagnosticPatch.kt:161`.
- **Causa verificata:** la stringa multilinea inserita da `method.addInstructions(afterInputsReady, ...)` conteneva `move -result-object v4`; l'opcode corretto è **un unico token**: `move-result-object`.
- **Tentativo fallito:** applicazione della stringa Smali malformata; non risultano altri tentativi significativi.
- **Soluzione:** correggere in `invoke-static {}, LXJX;->patch_logLkmplDemo()Ljava/util/List;` seguito immediatamente da `move-result-object v4`. Verificare firma del metodo chiamato `Ljava/util/List;` e ritorno `return-object` coerente. **Questo risolve la sintassi, non la semantica della sostituzione dei dati.**
- **Verifica:** l'utente ha confermato «funziona» dopo la correzione; errore del parser superato. Nel sorgente attuale su `dev` compare `move-result-object v1`, sintatticamente valido: il registro differisce dall'esempio storico `v4`.
- **Stato e limiti:** RISOLTO l'errore di parser; nessuna prova runtime conclusiva per ART, percentuali, formazione suggerita, comparatore, Premium o persistenza. Confrontare con **LFC-002**, che resta aperto.
- **Riferimenti:** `StateInputDiagnosticPatch.kt`, `method.addInstructions(afterInputsReady,...)`, `patch_logLkmplDemo()`; release `v1.0.0-dev.19`, correzione nelle revisioni successive verso `v1.0.0-dev.20`.


### Regole riutilizzabili e attività aperte

- **Nomi offuscati ≠ identità dei metodi.** Ad ogni versione confrontare firme, tipi, opcode, riferimenti e struttura del flusso, anche quando un vecchio nome continua a esistere.
- **Una diagnostica passiva non deve modificare i dati usati dall'app.** Verificare liveness dei registri, istruzioni iniettate e valori consegnati ai costruttori.
- **Livelli di prova separati:** equivalenza statica, compilazione, applicazione, runtime, funzionamento effettivo e persistenza non sono intercambiabili.
- **Aperto:** implementare/verificare supporto 13.2.2; confermare i campi dello stato `LjoEq` incluso il booleano equivalente a `Lkqgs.B`; correggere/chiarire la diagnostica A/C; eseguire test runtime e persistenza; valutare fingerprint strutturali più robusti.

---

## Fonometro — 11.5 (versionCode 82), 28–31 ottobre 2026

- **Applicazione:** `com.ktwapps.soundmeter`; APK `Fonometro-82-11.5.aspk`, DEX `classes2.dex`, patch Morphe `DisableAdsPatch.kt`.
- **Perimetro:** l'utente ha confermato il risultato della patch; non sono disponibili nella sessione log runtime o test sistematici delle superfici pubblicitarie. Le problematiche generali di firma e installazione restano nelle sezioni trasversali.

### FON-001 — Disabilitazione banner/interstitial senza alterare Premium — RISOLTO (caso d'uso)

- **Sintomo/obiettivo:** eliminare banner e interstitial evitando modifiche ad acquisti, licenze ed entitlement Premium.
- **Struttura identificata:** `Lc5/v;->f(Context)I` legge la preferenza locale `premium` (default `-1`); `MainActivity.C1()` carica banner e `n1()` li rimuove; `Detail.m1()` gestisce annunci e `Z0()` rimuove il banner nei dettagli; `Lc5/a.l(Activity)` e `m(Activity)` caricano annunci; `Lc5/a.o(Activity,I)` gestisce interstitial con callback `Lc5/a$e.E(I)`.
- **Soluzione:** patch `Fonometro - Remove ads`, `default = false`, compatibilità limitata a `11.5`. All'inizio di `MainActivity.C1()` invoca `n1()`, sgancia riferimenti annunci tramite `Lc5/a.i()`/`j()`, richiama `j1()` e termina. In `Detail.m1()` richiama `Z0()` e termina. In `Lc5/a.l()` e `m()` inserisce `return-void` per evitare caricamenti. In `Lc5/a.o()` chiama `Lc5/a$e.E(I)` con l'argomento originale, poi termina: **non** salta indiscriminatamente la callback al chiamante.
- **Controlli di sicurezza:** fingerprint di classe/firma, conteggio registri, riferimenti attesi, e stop esplicito se il bytecode non coincide. Lo stato Premium e il Billing non sono alterati dalla patch.
- **Tentativi falliti:** nessuno documentato; non è stato tentato di modificare Premium.
- **Verifica:** sorgente e presenza nel catalogo Morphe controllati; pre-release `v1.4.0-dev.1`; l'utente ha detto «Per me è già ok» e ha chiesto la pubblicazione stabile. Non risultano test automatici dedicati, log runtime, schermate prima/dopo o test esaustivi.
- **Stato/limiti:** RISOLTO **per l'utilizzo confermato dall'utente**, con copertura tecnica parziale. Da verificare callback interstitial via Logcat, tutte le schermate/formati, richieste residue agli SDK, comportamento dopo riavvio e bundle stabile `v1.8.0`. Nessuna garanzia per versioni successive a 11.5.
- **Riferimenti:** `patches/src/main/kotlin/lab/fonometro/patches/DisableAdsPatch.kt`; commit `2b6f67d`, `dev`; pre-release `v1.4.0-dev.1`.

### FON-002 — Escaping di classe DEX annidata nei literal Kotlin — RISOLTO (sorgente)

- **Sintomo:** nel codice Kotlin che compone Smali, il nome DEX annidato `Lc5/a$e;` deve contenere un dollaro letterale, mentre `$` è interpretato come interpolazione dal compilatore Kotlin.
- **Causa:** usare il riferimento senza un escaping corretto può creare un errore di compilazione o un riferimento Smali errato. Un tentativo intermedio di correzione non ha prodotto la rappresentazione finale voluta; **non è documentato un errore Gradle specifico**.
- **Soluzione:** rappresentare il dollaro con `${'$'}` anche nelle stringhe multilinea, sia nei riferimenti al tipo del campo `d` sia nelle chiamate `invoke-interface`. Esempio Kotlin: `"Lc5/a${'$'}e;->E"`.
- **Verifica:** rilettura di `DisableAdsPatch.kt` su `dev`; riferimenti Kotlin corretti nel sorgente e catalogo successivamente generato. Non disponibile una prova separata di fallimento/successo compilazione per ogni revisione intermedia.
- **Stato/limiti:** RISOLTO nel sorgente. Regola riutilizzabile per classi DEX annidate nominate nelle stringhe Kotlin.
- **Riferimenti:** `DisableAdsPatch.kt`; commit intermedi `cb3cddd`, `7493a4b`; metodo `Lc5/a$e;->E(I)V`.

### FON-003 — Pubblicazione stabile senza metadati pre-release — RISOLTO (integrazione)

- **Ambito/sintomo:** nel confronto `main...dev` comparivano anche `CHANGELOG.md`, `README.md`, `gradle.properties`, `patches-list.json`, `patches-bundle.json` generati da semantic-release per `dev`. Un merge indiscriminato avrebbe trasferito metadati del canale di sviluppo su `main`.
- **Causa:** il processo di release aggiorna metadati e catalogo durante le pre-release. **Non** si è verificato un merge errato: la divergenza è stata riconosciuta preventivamente.
- **Soluzione:** aggiornare su `dev` la denominazione `Fonometro - Remove ads` e la descrizione inglese `Removes banner and interstitial ads without modifying Premium status, licenses, or in-app purchases.`; creare `release/fonometro-remove-ads` da `main`; trasferire **solo** `DisableAdsPatch.kt` e verificare diff di un unico file; squash merge della PR #12; lasciare alla pipeline la rigenerazione dei metadati stabili.
- **Verifica:** PR #12 integrata, commit `e7101ee`; sorgente su `main` con descrizione corretta, catalogo `patches-list.json` contenente Fonometro; `patches-bundle.json` ha successivamente riportato `v1.8.0`, mentre il controllo iniziale riportava ancora `v1.3.0`.
- **Stato/limiti:** RISOLTO per integrazione e catalogo. La pubblicazione non prova il comportamento sul dispositivo del bundle stabile `v1.8.0`; nessun test d'installazione di tale bundle documentato. È un caso concreto della regola di pubblicazione selettiva già riportata per altre patch: **non** assume un errore di merge effettivo.
- **Riferimenti:** commit descrizione `997ea57`, PR [#12](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/pull/12), commit `e7101ee`, ramo `release/fonometro-remove-ads`, `.releaserc`, `patches-list.json`, `patches-bundle.json`.

**Caso aggiuntivo (release `v1.3.0`, PR #9/#10; distinto dalla pubblicazione Fonometro PR #12):** il confronto `main...dev` mostrava fino a 69 commit, ma solo sei file diversi (cinque generati e il sorgente Flowx, modificato esclusivamente in nome/descrizione). La PR #9 era integrabile, ma fu **chiusa senza merge** per changelog duplicato e metadati Flowx imprecisi. Dopo ripristino workflow, pulizia changelog e modifica del testo Flowx, fu pubblicata la dev `v1.3.0-dev.2` e **integrata la PR #10 con merge senza squash** (commit `8938194`), preservando la cronologia richiesta dal template. `semantic-release` generò la stabile `v1.3.0`; bundle `patches-1.3.0.mpp` da **1.342.804 byte**, MIME `application/dash-patch+xml`, workflow [40746604764](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/actions/runs/40746604764) riuscito, inclusa attestazione. Alla fine di **quella sessione storica** `main` e `dev` erano sincronizzati al commit `34dffe3`; ciò non descrive necessariamente il loro stato attuale. Non è stata modificata la logica delle patch in tale promozione e la release non equivale a test applicativo. Per dettagli sul changelog e i metadati Flowx: MOR-REL-002 e FLOWX-META-001.

### Regole riutilizzabili e verifiche mancanti

- Distinguere **caricamento, visualizzazione, rimozione e callback** della pubblicità: un ritorno anticipato non deve eliminare una notifica ancora necessaria.
- Riutilizzare, quando presenti, i percorsi originali di pulizia UI per non confondere rimozione degli annunci e cambio dello stato Premium.
- Nelle stringhe Kotlin destinate a Smali controllare il carattere `$` letterale dei nomi di classi annidate, comprese le stringhe multilinea.
- Verificare fingerprint/registri/riferimenti prima di patchare; pubblicare selettivamente i sorgenti lasciando che semantic-release generi i metadati per il canale corretto.
- **Ancora aperto:** Logcat dell'effettiva callback interstitial; copertura completa di schermate/formati e richieste residue; prova dopo arresto/riavvio; test runtime con bundle stabile `v1.8.0`; analisi su versioni diverse da `11.5`.

---

## Twilight — versione 14.32 (617), 23 ottobre 2026

- **App:** `com.urbandroid.lux`, APKM (APK base e split configurativi), `classes.dex`. **Repository originale della patch:** [Twilight-Morphe-Patches](https://github.com/Edoardo9708/Twilight-Morphe-Patches), branch `dev`. Le voci qui riportate riguardano la pre-release `v1.0.0-dev.4`, non l'efficacia dimostrata della modifica sul dispositivo.
- **Nota di ambito:** nel repository Twilight, al momento dell'analisi, `main` conteneva il template iniziale; `dev` conteneva la patch sperimentale e quattro pre-release. La pubblicazione di un bundle `.mpp` non dimostra installazione e funzionamento dell'APK patchato.

### TWI-001 — Log `isTrial()` incoerente con il valore effettivamente restituito — APERTO

- **Sintomo verificato nel sorgente:** il messaggio diagnostico è `TW-isTrial=true`, ma la patch forza il ritorno booleano `false`. Estratto:
  
  ```smali
  const-string v0, "TW-isTrial=true"
  invoke-static {v0, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
  const/4 v0, 0x0
  return v0
  ```
  
  Non è un risultato Logcat osservato sul dispositivo: è una **incoerenza statica** tra testo e istruzioni.
- **Causa:** la stringa di log è costante, non deriva dal valore restituito. Il commit `643ba68` ha modificato il ritorno da `0x1` a `0x0`, senza aggiornare il log né la descrizione («Fa restituire true...»).
- **Dettaglio DEX:** l'originale `TrialFilter.isTrial():Z` usa un registro (`registers_size=1`, `ins_size=1`), legge il campo `isTrial` e lo restituisce. `addInstructions(0,...)` inserisce un ritorno anticipato; `v0` coincide inizialmente con `p0`, viene riutilizzato per entrambe le stringhe di `Log.d` e poi per `const/4 v0, 0x0`. Il codice originale rimane nel metodo ma normalmente non è raggiungibile dopo il ritorno inserito. `TrialFilter.reevaluate()` non è stato modificato.
- **Tentativi:** modifiche sperimentali del valore forzato in più commit; **nessun crash o VerifyError documentato**. Le variazioni true/false non costituiscono di per sé test del funzionamento.
- **Correzione proposta (non implementata):** allineare descrizione e messaggio al ritorno reale. Se si mantiene `false`, usare `TW-isTrial=false` (oppure creare un log legato al valore effettivo); usare `true` nel messaggio solo quando il metodo restituisce effettivamente `0x1`.
- **Verifica svolta:** struttura del metodo originale verificata nel DEX, confronto di sorgente e commit; workflow Release completato e bundle `patches-1.0.0-dev.4.mpp` pubblicato. **Nessun Logcat runtime, applicazione patch, installazione o test funzionale documentato.**
- **Stato/limiti:** APERTO; difetto diagnostico verificato, correzione non applicata. Non dimostrati sblocco Pro, eventuali controlli di licenza aggiuntivi, avvio stabile o persistenza dopo riavvio.
- **Riferimenti:** `patches/src/main/kotlin/app/edoardo9708/twilight/patches/TrialStatusExperimentPatch.kt`, `TrialStatusFingerprint`, `TrialFilter.isTrial()`; commit `4bab57f`, `b4149aa`, `2afedeec`, `be27343`, `643ba68`; `v1.0.0-dev.4`.

### TWI-002 — GitHub Actions non può creare automaticamente la Pull Request — APERTO

- **Sintomo:** il workflow `Open a PR to main` fallisce sul branch `dev` con:
  
  ```text
  GraphQL: GitHub Actions is not permitted
  to create or approve pull requests
  (createPullRequest)
  ```
  
  Il job termina con exit code 1, pur essendo indipendente dal workflow Release.
- **Causa verificata dal messaggio di GitHub:** al token usato da GitHub Actions non è consentita la creazione di Pull Request nel repository, nonostante il workflow dichiari `pull-requests: write`. Non è un errore di build Kotlin o della patch.
- **Tentativi falliti:** esecuzioni ripetute del workflow, incluso il commit `643ba68`; nessuna modifica delle autorizzazioni documentata.
- **Procedura proposta, non verificata:** nel repository Twilight, `Settings > Actions > General > Workflow permissions`, abilitare `Allow GitHub Actions to create and approve pull requests`, quindi rieseguire il workflow e verificare che la PR venga creata. Non è stata effettuata la modifica e potrebbero emergere ulteriori errori dopo averla abilitata.
- **Verifiche:** log del workflow fallito con errore GraphQL; workflow `Release` completato separatamente con pre-release `v1.0.0-dev.4` e bundle `.mpp`. La riuscita della release **non** implica riuscita della PR.
- **Stato/limiti:** APERTO; correggere i permessi richiede ancora un'azione e una prova del risultato.
- **Riferimenti:** `.github/workflows/open_pull_request.yml` nel repository Twilight; [workflow PR fallito, run 39969220531](https://github.com/Edoardo9708/Twilight-Morphe-Patches/actions/runs/39969220531); [workflow Release riuscito, run 39969220587](https://github.com/Edoardo9708/Twilight-Morphe-Patches/actions/runs/39969220587); commit `643ba68`.

**Aggiornamento comparativo (altro repository, non correzione Twilight):** in `Edoardo9708/LegheFC-Morphe-Patches` lo stesso errore GraphQL è stato risolto dall'utente abilitando `Settings > Actions > General > Workflow permissions > Allow GitHub Actions to create and approve pull requests`. Il run [40742453207](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/actions/runs/40742453207) inizialmente fallito è riuscito rieseguendo il job `123007350279`; create automaticamente PR #9 e, successivamente, PR #10 (poi integrata). Il permesso è specifico del repository: **TWI-002 resta APERTO** finché il repository Twilight non è corretto e verificato autonomamente.

### Regole riutilizzabili e verifiche ancora necessarie

- Un messaggio Logcat costante non certifica il valore restituito: mantenere coerenti **bytecode, log e descrizione** e verificare i registri quando si aggiunge logging Smali.
- `addInstructions(0,...)` con `return` cambia il flusso di esecuzione senza modificare necessariamente il campo o lo stato originale. Distinguere **compilazione, pubblicazione, patch applicata, installazione, runtime e persistenza**.
- Le pipeline Release e creazione PR sono indipendenti: il successo della prima non risolve problemi di autorizzazione della seconda.
- **Aperto:** correggere log/descrizione; acquisire Logcat dopo patch; testare avvio, funzionalità interessate, controlli licenza indipendenti e persistenza; abilitare e verificare i permessi di creazione PR.

---

## Manutenzione release Morphe e metadati Flowx — 28 ottobre 2026

**Ambito:** `Edoardo9708/LegheFC-Morphe-Patches`; GitHub Actions, Gradle, Morphe Patches Plugin e `semantic-release`. Sono incidenti storici distinti dal funzionamento runtime delle singole patch. Le regole generali sulle pubblicazioni selettive erano già documentate in FON-003.

### MOR-REL-001 — Recuperi temporanei rimasti nel workflow di release — RISOLTO (workflow)

- **Sintomo:** `.github/workflows/release.yml` nel canale `dev` conteneva 166 righe, contro le 59 del template ufficiale `MorpheApp/morphe-patches-template`, con recuperi specifici per `v1.2.0-dev.12` e `v1.3.0-dev.1`.
- **Cause storiche documentate nel workflow:** aggiunta di una procedura per ricostruire `refs/notes/semantic-release-v1.2.0-dev.12` mancante, `continue-on-error` per proseguire dopo `semantic-release`, recupero con `./gradlew publish`, verifica dei DEX nel `.mpp` e cancellazione/ricaricamento degli asset tramite GitHub API. **La causa radice originaria del problema delle Git note non è stata riprodotta.** Questi recuperi erano serviti in passato ma non erano più appropriati come percorso ordinario; potevano intervenire sugli asset già pubblicati.
- **Soluzione:** sostituito **solo** il workflow `dev` con quello identico al template ufficiale; eliminati recuperi temporanei e percorso di pubblicazione alternativo; nessuna patch Kotlin modificata.
- **Verifica:** identità con il template tramite contenuto e SHA Git; commit [`6b80f199`](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/6b80f199966172e94567abd090eeb389b4463adb); run [40742453189](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/actions/runs/40742453189) riuscito; pubblicata `v1.3.0-dev.1` con asset `.mpp`; workflow successivi su `dev` e `main` completati.
- **Stato/limiti:** RISOLTO il ripristino del workflow, **non** la causa storica delle Git note. Successi futuri non garantiti in caso di nuove anomalie di sincronizzazione.

**Aggiornamento storico (24/10, `v1.2.0-dev.12`):** il primo `semantic-release` aveva creato il tag `v1.2.0-dev.12`, ma il push di `refs/notes/semantic-release-v1.2.0-dev.12` fu rifiutato con `missing necessary objects`. Nei run successivi `git tag v1.2.0-dev.12` falliva con exit code 128 perché il tag esisteva già. Il recupero manuale sostituiva l'asset pubblicato **sotto lo stesso numero di versione**, rendendo impossibile distinguere in modo affidabile sorgenti e bundle dal solo numero `dev.12`; il tag talvolta esisteva senza una GitHub Release corrispondente e i sorgenti differivano tra tag e `dev`. È documentato il successivo ritorno al workflow ufficiale, **non la causa radice del rifiuto Git notes**. Ripetere job o ricaricare un asset non fornisce una nuova identità di versione; preferire release distinte. Non è stato dimostrato che la cache Morphe abbia effettivamente conservato un bundle obsoleto. Riferimenti: commit `6144e22`, `4550b49`, `e5c2ddf`; workflow `40079801244`, `40080613788`, `40081316851`.

### MOR-REL-002 — Sezioni duplicate di `1.2.0-dev.12` nel changelog — RISOLTO

- **Sintomo:** `CHANGELOG.md` conteneva **sette** intestazioni `1.2.0-dev.12`, con date ed elenchi di commit in parte diversi, e mancavano le sezioni stabili `1.0.1`, `1.1.0` e `1.2.0`.
- **Causa verificata:** ripetuti recuperi della stessa prerelease avevano accumulato sezioni progressivamente più estese ma associate allo **stesso numero di versione**. I recuperi precedenti avevano consentito di proseguire senza pulire il file.
- **Soluzione:** preso il `CHANGELOG.md` dal tag pubblicato `v1.2.0-dev.12`; identificata e preservata **testualmente** la sezione originale; eliminate sei duplicazioni e ripristinate da `main` le tre sezioni stabili, lasciando intatte le altre prerelease.
- **Verifica:** sezioni dev.12 da 7 a 1, tre sezioni stabili recuperate, sezione originale identica al tag. Commit [`b634777`](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/b63477741332404be5a867be0761c2791284982c) con **solo** `CHANGELOG.md`. Run [40744561955](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/actions/runs/40744561955) riuscito senza creare una nuova release.
- **Stato/limiti:** RISOLTO il file storico identificato. Non corregge retroattivamente le note delle release GitHub né impedisce automaticamente nuovi duplicati.

**Precisazione storica `dev.12`:** le sette sezioni con lo stesso numero erano correlate ai tentativi di recupero/ripubblicazione con versione riutilizzata dopo il rifiuto del push delle Git notes (vedi MOR-REL-001). La rimozione delle duplicazioni corregge il changelog, non l'identità degli asset storicamente sostituiti. L'ipotesi di cache locale Morphe obsoleta non è stata confermata.

### MOR-REL-003 — Bundle `.mpp` ZIP valido ma privo del payload DEX — PARZIALMENTE RISOLTO

- **Ambito:** repository Morphe multipatch, prerelease `v1.2.0-dev.12`, Morphe Manager **2.1.0**, GitHub Actions, Gradle, `semantic-release` (24 ottobre 2026).
- **Sintomo:** dopo l'aggiornamento Morphe riconosceva sorgente e versione `dev.12`, ma mostrava **zero applicazioni**. Logcat: `Failed to load bundle Edoardo9708 Patches`; `IllegalStateException: Patch bundle is corrupted or incomplete`; causa interna `PatchException: APK contains no DEX files to patch`. **L'errore riguardava il bundle**, non gli APK da modificare.
- **Causa verificata:** durante il recupero manuale era stato caricato il primo `.mpp` generato **prima** della fase finale `Gradle publish`. Il file era leggibile come ZIP ma **non conteneva i DEX** necessari a Morphe. Non bastavano esistenza, dimensione, apertura come ZIP o correzione del MIME type.
- **Tentativi non risolutivi:** ripubblicazione del bundle preliminare; verifiche superficiali ZIP/dimensione; modifica del MIME senza rifare il payload.
- **Workaround adottato:** eseguire `./gradlew publish` e verificare **il bundle finale**, cercando file `.dex` sia tra le voci ZIP sia in eventuali JAR interni, prima di caricarlo su GitHub.
- **Verifica:** nel workflow [`40085781321`](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/actions/runs/40085781321) risultano `DEX files found: ['classes.dex', 'generated/extensions/signature.dex']`. Il file `patches-1.2.0-dev.12.mpp` è stato ripubblicato con MIME `application/dash-patch+xml`, dimensione **1.342.529 byte**; workflow riuscito.
- **Stato/limiti:** PARZIALMENTE RISOLTO. È verificata la correzione strutturale del bundle, **non** una successiva prova positiva del caricamento delle applicazioni in Manager, né compatibilità con versioni Manager diverse dalla 2.1.0. Il controllo sui DEX era un recupero storico, poi eliminato dal workflow ordinario durante MOR-REL-001; non affermare che faccia tuttora parte del workflow ufficiale.
- **Riferimenti:** `.github/workflows/release.yml`, `gradle.properties`, log `morphe_logcat-2026-10-24-112220.log.txt` alle 11:20:37 e 11:20:48; commit `1c4a850`, `2dfb2db`. MOR-REL-001 per successivo ripristino della pipeline.


### MOR-REL-005 — Release `v1.0.1` pubblicata ma backmerge fallito — PARZIALMENTE RISOLTO (storico)

- **Ambito:** repository multipatch Morphe, GitHub Actions, `semantic-release`, `@cleyrop-org/semantic-release-backmerge`, branch `dev`/`main`; release stabile `v1.0.1` (23/10/2026).
- **Sintomo:** workflow `Release` in stato `failure` **dopo** pubblicazione riuscita di `v1.0.1` e `patches-1.0.1.mpp`. Log: `Performing backmerge with "rebase" strategy` e `Command failed with exit code 1: git rebase origin/main`. Conflitti in `CHANGELOG.md`, `README.md`, `gradle.properties`, `patches-bundle.json`, `patches-list.json`.
- **Causa verificata:** il plugin tentava il rebase del commit prerelease `v1.0.1-dev.1` su `origin/main` mentre i metadati generati della stabile `v1.0.1` divergevano. Il problema riguardava **solo la sincronizzazione successiva alla pubblicazione**, non build o caricamento del bundle.
- **Tentativo fallito:** `git rebase origin/main` sul commit `dc54c3f`, bloccato da conflitti multipli.
- **Soluzione durante la sessione:** nessuna correzione del backmerge. README aggiornato separatamente sui due branch, mantenendo i rispettivi blocchi generati. **Aggiornamento storico successivo:** configurazione documentata con `backmergeStrategy: "merge"` e `mergeMode: "ours"`; sincronizzazione riuscita in altra release (vedere MOR-REL-001 e il riepilogo delle promozioni), ma **non** è una prova di risoluzione del caso `v1.0.1` né di gestione di futuri conflitti.
- **Verifica:** fasi Gradle e GitHub publish riuscite; release e bundle `patches-1.0.1.mpp` presenti; fallimento nel backmerge indicato dai log. Una release pubblicata **non** dimostra che `dev` e `main` siano sincronizzati, né che l'APK funzioni.
- **Stato e limiti:** PARZIALMENTE RISOLTO per l'incidente storico: asset pubblicato, backmerge non completato; nessun test runtime specifico della release.
- **Riferimenti:** workflow [`39936118763`](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/actions/runs/39936118763), job `120355950357`; PR #2 (commit `6aa2913`), commit prerelease `dc54c3f`, README `f037573`, `.releaserc` e cinque file generati citati. Voci correlate FON-003 e MOR-REL-001.
- **Numerazione:** l'ID `MOR-REL-005` è mantenuto dal resoconto originale; nel dizionario centrale consultato non figura ancora `MOR-REL-004`. La lacuna è intenzionalmente lasciata senza inventare una voce.


### FLOWX-META-001 — Nome "Diagnostica Gold" incoerente con la modifica dello stato locale — RISOLTO (metadati)

- **Ambito:** Flowx `com.enzuredigital.weatherbomb` 4.650, patch R9, `FlowxEntitlementDiagnosticPatch.kt`; release `v1.2.0` e `v1.3.0`.
- **Sintomo:** su `dev` il nome `Flowx - Diagnostica autorizzazione Gold` poteva far pensare a una lettura passiva; su `main` compariva anche la vecchia denominazione `Flowx - Sblocca Gold`.
- **Causa verificata nel sorgente:** la patch crea dati dimostrativi di entitlement `gold`/`active` e li inserisce nella cache/nei percorsi locali: helper `morpheCreateDemoRows`, metodi `Lz5;->c()`, `Lz5;->h()`, interazioni con `Lrs5;->i(Ljava/lang/Object;)V`. **Non è una pura diagnostica passiva**, ma non è provata alcuna variazione dell'abbonamento server.
- **Soluzione:** corretto **solo** il testo su `dev`: nome `Flowx - Test stato Gold locale`, descrizione `Inserisce dati dimostrativi Gold nella cache locale dell'app per verificarne il comportamento; non modifica l'abbonamento lato server.`. Bytecode e logica operativa **invariati**.
- **Verifica:** commit [`eccd1e2`](https://github.com/Edoardo9708/LegheFC-Morphe-Patches/commit/eccd1e28fa67e30281ddb3f20e6a9b8a25a89341) con i due campi modificati; `v1.3.0-dev.2` compilata e pubblicata, testo corretto dopo merge anche nella stabile `v1.3.0`.
- **Stato/limiti:** RISOLTO **solo** per la coerenza dei metadati. Nessun nuovo test runtime, controllo delle funzionalità Gold o prova di persistenza dopo riavvio. Una build riuscita non dimostra lo sblocco completo.

### Aggiornamenti correlati, verifiche aperte e regole riutilizzabili

- **Permessi PR (già TWI-002):** errore `GraphQL: GitHub Actions is not permitted to create or approve pull requests` risolto **nel repository unificato**, dopo modifica delle autorizzazioni e nuova esecuzione del job. Non cambiare lo stato TWI-002 relativo al **diverso repository Twilight**.
- **Promozione dei branch (già FON-003):** PR #9 chiusa senza merge per changelog duplicato e descrizione Flowx imprecisa; PR #10 integrata con merge senza squash, release stabile `v1.3.0`, commit `8938194` e release `34dffe3`. Il workflow, la pulizia changelog e la correzione di metadati sono documentati nelle tre voci sopra e nella nota aggiunta a FON-003.
- **Attenzione a `semantic-release`:** dopo il ripristino del workflow con commit `chore(ci)`, la nuova `v1.3.0-dev.1` includeva `feat:` storici, coerentemente con la regola `feat → minor` di `.releaserc`. È una spiegazione sostenuta dai metadati, non isolata in un esperimento separato.
- **Backmerge:** `.releaserc` include `backmergeStrategy: "merge"` e `mergeMode: "ours"`. La sincronizzazione `main`/`dev` è riuscita nel caso documentato; il comportamento con futuri conflitti non è verificato.
- **Regole:** confrontare il workflow con il template prima di aggiungere workaround permanenti; per correggere un changelog storico utilizzare tag e sezioni autentiche; considerare i **file realmente diversi**, non il solo numero dei commit; distinguere pipeline di release, PR, merge, pubblicazione bundle e test dell'applicazione; descrivere correttamente se una patch modifica **stato locale** anziché registrare soltanto.
- **Aperto/non dimostrato:** causa radice originaria delle Git note `dev.12`; comportamento del backmerge in conflitti futuri; efficacia e persistenza della patch Flowx Gold nella release stabile; nessun test nuovo di Leghe FC, Twilight o Outdooractive in questa manutenzione.

---

## Flowx 4.650 — prove R7/R8 sulle autorizzazioni Gold (24 ottobre 2026)

**Ambito:** `com.enzuredigital.weatherbomb`, `FlowxEntitlementDiagnosticPatch.kt`, Kotlin/Smali e Logcat; distinguere i log **R7** dalle modifiche introdotte nella **R8** e dai successivi esperimenti **R9**. FLOWX-META-001 documenta già la descrizione non completamente passiva della patch: le seguenti voci riguardano il comportamento dei dati.

### FLOWX-001 — Test `gold/active` positivo ma lista effettiva vuota — RISOLTO (diagnosi); Gold APERTO

- **Sintomo R7:** `TEST gold/active B() = true`, ma `h() cache WRITE ARG size = 0`, `h() cache WRITE ARG elements = []`, `h() cache x AFTER write size = 0` e `k() entitlement list size = 0`. La lista sintetica aveva invece `h() cache TEST size = 1`, con `EntitlementRow(product=gold, status=active, ...)`.
- **Causa verificata:** l'helper `morpheIsolatedCacheList` costruiva una lista **separata** con oggetti di prova, non la assegnava a `Lz5.x`. Il metodo `Ldw6.B(Lxi1;J)Z` accettava il singolo oggetto dimostrativo, ma `Lz5.k()` consultava una lista reale senza elementi. Un controllo booleano sintetico positivo **non** dimostra un entitlement Gold effettivamente presente.
- **Tentativo di interpretazione errato:** trattare `B() = true` sulla riga di prova come prova di disponibilità della funzione Gold. Non è un test del recupero dello stato originale.
- **Soluzione diagnostica:** confrontare separatamente l'argomento originale passato alla scrittura `Lrs5.i()`, la lista di prova, il valore effettivamente memorizzato in `Lz5.x` dopo la scrittura e quello letto da `Lz5.k()`. Non confondere cache e test sintetico.
- **Verifica:** log R7 con percorso `Lz5.h()` eseguito, dati dimostrativi creati, `x` ancora vuoto e `k()` che legge `size=0`. Marcatore `FX-20261024-R7`, commit `c69323a`.
- **Stato/limiti:** RISOLTO **il significato dei log**, APERTO il funzionamento Gold. Non dimostrato cosa restituisca il server, se ci siano altri caricamenti, se la lista venga riempita altrove o cancellata in seguito, né accesso alle funzioni premium/persistenza.
- **Riferimenti:** `FlowxEntitlementDiagnosticPatch.kt`, `morpheIsolatedCacheList`, `Lz5.h()`/`k()`/`x`, `Lrs5.i()`, `Ldw6.B()`; revision R7.

### FLOWX-002 — Reset alternativo di `Lz5.x` non coperto dalla prima diagnostica — PARZIALMENTE RISOLTO

- **Sintomo:** R7 controllava le scritture attraverso `Lz5.c()` e `Lz5.h()`, ma non poteva stabilire se la lista venisse **svuotata successivamente** da altri percorsi.
- **Causa verificata staticamente:** `Lz5.b()` contiene una chiamata a `Lrs5.j(current, emptyList)`, diversa dalla scrittura `Lrs5.i()`; il codice identifica il riferimento a `Lrs5.j()` e alla lista vuota `Lmh1.k`. **Non è dimostrato nei log R7** che questo reset sia stato eseguito.
- **Lacuna del precedente tentativo:** loggare solo `i()` e il risultato finale di `k()` non consente di attribuire a `b()` un eventuale svuotamento.
- **Soluzione diagnostica R8:** aggiunti `morpheLogBResetBefore` e `morpheLogBResetAfter`, che leggono `x` prima/dopo l'operazione originale `Lrs5.j()` senza rimuoverla. Marcatori previsti `FX-R8 b() RESET BEFORE x size = ...`, `FX-R8 b() RESET AFTER x size = ...`, `FX-R8 demo factory RETURN size = ...`, `PATCH CODE ID FX-20261024-R8`.
- **Verifica:** codice R8 introdotto, commit `3c3eae5`, pipeline GitHub completate; **nessun log runtime R8** disponibile in questa sessione per confermare che `b()` abbia effettivamente azzerato una lista prima non vuota.
- **Stato/limiti:** PARZIALMENTE RISOLTO: individuato e strumentato un possibile reset, senza prova dell'esecuzione o persistenza. R8 introduce anche prove di inserimento della lista dimostrativa nei percorsi locali di scrittura, quindi **non è soltanto passiva**. Non attribuire risultati R7 o R9 alla R8.
- **Riferimenti:** `FlowxEntitlementDiagnosticPatch.kt`, `Lz5.b()`, `Lrs5.j()`, `Lmh1.k`, `morpheLogBResetBefore/After`, `morpheCreateDemoRows`; R8.

### Regole riutilizzabili e questioni aperte

- Un bundle `.mpp` ZIP valido non implica presenza dei DEX; verificare il contenuto della **fase finale** di pubblicazione (MOR-REL-003), e usare versioni distinte per contenuti differenti (MOR-REL-001).
- Tracciare **oggetto di prova, argomento di scrittura, stato memorizzato, stato letto e reset** come cinque eventi distinti. Un oggetto che supera `B()` non è prova di autorizzazione dell'app o del server.
- Usare marcatori di revisione nei log per distinguere APK effettivamente eseguiti: `v1.2.0-dev.12` è stata riutilizzata per bundle diversi; identificazione del codice e versione GitHub possono divergere.
- **Aperto:** log R8/R9 per dimostrare se e quando `Lz5.x` si svuota; funzionamento effettivo Gold e persistenza dopo chiusura/riavvio; causa originaria del rifiuto Git notes; prova positiva del caricamento in Morphe Manager del bundle DEX ripristinato.

---

## Outdooractive — Membership 3.23.20 (23 ottobre 2026)

**Ambito:** `com.outdooractive.Outdooractive`, `classes4.dex`, Morphe Patcher/Kotlin/dexlib2, ADB Logcat, [repository Outdooractive](https://github.com/Edoardo9708/Outdooractive-Morphe-Patches) (`dev`), prerelease `v1.0.0-dev.1`–`dev.4`. I riferimenti ai log sono locali. Distinguere il codice del repository separato dalle versioni confluite nel [repository centrale](https://github.com/Edoardo9708/LegheFC-Morphe-Patches).

### OA-001 — Logging Membership eccessivo — PARZIALMENTE RISOLTO

- **Sintomo:** diagnostica dev.1 con **6.807 righe in 7,36 s**: 137 chiamate `isProUser()` tutte `false` e 51 `getLevel()` tutte `0`, nessuna `FATAL EXCEPTION` nel file osservato.
- **Causa verificata:** `morpheDiagPro()` e `morpheDiagLevel()` usavano `Log.i(tag,message,Throwable())` a ogni getter, generando stack trace completi anche su letture ripetute da LiveData, mappe e UI.
- **Soluzione (dev.2):** logger compatto che legge `Throwable.getStackTrace()[2]`, non stampa l'intero stack e deduplica con `ConcurrentHashMap.putIfAbsent()` per combinazione metodo/valore/chiamante. Esempio `OA-Membership: isProUser = false | caller: tdk.H(UserBarrier.kt:10)`. Nessun altro tentativo fallito documentato.
- **Verifica:** compilazione e pubblicazione `v1.0.0-dev.2` riuscite; **non** misurata sul dispositivo la riduzione dei messaggi: i conteggi si riferiscono alla **dev.1**.
- **Limiti:** costruzione dello stack comunque eseguita per ogni chiamata; inizializzazione pigra della mappa non sincronizzata, con rischio di deduplicazioni temporaneamente perse o duplicate.
- **Riferimenti:** `MembershipDiagnosticPatch.kt` / `addLogger()`; log `Testo incollato(2).txt` del 23/10/2026; commit `ef8b7d9`, `v1.0.0-dev.2`.

### OA-002 — Getter DEX con un registro: ricostruzione del metodo — PARZIALMENTE RISOLTO

- **Causa verificata:** `Membership.isProUser():Z` e `getLevel():I` contenevano solo `iget-boolean v0,p0; return v0` e `iget v0,p0; return v0`. Un registro totale per ciascun getter d'istanza: `v0` coincide con `p0`, dunque nessun locale libero. L'API `MutableMethod` disponibile non esponeva modifica diretta del `registerCount`.
- **Tentativi:** non risulta un tentativo fallito di impostare direttamente il numero di registri; limite identificato analizzando le API.
- **Soluzione (dev.3):** `instrumentGetter()` ricrea il metodo con `ImmutableMethod` e `MutableMethodImplementation(3)`, preservando firma, flag, annotazioni e restrizioni API; layout `v0` valore originale, `v1` registro aggiuntivo, `v2/p0` istanza. Fingerprint rigido su registro, due istruzioni, opcode, campo e tipo; abort se non corrisponde.
- **Verifica:** build e pre-release `v1.0.0-dev.3` riuscite; successivamente utente riferisce comportamento apparentemente funzionante col getter modificato, **senza dump DEX installato** o test esaustivo dei percorsi runtime.
- **Limiti:** validato staticamente per Outdooractive 3.23.20; compilazione e pubblicazione non dimostrano da sole la validità di ogni esecuzione ART.
- **Riferimenti:** `MembershipDiagnosticPatch.kt` / `instrumentGetter()`; `docs/ANALYSIS.md`; commit `6597887`; `v1.0.0-dev.3`.

### OA-003 — Descrizione «solo diagnostica» falsa dopo override dei getter — APERTO

- **Sintomo:** sul branch `dev` del **repository Outdooractive separato**, anche dopo `v1.0.0-dev.4`, commenti, nome e descrizione dichiarano conservazione dei valori originali, ma le istruzioni generate contengono `#return v0` e `return v1`.
- **Causa verificata:** dal commit `bab86cd` il getter `isProUser()` restituisce il valore di prova `1`, `getLevel()` il valore `2` (parametro `example` in `instrumentGetter()`). I campi originali sono letti/loggati, ma i risultati restituiti sono modificati. Nessun crash o errore di compilazione documentato.
- **Verifica:** dev.4 pubblicata; utente riferisce app funzionante e livello **ProPlus** visibile. Mancano test sistematici di funzioni/servizi remoti e persistenza dopo riavvio.
- **Correzione proposta e non applicata nel repository separato:** riportare `return v0` se la patch deve restare passiva, oppure allineare nome, descrizione, README e piano di test all'override locale.
- **Differenza verificata il 31/10/2026:** nel repository **centrale** su `dev` i metadati sono già `Outdooractive - Test getter abbonamento` e descrizione `Unlock Pro`, con commento esplicito sulla modifica dei getter; anche lì resta `return v1`. **I repository non sono identici**. La dicitura `Unlock Pro` non prova che siano accessibili servizi/entitlement sul backend.
- **Stato:** APERTO per coerenza del repository separato e verifica funzionale. Non è stata apportata alcuna correzione alla patch durante questa documentazione.
- **Riferimenti:** `MembershipDiagnosticPatch.kt`, `README.md`, `docs/ANALYSIS.md`, `docs/TEST_PLAN.md`; commit `bab86cd`, `v1.0.0-dev.4`, PR #1.

### OA-004 — Getter Membership locali e autorizzazioni backend sono livelli distinti — PARZIALMENTE RISOLTO

- **Questione:** quali componenti consultano `isProUser()`/`getLevel()` e quali diritti ne dipendono realmente?
- **Analisi verificata:** gli stack trace dev.1 individuano chiamanti da `UserBarrier.kt`, `UserMapsLiveData.kt`, `MapRequestHelper.java`, `MapFragment.java`, `FabMenu.kt`, `MainActivity.java`, `ProMembershipUpgradeView`, `FirebaseAppAnalytics.kt`, `CrashlyticsAccess.kt`, oltre a `RepositoryLiveData`, `BaseRequest` e callback asincrone. L'analisi statica mostra `/sync/user/profile` e `/user/profile/membership`, entrambi basati su oggetti `Membership` **non necessariamente della stessa istanza**.
- **Soluzione parziale:** associare le letture ai chiamanti mediante logging, distinguendole da risposte API, controlli server e cache; nessun tentativo fallito specifico documentato.
- **Verifica:** getter intercettati in Logcat e ProPlus comparso in UI dopo cambio locale; ciò non verifica accesso alle funzioni remote. Nessuna risposta API completa acquisita.
- **Limiti:** non determinati i percorsi esclusivamente locali, backend, ulteriori proprietà o controlli; manca validazione dopo riavvio, su mappe, profilo e percorsi.
- **Riferimenti:** log `Testo incollato(2).txt`, `docs/ANALYSIS.md`, `v1.0.0-dev.1`/`dev.4`.

### Regole generali e verifiche mancanti

- Controllare `registers_size`, parametri e opcode prima di usare registri Smali; nelle ricostruzioni DEX preservare firma e metadata del metodo.
- Verificare il bytecode **generato** dai parametri Kotlin, non basarsi sul nome o sui commenti; distinguere log passivo da override.
- Deduplicare il logging sui percorsi frequenti, misurando anche costo del recupero stack e accessi concorrenti.
- **Distinguere** build/bundle, installazione, runtime, modifica UI, autorizzazioni server e persistenza.
- **Aperto:** verifica prestazioni e concorrenza logger, test funzionali/riavvio, autorizzazioni remote, riallineamento metadata nel repository separato.

---

## Modello per le prossime voci

Usare un codice univoco (`APP-001`), nome dell'app e versione/build, sintomo osservabile, causa (**verificata** o **ipotizzata**), soluzione/tentativi falliti, modalità di verifica, risultato con limiti, eventuale commit/branch di riferimento. Documentare anche i workaround parziali e distinguere esplicitamente il funzionamento confermato dall'utente dalla sola esecuzione tecnica di una patch.
