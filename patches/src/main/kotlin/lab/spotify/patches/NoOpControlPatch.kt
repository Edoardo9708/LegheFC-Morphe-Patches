package lab.spotify.patches

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

/**
 * TEST DI CONTROLLO, Spotify 9.1.90.2270.
 *
 * Confronto diagnostico con le patch Spotify sperimentali:
 * - nessuna istruzione Smali aggiunta o rimossa;
 * - nessuna classe o risorsa modificata;
 * - solo il normale processo Morphe di ricomposizione e firma dell'APK.
 *
 * Se il player continua a saltare canzoni con questa SOLA patch,
 * il problema non deriva dalle iniezioni della nostra diagnostica.
 * Se smette, si puo' isolare una delle modifiche al bytecode.
 *
 * Non contiene log in app: aggiungere log renderebbe il controllo
 * diverso da un vero no-op. Usare il resoconto Morphe per identificare
 * versione del bundle, patch selezionata e numero di classi modificate.
 */
@Suppress("unused")
val spotifyNoOpControlPatch = bytecodePatch(
    name = "Spotify - No-op control",
    description = "Repackages the original APK without changing app code or resources. Use alone to isolate playback regressions.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Spotify",
            packageName = "com.spotify.music",
            targets = listOf(AppTarget(version = "9.1.90.2270"))
        )
    )

    execute {
        // Intentionally empty. Do not insert loggers or edit DEX/resources.
    }
}
