
package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Leghe FC 13.2.1 (build 445)
 *
 * Diagnostica del ramo Premium del pulsante
 * "Formazione consigliata".
 *
 * Intercetta il ramo che normalmente apre
 * GoPremiumActivity.
 *
 * Stampa un log e termina il callback senza
 * eseguire la navigazione Premium.
 *
 * Non modifica le collezioni A/C o lo stato B.
 */
@Suppress("unused")
val suggestedLineupBuyPathLogPatch = bytecodePatch(
    name = "Leghe FC - Test pulsante consigliata",
    description = "Diagnostica del ramo Premium con ritorno anticipato. Logcat: LegheFC-SUG.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Leghe FC",
            packageName = "it.quadronica.leghe",
            targets = listOf(
                AppTarget(version = "13.2.1")
            )
        )
    )

    execute {
        // Individua il metodo originale.
        val method = Fingerprint(
            definingClass = "LFAky;",
            name = "invoke"
        ).method

        // Cerca i riferimenti alla schermata Premium.
        val premiumEntries = method.implementation
            ?.instructions
            ?.withIndex()
            ?.filter { (_, instruction) ->

                val field =
                    (instruction as? ReferenceInstruction)
                        ?.reference as? FieldReference

                instruction.opcode == Opcode.SGET_OBJECT &&
                    field?.definingClass ==
                    "Lit/quadronica/leghe/ui/feature/gopremium/activity/GoPremiumActivity;"
            }
            ?.map { it.index }
            ?: error("Istruzioni del metodo non disponibili")

        // Nella versione originale ci sono tre punti.
        require(premiumEntries.size == 3) {
            "Attesi 3 riferimenti Premium, trovati ${premiumEntries.size}"
        }

        // Il terzo riguarda Formazione consigliata.
        val beforeSuggestedBuy = premiumEntries.last()

        // Inserisce il codice immediatamente prima
        // del ramo che apre GoPremiumActivity.
        method.addInstructions(
            beforeSuggestedBuy,
            """
                const-string v6, "LegheFC-SUG"
                const-string v7, "Formazione consigliata: ramo Premium intercettato"
                invoke-static {v6, v7}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                # INSERISCI QUI
                invoke-virtual {v0, v1, v2}, Landroidx/fragment/app/DialogFragment;->show(Landroidx/fragment/app/FragmentManager;Ljava/lang/String;)V

                sget-object v6, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v6

            """.trimIndent()
        )
    }
}
