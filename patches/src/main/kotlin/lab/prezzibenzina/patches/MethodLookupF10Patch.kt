package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object PbMethodLookupF10AssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbMethodLookupF10AssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/** F10: Java.use() e selezione dell'overload getPackageInfo(String,int), senza hook.
 * Nessuna modifica a metodi, firme, PackageInfo o risposta Google Maps.
 * Non selezionare insieme a F5/F6/F7/F8/F9: stessa libreria Frida.
 */
private val pbMethodLookupF10Resources = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { manifest ->
            val app = manifest.documentElement
                .getElementsByTagName("application")
                .item(0) as? Element
                ?: error("F10: application non trovata")
            app.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:extractNativeLibs",
                "true"
            )
        }

        val names = listOf(
            "libpbfridagadget.so" to "libpbfridagadget.so",
            "libpbfridagadget.config.so" to "libpbfridagadget.config.so",
            "libpblookup.script.so" to "libpbfridagadget.script.so"
        )

        for ((resourceName, apkName) in names) {
            val input = PbMethodLookupF10AssetLoader.open("pbdiag/$resourceName")
                ?: error("F10: risorsa assente dal bundle: pbdiag/$resourceName")
            val output = get("lib/arm64-v8a/$apkName")
            require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
                "F10: directory librerie non disponibile"
            }
            input.use { source ->
                output.outputStream().use { destination -> source.copyTo(destination) }
            }
            require(output.length() > if (apkName == "libpbfridagadget.so") 1_000_000 else 30) {
                "F10: risorsa Frida incompleta: $apkName"
            }
        }
    }
}

@Suppress("unused")
val prezziBenzinaMethodLookupF10Patch = bytecodePatch(
    name = "Prezzi Benzina - Method lookup only F10 (ARM64)",
    description = "Experimental: resolve getPackageInfo(String,int) with Java.use, without installing any hook or modifying signatures. Do not combine with F5/F6/F7/F8/F9.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )
    dependsOn(pbMethodLookupF10Resources)

    execute {
        val onCreate = Fingerprint(
            definingClass = "Lit/prezzibenzina/pb3/PBApplication;",
            name = "onCreate",
            returnType = "V",
            parameters = emptyList()
        ).method
        val implementation = onCreate.implementation
            ?: error("F10: PBApplication.onCreate senza bytecode")
        require(implementation.registerCount == 4) {
            "F10: versione DEX differente da Prezzi Benzina 3.26.10.10"
        }

        // Same Frida loading point as F6/F7/F8.
        onCreate.addInstructions(
            0,
            """
                const-string v0, "pbfridagadget"
                invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """.trimIndent()
        )
    }
}
