package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object PbGadgetOnlyF7AssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbGadgetOnlyF7AssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/**
 * F7: isolamento del crash nativo di F6.
 *
 * Carica lo stesso Frida Gadget 17.24.3 ARM64 e la stessa configurazione in
 * script mode. Lo script, pero', usa solo il core Frida per scrivere log:
 * NON importa il Java bridge e NON installa alcun hook.
 *
 * Non selezionare F7 assieme a F5 o F6: condividono il nome delle librerie
 * native nell'APK. Non serve alcun certificato o opzione.
 */
private val pbGadgetOnlyF7Resources = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { manifest ->
            val app = manifest.documentElement
                .getElementsByTagName("application")
                .item(0) as? Element
                ?: error("F7: application non trovata")
            app.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:extractNativeLibs",
                "true"
            )
        }

        val names = listOf(
            "libpbfridagadget.so" to "libpbfridagadget.so",
            "libpbfridagadget.config.so" to "libpbfridagadget.config.so",
            "libpbgadgetonly.script.so" to "libpbfridagadget.script.so"
        )

        for ((resourceName, apkName) in names) {
            val input = PbGadgetOnlyF7AssetLoader.open("pbdiag/$resourceName")
                ?: error("F7: risorsa assente dal bundle: pbdiag/$resourceName")
            val output = get("lib/arm64-v8a/$apkName")
            require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
                "F7: directory librerie non disponibile"
            }
            input.use { source ->
                output.outputStream().use { destination -> source.copyTo(destination) }
            }
            require(output.length() > if (apkName == "libpbfridagadget.so") 1_000_000 else 30) {
                "F7: risorsa Frida incompleta: $apkName"
            }
        }
    }
}

@Suppress("unused")
val prezziBenzinaGadgetOnlyF7Patch = bytecodePatch(
    name = "Prezzi Benzina - Gadget only F7 (ARM64)",
    description = "Experimental stability test: load Frida Gadget without Java bridge, Java hooks or certificate override. Does not fix Google Maps. Do not combine with F5/F6.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )
    dependsOn(pbGadgetOnlyF7Resources)

    execute {
        val onCreate = Fingerprint(
            definingClass = "Lit/prezzibenzina/pb3/PBApplication;",
            name = "onCreate",
            returnType = "V",
            parameters = emptyList()
        ).method
        val implementation = onCreate.implementation
            ?: error("F7: PBApplication.onCreate senza bytecode")
        require(implementation.registerCount == 4) {
            "F7: versione DEX differente da Prezzi Benzina 3.26.10.10"
        }

        // Stesso punto di caricamento di F5/F6, senza ulteriori istruzioni.
        onCreate.addInstructions(
            0,
            """
                const-string v0, "pbfridagadget"
                invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """.trimIndent()
        )
    }
}
