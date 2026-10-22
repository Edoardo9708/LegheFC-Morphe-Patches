package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

/**
 * Leghe FC 13.2.1 (build 445).
 *
 * Log in sola lettura delle collezioni A (List) e C (Set) in
 * DUE punti di LXJX.invokeSuspend: subito dopo il caricamento
 * di entrambi i riferimenti e appena prima di Lkqgs.<init>.
 *
 * DEX originale verificato:
 * - LXJX.invokeSuspend: registers=8, ins=2
 * - invoke-direct {v3, v1, v2, v0, v7}, Lkqgs;-><init>
 * - v1 = A (List); v0 = C (Set)
 * - v4 e v5 non sono piu' usati dal codice originale in questo punto.
 *
 * Nessuna modifica a collezioni, stato o verifiche Premium.
 * ATTENZIONE: i log possono contenere dati account/acquisti: oscurarli
 * prima di condividerli.
 */
@Suppress("unused")
val stateInputDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica ingresso LXJX",
    description = "Log A/C e classe concreta di A dopo il caricamento, prima di Lkqgs.<init>. Logcat: tag:LegheFC-LXJX.",
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
            definingClass = "LXJX;",
            name = "invokeSuspend"
        ).method

        val implementation = method.implementation
            ?: error("LXJX.invokeSuspend non ha istruzioni")

        // Evita di utilizzare registri sbagliati su versioni differenti del DEX.
        require(implementation.registerCount == 8) {
            "Layout registri LXJX diverso da quello atteso"
        }

        val constructorCalls = implementation.instructions.withIndex().filter { (_, instruction) ->
            val ref = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            instruction.opcode == Opcode.INVOKE_DIRECT &&
                ref?.definingClass == "Lkqgs;" &&
                ref.name == "<init>"
        }

        require(constructorCalls.size == 1) {
            "Attesa una sola chiamata a Lkqgs.<init>, trovate ${constructorCalls.size}"
        }

        val (beforeConstructor, instruction) = constructorCalls.single()
        val call = instruction as? FiveRegisterInstruction
            ?: error("Firma invoke-direct Lkqgs non supportata")

        require(
            call.registerC == 3 && // this: nuovo Lkqgs
            call.registerD == 1 && // A: List
            call.registerE == 2 && // B: boolean
            call.registerF == 0 && // C: Set
            call.registerG == 7    // D: String
        ) {
            "Registri della chiamata al costruttore diversi da quelli attesi"
        }


        // Trova il momento in cui v1 diventa una List, dopo che v0
        // e' gia' stato caricato e verificato come Set.
        // Questo e' il primo punto in LXJX in cui A e C sono
        // entrambi pronti. Non e' la creazione degli elementi
        // delle collezioni, che avviene a monte.
        val listCast = implementation.instructions.withIndex().singleOrNull { (_, ins) ->
            val type = (ins as? ReferenceInstruction)?.reference as? TypeReference
            ins.opcode == Opcode.CHECK_CAST &&
                type?.type == "Ljava/util/List;" &&
                (ins as? OneRegisterInstruction)?.registerA == 1
        } ?: error("Impossibile individuare il caricamento di A in v1")

        val afterInputsReady = listCast.index + 1
        require(
            afterInputsReady < beforeConstructor &&
            implementation.instructions.elementAt(afterInputsReady).opcode == Opcode.CONST_WIDE_16
        ) {
            "Il layout dopo il caricamento delle collezioni e' cambiato"
        }

        // IMPORTANTE: inserire prima la diagnostica all'indice PIU' ALTO
        // (beforeConstructor), poi quella all'indice piu' basso
        // (afterInputsReady), cosi' gli indici originali restano validi.

        // v4/v5 sono temporanei morti a questo punto; non toccare v0/v1/v2/v3/v7.
        method.addInstructions(
            beforeConstructor,
            """
                invoke-interface {v1}, Ljava/util/List;->size()I
                move-result v4
                invoke-static {v4}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LXJX A.size = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-static {v1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LXJX A = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-interface {v0}, Ljava/util/Set;->size()I
                move-result v4
                invoke-static {v4}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LXJX C.size = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-static {v0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LXJX C = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """.trimIndent()
        )

        // Snapshot immediatamente DOPO il caricamento di A e C,
        // PRIMA del calcolo di B e PRIMA della costruzione di Lkqgs.
        // v4/v5 vengono sovrascritti subito dopo dal codice originale.
        method.addInstructions(
            afterInputsReady,
            """
                invoke-interface {v1}, Ljava/util/List;->size()I
                move-result v4
                invoke-static {v4}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
                move-result-object v4
                const-string v5, "POST-LOAD A.size = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I


                # Identifica la classe concreta della lista A senza modificarla.
                invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                move-result-object v4
                invoke-virtual {v4}, Ljava/lang/Class;->getName()Ljava/lang/String;
                move-result-object v4
                const-string v5, "POST-LOAD A.class = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-static {v1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "POST-LOAD A = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-interface {v0}, Ljava/util/Set;->size()I
                move-result v4
                invoke-static {v4}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
                move-result-object v4
                const-string v5, "POST-LOAD C.size = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                invoke-static {v0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "POST-LOAD C = "
                invoke-virtual {v5, v4}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v4
                const-string v5, "LegheFC-LXJX"
                invoke-static {v5, v4}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """.trimIndent()
        )

    }
}
