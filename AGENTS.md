# Istruzioni per gli agenti — Morphe

Queste istruzioni si applicano ai lavori nel repository e integrano eventuali direttive operative locali, senza sostituirle.

## Consultazione obbligatoria dei problemi noti

1. Quando emerge un errore, un crash, un blocco di compilazione/installazione, un comportamento anomalo o un problema di ripristino dati, **consultare prima `PROBLEMI-NOTI.md`** e cercare corrispondenze per sintomo, applicazione e versione.
2. Se esiste una voce pertinente, verificare che la **causa** sia davvero la stessa: non riapplicare automaticamente una soluzione valida su una versione differente.
3. Controllare sempre gli **esiti e i limiti delle verifiche**: un test diagnostico riuscito non certifica tutte le funzionalità, e un backup parziale non è completo.
4. Se la soluzione nota è applicabile, preferirla a nuovi tentativi esplorativi; quando non è applicabile, documentare la differenza e procedere con diagnosi mirata.
5. Dopo una nuova soluzione o un workaround significativo, aggiornare `PROBLEMI-NOTI.md` includendo sintomi, causa verificata o ipotizzata, tentativi falliti rilevanti, rimedio, verifica, limiti, versione e riferimenti a branch/commit.

## Operazioni su dispositivi e dati

- Prima di disinstallazioni, sostituzioni di salvataggi o modifiche rischiose, preservare gli originali e verificare che il backup copra i dati necessari.
- Prima di comandi ADB, verificare il dispositivo destinatario (seriale/modello) e i permessi dell'ambiente.
- Se si modifica un salvataggio, lavorare su una copia, mantenere il gioco fermo quando richiesto e verificare il risultato nell'app.
- Conservare controlli di integrità riproducibili; non pretendere SHA-256 identici quando il formato codificato è cambiato intenzionalmente.

## Coesistenza con il contesto operativo

- Se il file locale `NOTE-OPERATIVE-PC.md` è presente nell'ambiente di lavoro, consultarlo per workspace, strumenti, Git, ADB e preferenze operative; non presumere che sia disponibile nel repository.
- Non modificare codice, release o il branch `main` quando il compito riguarda soltanto la documentazione, salvo richiesta esplicita.
