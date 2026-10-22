package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode

/**
 * Leghe FC 13.2.1 (build 445): diagnostica di sola lettura del campo Lkqgs.B.
 *
 * Intercetta Lkqgs.<init> prima di return-void, dopo l'inizializzazione
 * originale dei campi, e legge B direttamente dall'istanza (p0).
 * Il valore originale viene stampato come "B = true/false" in Logcat.
 *
 * In questo punto p1 e p2 non sono piu' usati dalle istruzioni originali.
 * Non viene mai scritto Lkqgs.B e non vengono alterati timer, annunci o accessi.
 */
@Suppress("unused")
val booleanFieldDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica stato B",
    description = "Legge il valore originale di Lkqgs.B quando viene creato uno stato. Filtro Logcat: tag:LegheFC-B.",
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

        // Dopo tutte le assegnazioni del costruttore; prima del suo return.
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
            
                iget-boolean p2, p0, Lkqgs;->B:Z
                invoke-static {p2}, Ljava/lang/String;->valueOf(Z)Ljava/lang/String;
                move-result-object p2
                const-string p1, "B = "
                invoke-virtual {p1, p2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p2
                const-string p1, "LegheFC-B"
                invoke-static {p1, p2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

            """.trimIndent()
        )
    }
}
