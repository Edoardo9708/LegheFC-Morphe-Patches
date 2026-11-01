# Minigore 2 v1.28 — moltiplicatore configurabile (sperimentale)

Package original: `net.mountainsheep.minigore2zombies`. Package della build parallela già testata sul telefono: `net.mountainsheep.minigore2zombiex`.

Patch: `patches/src/main/kotlin/lab/minigore/patches/MinigoreCoinMultiplierPatch.kt`.

## Obiettivo

La patch `rawResourcePatch` aggiunge l'opzione numerica `coinMultiplier` (predefinito 10, range 1..255). Sostituisce la costante usata dalla funzione nativa `GameUtility::addScores` nelle librerie ARMv7 e x86. Non altera prezzi del negozio, acquisti, salvataggi o il manifest. Modificare l'opzione richiede un nuovo APK; non e' un'impostazione durante il gameplay.

## Garanzie statiche

Siti v1.28: ARMv7 `0xE46EE` (2 byte), x86 `0x112AD0` (6 byte).
Verificare le dimensioni esatte dei file, 12 byte prima/dopo ogni sito e le istruzioni originali o già patchate; fermarsi su qualsiasi incongruenza. Validare entrambi i file prima di scrivere. ARM Thumb `movs r2, #N` limita il fattore a 255.

La logica equivalente è stata testata offline con Python per N = 1, 10, 25, 100, 255. APK x25 firmato e verificato con la stessa chiave della build Morphe precedente; gli unici contenuti APK modificati erano le due librerie native.

## Stato

**Sperimentale (default=false).** La compilazione e l'applicazione via Morphe Manager non sono ancora certificate, così come il gameplay con moltiplicatori alternativi.

**Nota sulla firma:** la patch non imposta la chiave del firmatario. Per installare sopra la versione parallela già in uso occorre la stessa chiave PKCS#12 salvata separatamente. Importarla come chiave principale in Morphe Manager potrebbe cambiare la firma delle *altre* app patchate; per questo, prima di procedere, esportare la chiave attuale del Manager o utilizzare lo script locale con la specifica chiave Minigore.

**Nota grafica:** gli APK originari non contengono le correzioni di target/resize dell'edizione parallela. La patch nativa da sola non le produce.

Non distribuire APK o keystore privati nel repository.
