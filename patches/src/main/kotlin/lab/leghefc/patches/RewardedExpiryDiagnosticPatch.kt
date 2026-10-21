package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

/**
 * Diagnostica in-app, per eseguire i test senza PC, ADB o Logcat.
 *
 * Esegue un Toast contenente l'orario di scadenza realmente calcolato
 * subito dopo la conferma di scrittura dello sblocco temporaneo.
 * Non altera le condizioni di accesso, né la durata della ricompensa.
 */
@Suppress("unused")
val rewardedExpiryDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica sblocco",
    description = "Dopo uno sblocco temporaneo, mostra l'orario di scadenza del percorso 5/15 minuti direttamente sul telefono.",
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
        val activity = "Lit/quadronica/leghe/ui/feature/gopremium/activity/GoPremiumActivity;"

        // I registri indicati sono stati verificati sull'APK 13.2.1 build 445.
        // I metodi X e Y conservano la scadenza in coppie di registri diverse.
        listOf(
            Triple("X", "v3", "5 min"),
            Triple("Y", "v2", "15 min")
        ).forEach { (methodName, expiryRegister, title) ->
            val method = Fingerprint(
                definingClass = activity,
                name = methodName
            ).method

            val insertAt = method.indexOfFirstInstructionOrThrow {
                opcode == Opcode.NEW_INSTANCE &&
                    getReference<TypeReference>()?.type == "Landroid/content/Intent;"
            }

            // Inserito nel percorso che porta a setResult/finish, dopo il salvataggio
            // della scadenza. I registri v0 e v1 vengono reinizializzati dal
            // codice originale subito dopo questa posizione.
            method.addInstructions(
                insertAt,
                """
                    new-instance v0, Ljava/util/Date;
                    invoke-direct {v0, $expiryRegister, v${expiryRegister.removePrefix("v").toInt() + 1}}, Ljava/util/Date;-><init>(J)V
                    invoke-static {v0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                    move-result-object v0
                    const-string v1, "LAB $title - fino alle "
                    invoke-virtual {v1, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v0
                    const/4 v1, 0x1
                    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
                    move-result-object v0
                    invoke-virtual {v0}, Landroid/widget/Toast;->show()V
                """.trimIndent()
            )
        }
    }
}
