package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.util.Base64

private object PbCustomSignerAssetLoader {
    fun open(path: String): java.io.InputStream? =
        PbCustomSignerAssetLoader::class.java.classLoader.getResourceAsStream(path)
}

/**
 * F6 - esperimento isolato su Android 17, Prezzi Benzina 3.26.10.10.
 *
 * Certificato X.509 PUBBLICO fornito dall'utente, DER codificato Base64.
 * Non sono richieste ne' accettate chiavi private e NON basta una SHA-1.
 *
 * Sul dispositivo lo script Frida intercetta un solo metodo Java:
 * PackageManager.getPackageInfo(String,int). Sostituisce localmente, solo
 * per il nostro package, i dati certificato del PackageInfo restituito
 * se richiesti dal chiamante. Non altera la firma REALE dell'APK e non
 * modifica le risposte di Android ad altri processi.
 *
 * Non abilitarla insieme alla F5 diagnostica (stesso Frida Gadget).
 * M1 Maps key e Remove Ads A1 sono patch indipendenti.
 */
// A shared option must belong to the VISIBLE bytecode patch, not only its
// unnamed resource dependency: otherwise Morphe cannot show the input field.
// Morphe Manager infers a FOLDER picker from option keys containing
// "custom", "icon" or "header". Keep the stored key free of those words;
// the visible title can still contain "Custom".
private val pbCustomCertificateOption = stringOption(
    key = "signingCertificateDerBase64",
    title = "Custom signing certificate DER (Base64)",
    description = "Paste the Base64 contents of the public X.509 DER certificate; do not select a folder.",
    required = true
)

private val pbCustomSignerResources = resourcePatch {
    execute {
        // Le opzioni Morphe sono impostate sul telefono prima del patching.
        // Non includiamo alcun certificato personale nel repository.
        val certificateBase64 = pbCustomCertificateOption.value?.filterNot(Char::isWhitespace).orEmpty()
        require(certificateBase64.length in 128..16000) {
            "F6: inserire il certificato pubblico X.509 DER/Base64, non lo SHA-1"
        }

        val bytes = try {
            Base64.getDecoder().decode(certificateBase64)
        } catch (_: IllegalArgumentException) {
            error("F6: certificato Base64 non valido")
        }
        require(bytes.size in 128..12000) {
            "F6: lunghezza del certificato DER non valida"
        }

        val cert = try {
            CertificateFactory.getInstance("X.509").generateCertificate(
                ByteArrayInputStream(bytes)
            )
        } catch (_: Exception) {
            error("F6: dati forniti non rappresentano un certificato X.509 DER valido")
        }
        require(cert.encoded.contentEquals(bytes)) {
            "F6: il certificato non e' un singolo DER X.509 canonico"
        }

        val androidNs = "http://schemas.android.com/apk/res/android"
        val propertyName = "lab.prezzibenzina.pbdiag.CUSTOM_CERT_DER"
        document("AndroidManifest.xml").use { manifest ->
            val applications = manifest.documentElement.getElementsByTagName("application")
            require(applications.length == 1) {
                "F6: voce application non univoca"
            }
            val app = applications.item(0) as Element
            require((0 until app.getElementsByTagName("meta-data").length).none { index ->
                (app.getElementsByTagName("meta-data").item(index) as Element)
                    .getAttributeNS(androidNs, "name") == propertyName
            }) {
                "F6: certificato personalizzato gia' presente nel manifest"
            }

            // Metadata accessibile SOLO all'app in cui iniettiamo Frida.
            // Non toccare com.google.android.geo.API_KEY (gestito da M1).
            val metadata = manifest.createElement("meta-data")
            metadata.setAttributeNS(androidNs, "android:name", propertyName)
            metadata.setAttributeNS(androidNs, "android:value", certificateBase64)
            app.appendChild(metadata)
            app.setAttributeNS(androidNs, "android:extractNativeLibs", "true")
        }

        // Libreria nativa gia' preparata dal build server: identica alla F5,
        // ma questo esperimento installa uno script Java DIFFERENTE.
        val names = listOf(
            "libpbfridagadget.so" to "libpbfridagadget.so",
            "libpbfridagadget.config.so" to "libpbfridagadget.config.so",
            "libpbcustom.script.so" to "libpbfridagadget.script.so"
        )
        for ((resource, apkName) in names) {
            val input = PbCustomSignerAssetLoader.open("pbdiag/$resource")
                ?: error("F6: risorsa assente nel bundle: pbdiag/$resource")
            val output = get("lib/arm64-v8a/$apkName")
            require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
                "F6: directory librerie non disponibile"
            }
            input.use { from ->
                output.outputStream().use { to -> from.copyTo(to) }
            }
            require(output.length() > if (apkName == "libpbfridagadget.so") 1_000_000 else 30) {
                "F6: risorsa Gadget incompleta"
            }
        }
    }
}

@Suppress("unused")
val prezziBenzinaCustomSignatureF6Patch = bytecodePatch(
    name = "Prezzi Benzina - Custom signer response F6 (ARM64)",
    description = "Experimental: use a user-supplied public X.509 DER/Base64 certificate in the locally returned PackageInfo. Does not re-sign APK or alter server verification. Do not combine with F5.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )
    // This option is exposed on the user-selectable F6 patch; the dependent
    // resource patch reads the same Option object during execution.
    pbCustomCertificateOption()
    dependsOn(pbCustomSignerResources)

    execute {
        val method = Fingerprint(
            definingClass = "Lit/prezzibenzina/pb3/PBApplication;",
            name = "onCreate",
            returnType = "V",
            parameters = emptyList()
        ).method
        val impl = method.implementation
            ?: error("F6: PBApplication.onCreate senza bytecode")
        require(impl.registerCount == 4) {
            "F6: versione DEX dell'app differente da quella attesa"
        }

        // Una sola copia di Frida Gadget per processo.
        // F5 e F6 non vanno selezionate insieme.
        method.addInstructions(
            0,
            """
                const-string v0, "pbfridagadget"
                invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """.trimIndent()
        )
    }
}
