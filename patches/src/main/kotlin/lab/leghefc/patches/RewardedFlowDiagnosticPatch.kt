package lab.leghefc.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Traces rewarded access to a Toast entirely on the user's device, without ADB.
 *
 * The injection site is deliberately after setResult and immediately before
 * Activity.finish, when local registers v0/v1 are dead on the success path.
 *
 * Original verified APK: 13.2.1 build 445.
 * X: finish at dex code unit offset 96, locals v0/v1 unused after setResult.
 * Y: finish at dex code unit offset 75, locals v0/v1 unused after setResult.
 *
 * No entitlement, duration, purchase or advertising API is modified.
 */
@Suppress("unused")
val rewardedFlowDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica sblocco v2",
    description = "Mostra 5/15 minuti solo quando il percorso di sblocco conclude con successo. Non modifica lo stato Premium.",
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

        listOf(
            "X" to "LAB: sblocco 5 minuti (X)",
            "Y" to "LAB: sblocco 15 minuti (Y)"
        ).forEach { (methodName, message) ->
            val method = Fingerprint(
                definingClass = activity,
                name = methodName
            ).method

            // The two dead registers are reused only after setResult has
            // consumed the success Intent. This avoids clobbering live values.
            val beforeFinish = method.indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    getReference<MethodReference>()?.name == "finish" &&
                    getReference<MethodReference>()?.definingClass == "Landroid/app/Activity;"
            }

            method.addInstructions(
                beforeFinish,
                """
                    const-string v0, "$message"
                    const/4 v1, 0x1
                    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
                    move-result-object v0
                    invoke-virtual {v0}, Landroid/widget/Toast;->show()V
                """.trimIndent()
            )
        }
    }
}
