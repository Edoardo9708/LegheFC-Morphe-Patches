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
 * Leghe FC 13.2.1 (build 445): read-only Y (rewarded ad success)
 * diagnostic. Computes the remaining seconds from the timestamp stored
 * in LXJl.D, the same state that Y writes after the ad closes.
 *
 * The injector only inserts instructions directly before Activity.finish()
 * and after Activity.setResult(), when local registers v0..v5 are dead.
 * Does not modify ads, purchases, account entitlements or unlock durations.
 */
@Suppress("unused")
val rewardedFlowDiagnosticPatch = bytecodePatch(
    name = "Leghe FC - Diagnostica timer v3",
    description = "Dopo la pubblicita premiata mostra i secondi residui dello sblocco temporaneo salvato dall'app.",
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
            definingClass = "Lit/quadronica/leghe/ui/feature/gopremium/activity/GoPremiumActivity;",
            name = "Y"
        ).method

        val beforeFinish = method.indexOfFirstInstructionOrThrow {
            opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.name == "finish" &&
                getReference<MethodReference>()?.definingClass == "Landroid/app/Activity;"
        }

        // Registers from the original dex:
        // Y: registersSize=7, insSize=1 -> p0=v6, v0..v5 are locals.
        // Here, after setResult, these locals are not live anymore.
        method.addInstructions(
            beforeFinish,
            """
                iget-object v0, p0, LEzy;->H:LXJl;
                iget-object v0, v0, LXJl;->D:Leadg;
                invoke-interface {v0}, Leadg;->getValue()Ljava/lang/Object;
                move-result-object v0
                check-cast v0, Ljava/lang/Number;
                invoke-virtual {v0}, Ljava/lang/Number;->longValue()J
                move-result-wide v2
                invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
                move-result-wide v4
                sub-long v2, v2, v4
                const-wide/16 v4, 0x3e8
                div-long v2, v2, v4
                invoke-static {v2, v3}, Ljava/lang/String;->valueOf(J)Ljava/lang/String;
                move-result-object v0
                const-string v1, "LAB Y: secondi residui = "
                invoke-virtual {v1, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v0
                const/4 v1, 0x1
                invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
                move-result-object v0
                invoke-virtual {v0}, Landroid/widget/Toast;->show()V
            """.trimIndent()
        )
    }
}
