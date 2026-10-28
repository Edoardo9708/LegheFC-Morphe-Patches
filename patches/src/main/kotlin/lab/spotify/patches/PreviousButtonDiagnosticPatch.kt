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
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

/**
 * Spotify 9.1.90.2270, versionCode 147078674, APK SHA-256:
 * 5c05366de31788fd0eec6e046c3610311acf5e63c15345e5bdaca6e8f09fb3b4.
 *
 * DIAGNOSTICA SOLTANTO:
 * - logga i motivi originali che vietano il pulsante "precedente";
 * - logga quando viene costruito uno SkipToPrevTrackCommand.
 *
 * Non modifica restrizioni, permessi, valori restituiti o comandi.
 * Lettura sul telefono: adb logcat -d -v time -s Spotify-Prev:I
 */
@Suppress("unused")
val spotifyPreviousButtonDiagnosticPatch = bytecodePatch(
    name = "Spotify - Previous button diagnostics",
    description = "Logs previous-track restrictions and command creation to ADB Logcat without changing playback.",
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
        val controlsClass = "Lp/dpb1;"
        val commandBuilderClass =
            "Lcom/spotify/player/model/command/AutoValue_SkipToPrevTrackCommand${'$'}Builder;"

        // dpb1.e(PlayerState, boolean, boolean): costruzione delle abilitazioni
        // dei controlli. Il risultato della chiamata sotto rimane in v3.
        val controls = Fingerprint(
            definingClass = controlsClass,
            name = "e",
            returnType = "Lp/ogb1;",
            parameters = listOf("Lcom/spotify/player/model/PlayerState;", "Z", "Z")
        ).method
        val controlsBody = controls.implementation
            ?: error("Spotify Prev: missing controls bytecode")
        val instructions = controlsBody.instructions.toList()

        val reasonsCalls = instructions.withIndex().filter { (_, insn) ->
            val ref = (insn as? ReferenceInstruction)?.reference as? MethodReference
            insn.opcode == Opcode.INVOKE_VIRTUAL &&
                ref?.definingClass == "Lcom/spotify/player/model/Restrictions;" &&
                ref.name == "disallowSkippingPrevReasons" &&
                ref.parameterTypes.isEmpty() &&
                ref.returnType == "Lp/da60;"
        }
        require(controlsBody.registerCount == 16 && reasonsCalls.size == 1) {
            "Spotify Prev: unexpected controls implementation"
        }
        val callIndex = reasonsCalls.single().index
        val moveResult = instructions.getOrNull(callIndex + 1)
        val check = instructions.getOrNull(callIndex + 2)
        val checkRef = (check as? ReferenceInstruction)?.reference as? MethodReference
        require(
            moveResult?.opcode == Opcode.MOVE_RESULT_OBJECT &&
                (moveResult as? OneRegisterInstruction)?.registerA == 3 &&
                check?.opcode == Opcode.INVOKE_VIRTUAL &&
                checkRef?.definingClass == "Ljava/util/AbstractCollection;" &&
                checkRef.name == "isEmpty" &&
                checkRef.returnType == "Z" &&
                (check as? FiveRegisterInstruction)?.registerC == 3
        ) {
            "Spotify Prev: reasons register or restriction check changed"
        }

        val controlsDef = mutableClassDefBy(controlsClass)
        require(controlsDef.methods.none { it.name == "morpheLogPrevReasons" }) {
            "Spotify Prev: restrictions logger already present"
        }
        controlsDef.methods.add(
            ImmutableMethod(
                controlsClass,
                "morpheLogPrevReasons",
                listOf(ImmutableMethodParameter("Lp/da60;", null, null)),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(4)
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                        # p0: elenco ORIGINALE dei motivi, non modificato.
                        invoke-static {p0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                        move-result-object v0
                        const-string v1, "Spotify-Prev"
                        const-string v2, "REASONS prev = "
                        invoke-virtual {v2, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v2
                        invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                        return-void
                    """.trimIndent()
                )
            }
        )
        // Tra il move-result-object e l'isEmpty originali: v3 resta invariato.
        controls.addInstructions(
            callIndex + 2,
            "invoke-static {v3}, Lp/dpb1;->morpheLogPrevReasons(Lp/da60;)V"
        )

        // Ogni SkipToPrevTrackCommand con opzioni/default passa da questo build().
        // Entriamo nel metodo originale senza toccare alcun registro.
        val build = Fingerprint(
            definingClass = commandBuilderClass,
            name = "build",
            returnType = "Lcom/spotify/player/model/command/SkipToPrevTrackCommand;",
            parameters = emptyList()
        ).method
        val buildBody = build.implementation
            ?: error("Spotify Prev: missing command builder bytecode")
        require(
            buildBody.registerCount == 5 &&
                buildBody.instructions.firstOrNull()?.opcode == Opcode.NEW_INSTANCE
        ) {
            "Spotify Prev: unexpected SkipToPrevTrackCommand builder"
        }

        val builderDef = mutableClassDefBy(commandBuilderClass)
        require(builderDef.methods.none { it.name == "morpheLogPrevCommand" }) {
            "Spotify Prev: command logger already present"
        }
        builderDef.methods.add(
            ImmutableMethod(
                commandBuilderClass,
                "morpheLogPrevCommand",
                emptyList(),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(2)
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                        const-string v0, "Spotify-Prev"
                        const-string v1, "COMMAND SkipToPrevTrackCommand built"
                        invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                        return-void
                    """.trimIndent()
                )
            }
        )
        build.addInstructions(
            0,
            "invoke-static {}, ${commandBuilderClass}->morpheLogPrevCommand()V"
        )

        // Traccia i due ingressi concreti onSkipToPrevious() presenti
        // nell'APK: entrambi sono callback senza parametri e con ritorno void.
        // Il logger risiede in un helper con registri propri: nessun
        // valore o flusso originale viene alterato.
        for ((callbackClass, marker) in listOf(
            "Lp/zpf0;" to "ANDROID_CALLBACK zpf0.onSkipToPrevious",
            "Lp/gsf0;" to "ANDROID_CALLBACK gsf0.onSkipToPrevious"
        )) {
            val callback = Fingerprint(
                definingClass = callbackClass,
                name = "onSkipToPrevious",
                returnType = "V",
                parameters = emptyList()
            ).method
            val body = callback.implementation
                ?: error("Spotify Prev: missing Android media callback")
            require(body.instructions.isNotEmpty() && body.registerCount >= 1) {
                "Spotify Prev: unexpected Android media callback"
            }
            val cls = mutableClassDefBy(callbackClass)
            val logger = "morpheLogSkipToPrevious"
            require(cls.methods.none { it.name == logger }) {
                "Spotify Prev: callback logger already present"
            }
            cls.methods.add(
                ImmutableMethod(
                    callbackClass, logger, emptyList(), "V",
                    AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                    null, null, MutableMethodImplementation(2)
                ).toMutable().apply {
                    addInstructions(
                        0,
                        """
                            const-string v0, "Spotify-Prev"
                            const-string v1, "$marker"
                            invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                            return-void
                        """.trimIndent()
                    )
                }
            )
            callback.addInstructions(
                0, "invoke-static {}, ${callbackClass}->${logger}()V"
            )
        }

        // Registra il nome dell'azione che arriva allo smistatore
        // Rx del player. Il parametro p1 e' la String originale
        // dell'azione; il helper non la modifica.
        val dispatcherClass = "Lp/hz3;"
        val dispatcher = Fingerprint(
            definingClass = dispatcherClass,
            name = "o",
            returnType = "Lio/reactivex/rxjava3/core/Completable;",
            parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;", "Lp/ihz;")
        ).method
        val dispatcherBody = dispatcher.implementation
            ?: error("Spotify Prev: player dispatcher missing")
        require(dispatcherBody.registerCount == 27 && dispatcherBody.instructions.size > 100) {
            "Spotify Prev: unexpected player dispatcher implementation"
        }
        val dispatcherDef = mutableClassDefBy(dispatcherClass)
        require(dispatcherDef.methods.none { it.name == "morpheLogPlayerDispatch" }) {
            "Spotify Prev: player dispatch logger already present"
        }
        dispatcherDef.methods.add(
            ImmutableMethod(
                dispatcherClass,
                "morpheLogPlayerDispatch",
                listOf(ImmutableMethodParameter("Ljava/lang/String;", null, null)),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null, null, MutableMethodImplementation(3)
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                        const-string v0, "Spotify-Prev"
                        const-string v1, "PLAYER_DISPATCH action="
                        invoke-static {p0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
                        move-result-object v2
                        invoke-virtual {v1, v2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v1
                        invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                        return-void
                    """.trimIndent()
                )
            }
        )
        dispatcher.addInstructions(
            0, "invoke-static/range {p1 .. p1}, Lp/hz3;->morpheLogPlayerDispatch(Ljava/lang/String;)V"
        )
    }
}
