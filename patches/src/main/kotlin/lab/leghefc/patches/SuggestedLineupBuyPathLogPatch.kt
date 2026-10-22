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
 * Leghe FC 13.2.1 (445): diagnostica del ramo Premium del pulsante
 * "Formazione consigliata" in LFAky.invoke(Object).
 *
 * In questo metodo ci sono tre letture statiche di
 * GoPremiumActivity.m. La terza corrisponde al ramo di acquisto
 * immediatamente successivo ai controlli isEmpty() di Lkqgs.A e C
 * e alternativo a LineupInsertionSuggestDialogFragment.
 *
 * DEX originale: registersSize=43, insSize=2; nel ramo selezionato,
 * v6 (esito A.isEmpty) e v7 (temporaneo iniziale) non sono piu' vivi,
 * mentre v0, v1, v4 e v5 servono per la successiva navigazione.
 * Si scrive soltanto su v6/v7. Nessun cambio a condizioni, dati,
 * acquisti, salti o schermate.
 */
@Suppress("unused")
val suggestedLineupBuyPathLogPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica pulsante consigliata",
    description = "Scrive un log quando il pulsante Formazione consigliata entra nel ramo Premium. Logcat: tag:LegheFC-SUG.",
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
        val method = Fingerprint(
            definingClass = "LFAky;",
            name = "invoke"
        ).method

        val premiumEntries = method.implementation?.instructions
            ?.withIndex()
            ?.filter { (_, instruction) ->
                val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                instruction.opcode == Opcode.SGET_OBJECT &&
                    field?.definingClass ==
                    "Lit/quadronica/leghe/ui/feature/gopremium/activity/GoPremiumActivity;"
            }
            ?.map { it.index }
            ?: error("Istruzioni di LFAky.invoke non disponibili")

        // Non scegliere mai un punto in modo approssimativo:
        // nel DEX originale i percorsi Premium sono esattamente tre.
        require(premiumEntries.size == 3) {
            "Attese 3 aperture Premium in LFAky.invoke, trovate ${premiumEntries.size}"
        }

        val beforeSuggestedBuy = premiumEntries.last()
        method.addInstructions(
            beforeSuggestedBuy,
            """
                const-string v6, "LegheFC-SUG"
                const-string v7, "Formazione consigliata: ramo acquisto Premium raggiunto"
                invoke-static {v6, v7}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """.trimIndent()
        )
    }
}
