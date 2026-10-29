package lab.prezzibenzina.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

/**
 * Prezzi Benzina 3.26.10.10 (versionCode 312610100), base.apk / classes6.dex.
 *
 * Diagnostica D1: registra il valore booleano restituito da cinque metodi,
 * prima di OGNI return originale. Non cambia valori, preferenze, acquisti,
 * abbonamenti, interfaccia, gestione delle pubblicita' o chiamate RevenueCat.
 *
 * Logcat tag: PB-Entitlement
 * Marker del codice: PB-20261029-D1 (presente in ogni riga)
 */
@Suppress("unused")
val prezziBenzinaEntitlementDiagnosticPatch = bytecodePatch(
    name = "Prezzi Benzina - Entitlement diagnostics D1",
    description = "Logs original Premium, price history and ads decisions without modifying purchases or feature access.",
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
        val target = "Lit/prezzibenzina/pb3/general/MySettings;"

        data class Probe(
            val name: String,
            val parameters: List<String>,
            val registers: Int,
            val expectedReturns: Int,
            val keys: List<String>,
            val expectedCallClass: String,
            val expectedCallName: String,
            val expectedCallCount: Int,
            val logger: String
        )

        val prefsClass = "Landroid/content/SharedPreferences;"
        val specs = listOf(
            Probe(
                "hasEntitlementNoAds", emptyList(), 4, 2,
                listOf("no_ads", "has_huawei_subscription"),
                prefsClass, "getBoolean", 2, "morpheLogNoAds"
            ),
            Probe(
                "hasEntitlementPremium", listOf("Landroid/content/Context;"), 5, 2,
                listOf("inapp_premium"),
                "Lit/prezzibenzina/pb3/session/SessionHelper;", "isAdmin", 1,
                "morpheLogPremium"
            ),
            Probe(
                "hasPriceHistoryEntitlement", listOf("Landroid/content/Context;"), 5, 2,
                listOf("inapp_price_history"),
                "Lit/prezzibenzina/pb3/session/SessionHelper;", "isAdmin", 1,
                "morpheLogPriceHistory"
            ),
            Probe(
                "showBannerAds", listOf("Landroid/content/Context;"), 3, 1,
                emptyList(), target, "showInterstitialAds", 1,
                "morpheLogBanner"
            ),
            Probe(
                "showInterstitialAds", listOf("Landroid/content/Context;"), 3, 2,
                emptyList(), target, "actualAdsLevel", 1,
                "morpheLogInterstitial"
            )
        )

        // Identifichiamo e controlliamo tutti i metodi PRIMA di modificarne uno.
        // Qualunque struttura diversa da quella vista nel DEX blocca la patch.
        val targets: List<Triple<Probe, MutableMethod, List<Pair<Int, Int>>>> =
            specs.map { spec ->
                val method = Fingerprint(
                    definingClass = target,
                    name = spec.name,
                    returnType = "Z",
                    parameters = spec.parameters
                ).method
                val implementation = method.implementation
                    ?: error("Prezzi Benzina: metodo " + spec.name + " senza bytecode")
                val instructions = implementation.instructions.toList()

                val calls = instructions.mapNotNull {
                    (it as? ReferenceInstruction)?.reference as? MethodReference
                }
                val keys = instructions.mapNotNull {
                    ((it as? ReferenceInstruction)?.reference as? StringReference)?.string
                }.toSet()

                val returns = instructions.withIndex().filter {
                    it.value.opcode == Opcode.RETURN
                }.map { indexed ->
                    val register = (indexed.value as? OneRegisterInstruction)?.registerA
                        ?: error("Prezzi Benzina: RETURN non riconosciuto")
                    indexed.index to register
                }
                val callCount = calls.count {
                    it.definingClass == spec.expectedCallClass &&
                        it.name == spec.expectedCallName
                }

                require(
                    implementation.registerCount == spec.registers &&
                        returns.size == spec.expectedReturns &&
                        spec.keys.all { it in keys } &&
                        callCount == spec.expectedCallCount
                ) {
                    "Prezzi Benzina: " + spec.name +
                        " non corrisponde al DEX originale 3.26.10.10"
                }

                Triple(spec, method, returns)
            }

        val mutableClass = mutableClassDefBy(target)
        require(specs.none { spec ->
            mutableClass.methods.any { it.name == spec.logger }
        }) {
            "Prezzi Benzina: helper di diagnostica gia' presenti"
        }

        // Ogni helper legge un booleano, lo converte in testo e lo stampa.
        // Ha registri propri: nessuna variabile della funzione originale cambia.
        for (spec in specs) {
            val prefix = "PB-20261029-D1 " + spec.name + " RETURN = "
            val loggerBody = """
                # v0: tag Logcat; v1: messaggio; v2: valore convertito.
                # p0: booleano originale restituito dal chiamante.
                const-string v0, "PB-Entitlement"
                const-string v1, "$prefix"
                invoke-static {p0}, Ljava/lang/String;->valueOf(Z)Ljava/lang/String;
                move-result-object v2
                invoke-virtual {v1, v2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v1
                invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                return-void
            """.trimIndent()

            mutableClass.methods.add(
                ImmutableMethod(
                    target,
                    spec.logger,
                    listOf(ImmutableMethodParameter("Z", null, null)),
                    "V",
                    AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                    null,
                    null,
                    MutableMethodImplementation(4)
                ).toMutable().apply {
                    addInstructions(0, loggerBody)
                }
            )
        }

        // Inseriamo la chiamata immediatamente prima di ogni RETURN.
        // L'ordine inverso preserva gli indici originali delle istruzioni.
        // Il registro del valore di ritorno viene solo letto, non modificato.
        for ((spec, method, returns) in targets) {
            for ((index, register) in returns.asReversed()) {
                method.addInstructions(
                    index,
                    "invoke-static {v" + register + "}, " +
                        target + "->" + spec.logger + "(Z)V"
                )
            }
        }
    }
}
