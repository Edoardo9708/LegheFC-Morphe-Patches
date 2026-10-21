package lab.leghefc.patches

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Esperimento limitato alla modifica dell'etichetta dell'app.
 * Non altera acquisti, privilegi Premium, login o comunicazioni di rete.
 */
@Suppress("unused")
val legheFcLabLabelPatch = resourcePatch(
    name = "Leghe FC - Etichetta LAB",
    description = "Rinomina l'app in Leghe FC LAB (solo test visivo).",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Leghe FC",
            packageName = "it.quadronica.leghe",
            targets = listOf(AppTarget(version = "13.2.1"))
        )
    )

    execute {
        document("AndroidManifest.xml").use { manifest ->
            val application = manifest.getElementsByTagName("application").item(0) as? Element
                ?: error("Elemento application assente dal manifest")
            application.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:label",
                "Leghe FC LAB"
            )
        }
    }
}
