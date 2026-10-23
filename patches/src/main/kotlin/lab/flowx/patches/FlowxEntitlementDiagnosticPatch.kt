package lab.flowx.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

/**
 * Flowx 4.650, APK esaminato (classes.dex).
 *
 * Lxi1; = EntitlementRow, costruttore sintetico:
 *   <init>(I, String, String, String, String, String, String)V
 * Il primo int e' la maschera Kotlin dei parametri predefiniti.
 * Maschera 0: utilizza tutte le sei stringhe fornite.
 *
 * Lz5;->k()V valuta gli elementi dell'elenco e chiama:
 *   Ldw6;->B(Lxi1;J)Z
 *
 * La patch NON sostituisce gli elementi reali: all'inizio di k()
 * crea un oggetto di prova separato e stampa soltanto il risultato.
 */
@Suppress("unused")
val flowxEntitlementDiagnosticPatch = bytecodePatch(
    name = "Flowx - Diagnostica autorizzazione Gold",
    description = "Verifica locale dello stato Gold con dati di prova, senza modificare l'abbonamento.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Flowx",
            packageName = "com.enzuredigital.weatherbomb",
            targets = listOf(AppTarget(version = "4.650"))
        )
    )

    execute {
        val target = "Lz5;"
        val helper = "morpheTestEntitlement"
        val entry = Fingerprint(
            definingClass = target,
            name = "k",
            returnType = "V",
            parameters = emptyList()
        ).method
        val body = entry.implementation ?: error("Flowx: metodo k() senza bytecode")

        // Sicurezza: modifica consentita solo se il bytecode contiene
        // esattamente la chiamata a B() verificata nell'APK 4.650.
        val bCalls = body.instructions.filter { instruction ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            instruction.opcode == Opcode.INVOKE_STATIC &&
                reference?.definingClass == "Ldw6;" &&
                reference.name == "B" &&
                reference.parameterTypes.toList() == listOf("Lxi1;", "J") &&
                reference.returnType == "Z"
        }
        require(body.registerCount == 5 && bCalls.size == 1) {
            "Flowx: il controllo Gold non corrisponde al bytecode verificato"
        }

        val targetClass = mutableClassDefBy(target)
        require(targetClass.methods.none { it.name == helper }) {
            "Flowx: il metodo diagnostico e' gia' presente"
        }

        targetClass.methods.add(
            ImmutableMethod(
                target,
                helper,
                emptyList(),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(13)
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                        # v0: istanza EntitlementRow da usare solo nel test.
                        new-instance v0, Lxi1;

                        # v1: maschera Kotlin = 0; nessun campo predefinito.
                        const/4 v1, 0x0

                        # v2/v3: prodotto e stato validi secondo B().
                        const-string v2, "gold"
                        const-string v3, "active"

                        # v4-v7: metadati dimostrativi, mai letti dal server.
                        const-string v4, "morphe-test"
                        const-string v5, "morphe-test"
                        const-string v6, "morphe-test"
                        const-string v7, ""

                        # Inizializza l'oggetto usando il costruttore reale.
                        invoke-direct/range {v0 .. v7}, Lxi1;-><init>(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

                        # Ottiene un riferimento temporale; con active B() non verifica la scadenza.
                        invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
                        move-result-wide v8

                        # Chiama la funzione originale con il SOLO oggetto di test.
                        invoke-static {v0, v8, v9}, Ldw6;->B(Lxi1;J)Z
                        move-result v10

                        # Converte il booleano restituito in una stringa.
                        invoke-static {v10}, Ljava/lang/String;->valueOf(Z)Ljava/lang/String;
                        move-result-object v10

                        # Composizione del messaggio da registrare nel log.
                        const-string v11, "Flowx-Entitlement"
                        const-string v12, "TEST gold/active B() = "
                        invoke-virtual {v12, v10}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v12

                        # Scrive il risultato nel log Android.
                        invoke-static {v11, v12}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                        # Fine diagnostica: nessuna modifica al livello reale.
                        return-void
                    """.trimIndent()
                )
            }
        )

        // Esegue il test anche se l'elenco ricevuto dal server e' vuoto.
        // Il metodo k() prosegue poi con tutte le sue istruzioni originali.
        entry.addInstructions(
            0,
            "invoke-static {}, Lz5;->morpheTestEntitlement()V"
        )
    }
}
