package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode

/**
 * Leghe FC 13.2.1 (build 445): diagnostica in sola lettura di Lkqgs.A e Lkqgs.C.
 *
 * Prima del return nel costruttore Lkqgs.<init>, dopo le iput originali:
 * - legge A (List) e C (Set) dall'istanza (p0);
 * - stampa size e String.valueOf (toString) su Logcat, tag LegheFC-AC.
 *
 * Il costruttore originale ha registersSize=5, insSize=5 (p0..p4);
 * alla sua uscita p1/p2/p3 non servono piu' al metodo originale.
 * La patch non modifica i campi ne' lo stato Premium.
 *
 * I contenuti delle collezioni potrebbero contenere dati dell'account:
 * non condividere il log completo senza controllarlo.
 */
@Suppress("unused")
val accessCollectionsDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica collezioni A/C",
    description = "Log in sola lettura di A.size, A, C.size e C alla creazione dello stato. Logcat: tag:LegheFC-AC.",
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

        val beforeReturn = method.indexOfFirstInstructionOrThrow {
            opcode == Opcode.RETURN_VOID
        }

        method.addInstructions(
            beforeReturn,
            """
                iget-object p1, p0, Lkqgs;->A:Ljava/util/List;
                invoke-interface {p1}, Ljava/util/List;->size()I
                move-result p2
                invoke-static {p2}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
                move-result-object p2
                const-string p3, "A.size = "
                invoke-virtual {p3, p2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p2
                const-string p3, "LegheFC-AC"
                invoke-static {p3, p2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                move-result-object p2
                const-string p3, "A = "
                invoke-virtual {p3, p2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p2
                const-string p3, "LegheFC-AC"
                invoke-static {p3, p2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                iget-object p1, p0, Lkqgs;->C:Ljava/util/Set;
                invoke-interface {p1}, Ljava/util/Set;->size()I
                move-result p2
                invoke-static {p2}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
                move-result-object p2
                const-string p3, "C.size = "
                invoke-virtual {p3, p2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p2
                const-string p3, "LegheFC-AC"
                invoke-static {p3, p2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                move-result-object p2
                const-string p3, "C = "
                invoke-virtual {p3, p2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p2
                const-string p3, "LegheFC-AC"
                invoke-static {p3, p2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """.trimIndent()
        )
    }
}
