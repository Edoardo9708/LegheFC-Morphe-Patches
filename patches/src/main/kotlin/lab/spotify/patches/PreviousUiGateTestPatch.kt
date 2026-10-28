package lab.spotify.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

/**
 * Spotify 9.1.90.2270 - test mirato della sola abilitazione UI
 * del pulsante "brano precedente".
 *
 * APK originale SHA-256:
 * 5c05366de31788fd0eec6e046c3610311acf5e63c15345e5bdaca6e8f09fb3b4
 *
 * Non modifica lo stato del player, le liste di restrizioni, la sessione
 * Android, il motore nativo, le licenze o gli annunci.
 *
 * Soltanto nella funzione dpb1.e(PlayerState,Z,Z), se l'unico motivo
 * di disabilitazione del pulsante precedente e' "mft_disallow",
 * rende true il booleano UI calcolato dal codice originale.
 *
 * Restano bloccati i casi con ad_disallow o qualsiasi altra restrizione.
 * Logcat: logcat -v time -s Spotify-Prev:I
 */
@Suppress("unused")
val spotifyPreviousUiGateTestPatch = bytecodePatch(
    name = "Spotify - Previous button UI gate test",
    description = "Tests enabling the Previous button only when mft_disallow is the sole restriction. Does not change playback permissions.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Spotify",
            packageName = "com.spotify.music",
            targets = listOf(AppTarget(version = "9.1.90.2270"))
        )
    )

    execute {
        val clsName = "Lp/dpb1;"
        val method = Fingerprint(
            definingClass = clsName,
            name = "e",
            returnType = "Lp/ogb1;",
            parameters = listOf("Lcom/spotify/player/model/PlayerState;", "Z", "Z")
        ).method
        val body = method.implementation
            ?: error("Spotify Previous UI: controls method has no bytecode")
        require(body.registerCount == 16) {
            "Spotify Previous UI: unexpected register count"
        }

        val instructions = body.instructions.toList()
        val reasonsCalls = instructions.withIndex().filter { (_, insn) ->
            val ref = (insn as? ReferenceInstruction)?.reference as? MethodReference
            insn.opcode == Opcode.INVOKE_VIRTUAL &&
                ref?.definingClass == "Lcom/spotify/player/model/Restrictions;" &&
                ref.name == "disallowSkippingPrevReasons" &&
                ref.parameterTypes.isEmpty() &&
                ref.returnType == "Lp/da60;"
        }
        require(reasonsCalls.size == 1) {
            "Spotify Previous UI: previous-restrictions getter was not unique"
        }

        val reasonsIndex = reasonsCalls.single().index
        val reasonsResult = instructions.getOrNull(reasonsIndex + 1)
        require(
            reasonsResult?.opcode == Opcode.MOVE_RESULT_OBJECT &&
                (reasonsResult as? OneRegisterInstruction)?.registerA == 3
        ) {
            "Spotify Previous UI: reasons register changed"
        }

        // Tolleriamo l'eventuale logger della patch diagnostica che viene
        // inserito tra move-result-object v3 e isEmpty() originale.
        val isEmptyIndex = ((reasonsIndex + 2)..(reasonsIndex + 4)).firstOrNull { i ->
            val instruction = instructions.getOrNull(i)
            val ref = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            instruction?.opcode == Opcode.INVOKE_VIRTUAL &&
                ref?.definingClass == "Ljava/util/AbstractCollection;" &&
                ref.name == "isEmpty" &&
                ref.parameterTypes.isEmpty() &&
                ref.returnType == "Z"
        } ?: error("Spotify Previous UI: isEmpty() on previous restrictions missing")

        val result = instructions.getOrNull(isEmptyIndex + 1)
        require(
            result?.opcode == Opcode.MOVE_RESULT &&
                (result as? OneRegisterInstruction)?.registerA == 3
        ) {
            "Spotify Previous UI: isEmpty result is not in v3"
        }

        val def = mutableClassDefBy(clsName)
        val helper = "morpheAllowPreviousMftOnly"
        require(def.methods.none { it.name == helper }) {
            "Spotify Previous UI: helper already exists"
        }

        // p0: PlayerState originale; p1: risultato originale di isEmpty().
        // Questo helper non modifica gli oggetti ricevuti e rilegge l'elenco
        // originale per verificare che contenga soltanto mft_disallow.
        def.methods.add(
            ImmutableMethod(
                clsName,
                helper,
                listOf(
                    ImmutableMethodParameter("Lcom/spotify/player/model/PlayerState;", null, null),
                    ImmutableMethodParameter("Z", null, null)
                ),
                "Z",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(5)
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                        invoke-virtual {p0}, Lcom/spotify/player/model/PlayerState;->restrictions()Lcom/spotify/player/model/Restrictions;
                        move-result-object v0
                        invoke-virtual {v0}, Lcom/spotify/player/model/Restrictions;->disallowSkippingPrevReasons()Lp/da60;
                        move-result-object v0

                        # Consenti l'override soltanto con una singola restrizione.
                        invoke-virtual {v0}, Ljava/util/AbstractCollection;->size()I
                        move-result v1
                        const/4 v2, 0x1
                        if-ne v1, v2, :original

                        const-string v1, "mft_disallow"
                        invoke-virtual {v0, v1}, Ljava/util/AbstractCollection;->contains(Ljava/lang/Object;)Z
                        move-result v1
                        if-eqz v1, :original

                        const-string v0, "Spotify-Prev"
                        const-string v1, "UI_GATE: mft-only override"
                        invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

                        const/4 v0, 0x1
                        return v0

                        :original
                        return p1
                    """.trimIndent()
                )
            }
        )

        // L'originale ha appena scritto il risultato di isEmpty() in v3.
        // p0 e' il PlayerState; soltanto v3 (booleano UI) viene eventualmente
        // ricalcolato. Nessun altro registro originale viene toccato.
        method.addInstructions(
            isEmptyIndex + 2,
            """
                invoke-static {p0, v3}, Lp/dpb1;->morpheAllowPreviousMftOnly(Lcom/spotify/player/model/PlayerState;Z)Z
                move-result v3
            """.trimIndent()
        )
    }
}
