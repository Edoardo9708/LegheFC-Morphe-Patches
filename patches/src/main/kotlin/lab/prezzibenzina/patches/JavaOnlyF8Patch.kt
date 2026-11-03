package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object PbJavaOnlyF8AssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbJavaOnlyF8AssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/**
 * F8: isolamento del crash nativo di F6.
 *
 * Carica Frida Gadget 17.24.3 ARM64 come F7. Lo script importa il bridge
 * Java ed esegue Java.perform() dopo 1500 ms, come F6, senza Java.use(),
 * intercettazioni o alterazioni delle informazioni sulla firma.
 *
 * Non selezionare F8 assieme a F5, F6 o F7: condividono il nome delle librerie
 * native nell'APK. Non serve alcun certificato o opzione.
 */
private val pbJavaOnlyF8Resources = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { manifest ->
            val app = manifest.documentElement
                .getElementsByTagName("application")
                .item(0) as? Element
                ?: error("F8: application non trovata")
            app.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:extractNativeLibs",
                "true"
            )
        }

        val names = listOf(
            "libpbfridagadget.so" to "libpbfridagadget.so",
            "libpbfridagadget.config.so" to "libpbfridagadget.config.so",
            "libpbjavaonly.script.so" to "libpbfridagadget.script.so"
        )

        for ((resourceName, apkName) in names) {
            val input = PbJavaOnlyF8AssetLoader.open("pbdiag/$resourceName")
                ?: error("F8: risorsa assente dal bundle: pbdiag/$resourceName")
            val output = get("lib/arm64-v8a/$apkName")
            require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
                "F8: directory librerie non disponibile"
            }
            input.use { source ->
                output.outputStream().use { destination -> source.copyTo(destination) }
            }
            require(output.length() > if (apkName == "libpbfridagadget.so") 1_000_000 else 30) {
                "F8: risorsa Frida incompleta: $apkName"
            }
        }
    }
}

@Suppress("unused")
val prezziBenzinaJavaOnlyF8Patch = bytecodePatch(
    name = "Prezzi Benzina - Java bridge only F8 (ARM64)",
    description = "Experimental stability test: load Frida Gadget and run Java.perform only, without Java hooks or certificate override. Does not fix Maps. Do not combine with F5/F6/F7.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )
    dependsOn(pbJavaOnlyF8Resources)

    execute {
        val onCreate = Fingerprint(
            definingClass = "Lit/prezzibenzina/pb3/PBApplication;",
            name = "onCreate",
            returnType = "V",
            parameters = emptyList()
        ).method
        val implementation = onCreate.implementation
            ?: error("F8: PBApplication.onCreate senza bytecode")
        require(implementation.registerCount == 4) {
            "F8: versione DEX differente da Prezzi Benzina 3.26.10.10"
        }

        // Stesso punto di caricamento di F5/F6/F7, senza ulteriori istruzioni.
        onCreate.addInstructions(
            0,
            """
                const-string v0, "pbfridagadget"
                invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """.trimIndent()
        )
    }
}
