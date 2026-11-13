package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object PbNoopHookF9AssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbNoopHookF9AssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/**
 * F9: hook di sola intercettazione, senza sostituzione di dati.
 *
 * Mantiene lo stesso Gadget Frida 17.24.3 ARM64, lo stesso onCreate e
 * ritardo di 1500 ms di F6/F8. Installa unicamente un hook su
 * ApplicationPackageManager.getPackageInfo(String,int) che chiama il metodo
 * originale e ne restituisce il medesimo PackageInfo, senza alterarlo.
 *
 * Non selezionare insieme a F5, F6, F7 o F8 (condividono le librerie).
 * Nessun certificato/opzione richiesta. Non risolve il blocco Google Maps.
 */
private val pbNoopHookF9Resources = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { manifest ->
            val app = manifest.documentElement
                .getElementsByTagName("application")
                .item(0) as? Element
                ?: error("F9: application non trovata")
            app.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:extractNativeLibs",
                "true"
            )
        }

        val names = listOf(
            "libpbfridagadget.so" to "libpbfridagadget.so",
            "libpbfridagadget.config.so" to "libpbfridagadget.config.so",
            "libpbnoop.script.so" to "libpbfridagadget.script.so"
        )

        for ((resourceName, apkName) in names) {
            val input = PbNoopHookF9AssetLoader.open("pbdiag/$resourceName")
                ?: error("F9: risorsa assente dal bundle: pbdiag/$resourceName")
            val output = get("lib/arm64-v8a/$apkName")
            require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
                "F9: directory librerie non disponibile"
            }
            input.use { source ->
                output.outputStream().use { destination -> source.copyTo(destination) }
            }
            require(output.length() > if (apkName == "libpbfridagadget.so") 1_000_000 else 30) {
                "F9: risorsa Frida incompleta: $apkName"
            }
        }
    }
}

@Suppress("unused")
val prezziBenzinaNoopHookF9Patch = bytecodePatch(
    name = "Prezzi Benzina - No-op PackageInfo hook F9 (ARM64)",
    description = "Experimental stability test: intercept getPackageInfo(String,int) but return original PackageInfo unchanged. No certificate modification. Does not fix Maps. Use F9 alone, not with F5-F8.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )
    dependsOn(pbNoopHookF9Resources)

    execute {
        val onCreate = Fingerprint(
            definingClass = "Lit/prezzibenzina/pb3/PBApplication;",
            name = "onCreate",
            returnType = "V",
            parameters = emptyList()
        ).method
        val implementation = onCreate.implementation
            ?: error("F9: PBApplication.onCreate senza bytecode")
        require(implementation.registerCount == 4) {
            "F9: versione DEX differente da Prezzi Benzina 3.26.10.10"
        }

        // Stesso punto di caricamento di F5/F6/F7/F8, senza ulteriori istruzioni.
        onCreate.addInstructions(
            0,
            """
                const-string v0, "pbfridagadget"
                invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """.trimIndent()
        )
    }
}
