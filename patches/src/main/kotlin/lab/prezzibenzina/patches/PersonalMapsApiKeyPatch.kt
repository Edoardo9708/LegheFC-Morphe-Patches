package lab.prezzibenzina.patches

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

/**
 * M1 - Google Maps API key personale, senza modificare firme o controlli di
 * autenticita'. SOLO per Prezzi Benzina 3.26.10.10.
 *
 * Verifica effettuata sul manifest dell'APK originale:
 * <meta-data android:name="com.google.android.geo.API_KEY"
 *            android:value="@0x7f1501a5" />
 * Il valore e' un riferimento a una risorsa: lo sostituiamo con una stringa.
 *
 * NON pubblicare mai una chiave API personale in un repository.
 * Inserirla nell'opzione della patch in Morphe al momento dell'applicazione.
 *
 * Alternativa solo per sperimentazione LOCALE, da NON committare:
 *   // val apiKey = "AIza...CHIAVE_PERSONALE..."
 *
 * Questa patch e' indipendente da Remove Ads A1 e NON richiede Frida.
 */
@Suppress("unused")
val prezziBenzinaPersonalMapsApiKeyPatch = resourcePatch(
    name = "Prezzi Benzina - Personal Google Maps key M1",
    description = "Replace the Maps SDK manifest API key with your own Google Cloud key. Enter it in patch options; no certificate spoofing.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )

    // Inserire la chiave tramite l'interfaccia delle opzioni di Morphe.
    // Evita di lasciare credenziali private nel sorgente GitHub.
    val mapsApiKey by stringOption("Your Google Maps API key", required = true)

    execute {
        val apiKey = mapsApiKey?.trim().orEmpty()

        // Una API key Google Maps standard inizia per AIza e ha 39 caratteri.
        // Non loggare mai la chiave o includerla nei messaggi d'errore.
        require(Regex("^AIza[0-9A-Za-z_-]{35}$").matches(apiKey)) {
            "M1: inserire nelle opzioni una Google Maps API key valida (AIza..., 39 caratteri)"
        }

        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val metadataName = "com.google.android.geo.API_KEY"

        document("AndroidManifest.xml").use { manifest ->
            val applications = manifest.documentElement
                .getElementsByTagName("application")
            require(applications.length == 1) {
                "M1: AndroidManifest.xml non contiene una singola <application>"
            }

            val application = applications.item(0) as Element
            val nodes = application.getElementsByTagName("meta-data")
            val matches = (0 until nodes.length)
                .map { nodes.item(it) as Element }
                .filter {
                    it.getAttributeNS(androidNamespace, "name") == metadataName
                }

            // Protegge da manifest di altre versioni e duplicati incompatibili.
            require(matches.size == 1) {
                "M1: voce com.google.android.geo.API_KEY non trovata o duplicata"
            }

            val target = matches.single()
            require(target.hasAttributeNS(androidNamespace, "value")) {
                "M1: la voce Maps non possiede android:value"
            }

            // Il vecchio android:value e' un riferimento alla risorsa
            // @0x7f1501a5 (nel manifest binario originale).
            // Sovrascrive soltanto il valore del metadata Maps.
            target.setAttributeNS(androidNamespace, "android:value", apiKey)
        }
    }
}
