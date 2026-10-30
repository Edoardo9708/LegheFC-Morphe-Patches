package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object PbFridaAssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbFridaAssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/**
 * Esperimento F5: Frida Gadget 17.24.3, SOLO ARM64.
 * Non modifica firme, token Maps, Billing, Premium o permessi.
 *
 * Preparazione delle risorse: il bundle contiene la libreria upstream,
 * la configurazione script-mode e un singolo hook Java di diagnostica.
 * Lo script registra in Logcat con il tag PB-Signature.
 *
 * Questa patch e' volutamente separata da Remove Ads A1.\n * F3 ha confermato che il caricamento nativo funziona.\n * F5 intercetta solo getPackageInfo(String,int), registra le impronte dei certificati restituiti e mantiene il risultato originale.
 */
private val pbFridaSignatureResources = resourcePatch {
    execute {
        // Frida in script mode usa un file di configurazione e uno script
        // accanto alla libreria nativa. Android deve estrarre questi file.
        document("AndroidManifest.xml").use { manifest ->
            val application = manifest.documentElement
                .getElementsByTagName("application")
                .item(0) as? Element
                ?: error("Prezzi Benzina: <application> non trovato")
            application.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:extractNativeLibs",
                "true"
            )
        }

        val names = listOf(
            "libpbfridagadget.so",
            "libpbfridagadget.config.so",
            "libpbfridagadget.script.so"
        )

        // Tutte le librerie/script vengono impacchettati nel bundle .mpp
        // dal workflow di build. Nessun download sul telefono.
        for (name in names) {
            val path = "pbdiag/$name"
            val input = PbFridaAssetLoader.open(path)
                ?: error("Prezzi Benzina: risorsa Frida assente nel bundle: $path")
            val output = get("lib/arm64-v8a/$name")
            require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
                "Prezzi Benzina: impossibile creare directory librerie"
            }
            input.use { from ->
                output.outputStream().use { to -> from.copyTo(to) }
            }
            require(output.length() > if (name == "libpbfridagadget.so") 1_000_000 else 30) {
                "Prezzi Benzina: file Frida incompleto: $name"
            }
        }
    }
}

/**
 * Avvia Frida Gadget una sola volta dall'Application.onCreate(), prima della
 * logica applicativa. La libreria avvia automaticamente lo script diagnostico,
 * quindi sul telefono NON servono ADB, Termux, Objection o un client Frida.
 */
@Suppress("unused")
val prezziBenzinaFridaSignatureTracePatch = bytecodePatch(
    name = "Prezzi Benzina - Signature trace F5 (ARM64)",
    description = "Experimental read-only inspection of returned PackageInfo signing certificates (SHA-1/SHA-256) and call stacks. No signature changes.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )

    dependsOn(pbFridaSignatureResources)

    execute {
        val app = "Lit/prezzibenzina/pb3/PBApplication;"
        val start = Fingerprint(
            definingClass = app,
            name = "onCreate",
            returnType = "V",
            parameters = emptyList()
        ).method
        val body = start.implementation
            ?: error("Prezzi Benzina: PBApplication.onCreate() senza codice")
        require(body.registerCount == 4) {
            "Prezzi Benzina: onCreate() non corrisponde al DEX 3.26.10.10"
        }

        // v0 e' un registro locale in PBApplication.onCreate (3 locali,
        // p0 == v3). Non sovrascriviamo il parametro 'this'.
        // La libreria si carica una sola volta per processo quando parte
        // l'Application. Gadget in script mode NON attende una connessione.
        start.addInstructions(
            0,
            """
                const-string v0, "pbfridagadget"
                invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """.trimIndent()
        )
    }
}
