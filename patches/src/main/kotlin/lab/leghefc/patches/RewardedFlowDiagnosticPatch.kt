package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode

/**
 * Leghe FC 13.2.1 (build 445): esercizio indipendente di iput/iget boolean.
 *
 * Nel costruttore Lkqgs.<init> verificato nel DEX originale:
 *   registersSize=5, insSize=5, p0..p4, una sola return-void.
 * Tutti i campi sono gia' inizializzati prima del return. A quel punto
 * p1/p2 non sono piu' utilizzati e possono servire da registri di test.
 *
 * L'esperimento crea un oggetto android.util.MutableBoolean separato,
 * ne scrive value=true con iput-boolean, lo rilegge con iget-boolean
 * e stampa il risultato su Logcat con tag LegheFC-BOOL.
 * NON scrive Lkqgs.B, non cambia timer, pubblicita' o abbonamenti.
 */
@Suppress("unused")
val booleanFieldDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Test iput/iget boolean",
    description = "Scrive e rilegge un booleano di prova in un oggetto separato. Logcat: LegheFC-BOOL.",
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
            definingClass = "Lkqgs;",
            name = "<init>"
        ).method

        // Inserimento prima del return: dopo tutte le iput originali.
        // p1 e p2 non sono piu' vivi in questo preciso punto del metodo.
        val beforeReturn = method.indexOfFirstInstructionOrThrow {
            opcode == Opcode.RETURN_VOID
        }

        method.addInstructions(
            beforeReturn,
            """
                new-instance p1, Landroid/util/MutableBoolean;
                const/4 p2, 0x0
                invoke-direct {p1, p2}, Landroid/util/MutableBoolean;-><init>(Z)V

                const/4 p2, 0x1
                iput-boolean p2, p1, Landroid/util/MutableBoolean;->value:Z
                iget-boolean p2, p1, Landroid/util/MutableBoolean;->value:Z

                iput-boolean p2, p0, Lkqgs;->B:Z

                invoke-static {p2}, Ljava/lang/String;->valueOf(Z)Ljava/lang/String;
                move-result-object p2
                const-string p1, "LegheFC-BOOL"
                invoke-static {p1, p2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """.trimIndent()
        )
    }
}
