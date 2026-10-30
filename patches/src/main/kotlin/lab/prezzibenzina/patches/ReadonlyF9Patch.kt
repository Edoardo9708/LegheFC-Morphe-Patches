package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object PbReadonlyF9AssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbReadonlyF9AssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/** F9: installa getPackageInfo(String,int) con restituzione invariata.
 * Nessuna modifica a firme, PackageInfo o risposta Google Maps.
 * Non selezionare insieme a F5, F6, F7 o F8: stessa libreria Frida.
 */
private val pbReadonlyF9Resources = resourcePatch {
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
            "libpbreadonly.script.so" to "libpbfridagadget.script.so"
        )

        for ((resourceName, apkName) in names) {
            val input = PbReadonlyF9AssetLoader.open("pbdiag/$resourceName")
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
val prezziBenzinaReadonlyF9Patch = bytecodePatch(
    name = "Prezzi Benzina - Read-only hook F9 (ARM64)",
    description = "Experimental: hook getPackageInfo(String,int) and return its original result unchanged. No signing override. Do not combine with F5/F6/F7/F8.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )
    dependsOn(pbReadonlyF9Resources)

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
