package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/**
 * Esperimento locale T1 per Prezzi Benzina 3.26.10.10.
 *
 * Forza SOLO MySettings.hasEntitlementPremium(Context) a restituire true.
 * Non modifica SharedPreferences, gli acquisti, RevenueCat o lo stato remoto.
 * Non combinare con la diagnostica D1 sullo stesso metodo.
 *
 * Logcat: tag PB-PremiumT1
 */
@Suppress("unused")
val prezziBenzinaPremiumBooleanT1Patch = bytecodePatch(
    name = "Prezzi Benzina - Premium boolean TRUE T1 (LOCAL TEST)",
    description = "Local-only experiment: returns true from hasEntitlementPremium(Context). No purchase or server entitlement changes.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Prezzi Benzina",
            packageName = "org.vernazza.androidfuel",
            targets = listOf(AppTarget(version = "3.26.10.10"))
        )
    )

    execute {
        val owner = "Lit/prezzibenzina/pb3/general/MySettings;"
        val method = Fingerprint(
            definingClass = owner,
            name = "hasEntitlementPremium",
            returnType = "Z",
            parameters = listOf("Landroid/content/Context;")
        ).method

        val body = method.implementation
            ?: error("T1: hasEntitlementPremium(Context) has no bytecode")
        val instructions = body.instructions.toList()
        val strings = instructions.mapNotNull {
            ((it as? ReferenceInstruction)?.reference as? StringReference)?.string
        }.toSet()
        val calls = instructions.mapNotNull {
            (it as? ReferenceInstruction)?.reference as? MethodReference
        }
        val adminCallCount = calls.count {
            it.definingClass == "Lit/prezzibenzina/pb3/session/SessionHelper;" &&
                it.name == "isAdmin"
        }

        require(
            body.registerCount == 5 &&
                instructions.count { it.opcode == Opcode.RETURN } == 2 &&
                "inapp_premium" in strings &&
                adminCallCount == 1
        ) { "T1: original method differs from inspected Prezzi Benzina 3.26.10.10" }

        method.addInstructions(
            0,
            """
                const-string v0, "PB-PremiumT1"
                const-string v1, "T1 hasEntitlementPremium(Context) forced TRUE (local test)"
                invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                const/4 v0, 0x1
                return v0
            """.trimIndent()
        )
    }
}
