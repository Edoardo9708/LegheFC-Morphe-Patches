package lab.skisafari.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.io.File
import java.security.MessageDigest

private const val PACKAGE_NAME = "com.yodo1.skisafarigp"
private const val IL2CPP_PATH = "lib/arm64-v8a/libil2cpp.so"
private const val METADATA_PATH = "assets/bin/Data/Managed/Metadata/global-metadata.dat"
private const val NATIVE_PATCH_PATH = "lib/arm64-v8a/libskisafariunlock.so"

private object SkiSafariResources {
    fun open(path: String) = SkiSafariResources::class.java.classLoader.getResourceAsStream(path)
}

// SHA-256 values are from the original Ski Safari 2.5 (108) APK set pulled from
// the Galaxy S23+. The patch adds its own helper library and never rewrites
// libil2cpp.so.
private const val EXPECTED_IL2CPP_SHA256 =
    "b24154995803633ab95eeea21836f67d74699b305a2ed631a2600c59dfdd639a"
private const val EXPECTED_METADATA_SHA256 =
    "b8d2786c3be10261c7c666ce8155f4d1d6d98ec994586e16334219a5fc0f5144"

private fun File.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(readBytes())
    .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }

private val skiSafariUnlockNativeResources = rawResourcePatch {
    execute {
        val il2cpp = get(IL2CPP_PATH)
        require(il2cpp.isFile) {
            "Ski Safari Unlock: $IL2CPP_PATH non trovato. Selezionare l'APK set ARM64 originale 2.5 (108)."
        }
        require(il2cpp.sha256() == EXPECTED_IL2CPP_SHA256) {
            "Ski Safari Unlock: SHA-256 di libil2cpp.so non corrispondente; patch interrotta."
        }

        val metadata = get(METADATA_PATH)
        require(metadata.isFile && metadata.sha256() == EXPECTED_METADATA_SHA256) {
            "Ski Safari Unlock: metadata IL2CPP assenti o diversi dalla build 2.5 (108); patch interrotta."
        }

        val helper = SkiSafariResources.open("skisafari/libskisafariunlock.so")
            ?: error("Ski Safari Unlock: libreria helper ARM64 assente dal bundle Morphe.")
        val output = get(NATIVE_PATCH_PATH)
        require(output.parentFile?.mkdirs() == true || output.parentFile?.isDirectory == true) {
            "Ski Safari Unlock: impossibile creare la directory ARM64 di destinazione."
        }
        helper.use { input -> output.outputStream().use { stream -> input.copyTo(stream) } }

        val bytes = output.readBytes()
        require(bytes.size > 4096 && bytes[0] == 0x7f.toByte() && bytes[1] == 0x45.toByte() &&
            bytes[2] == 0x4c.toByte() && bytes[3] == 0x46.toByte()) {
            "Ski Safari Unlock: libreria helper ELF incompleta o non valida."
        }
        require(bytes[4] == 2.toByte() && bytes[5] == 1.toByte() &&
            bytes[18] == 0xb7.toByte() && bytes[19] == 0.toByte()) {
            "Ski Safari Unlock: la libreria helper non e' ELF64 little-endian AArch64."
        }
    }
}

@Suppress("unused")
val skiSafariUnlockAllItemsPatch = bytecodePatch(
    name = "Ski Safari – Unlock All Items",
    description = "Experimental: invokes ShopItemHelper.UnlockAllItems once through UnityPlayer's verified game-thread queue after the profile is ready. The game saves changed unlock flags through GameState.Save. Runtime execution is blocked by PairIP on the locally signed build and is not verified.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Ski Safari",
            packageName = PACKAGE_NAME,
            targets = listOf(AppTarget(version = "2.5"))
        )
    )
    dependsOn(skiSafariUnlockNativeResources)
    extendWith("extensions/extension.mpe")

    execute {
        val unityQueue = Fingerprint(
            definingClass = "Lcom/unity3d/player/UnityPlayer;",
            name = "queueGLThreadEvent",
            returnType = "V",
            parameters = listOf("Ljava/lang/Runnable;")
        ).method
        val queueCalls = unityQueue.implementation?.instructions?.mapNotNull {
            (it as? ReferenceInstruction)?.reference as? MethodReference
        } ?: error("Ski Safari Unlock: dispatcher Unity assente.")
        require(queueCalls.any {
            it.definingClass == "Ljava/util/concurrent/ConcurrentLinkedQueue;" && it.name == "add"
        } && queueCalls.any {
            it.definingClass == "Ljava/lang/Runnable;" && it.name == "run"
        }) { "Ski Safari Unlock: dispatcher Unity differente dalla build verificata." }

        val unityActivity = Fingerprint(
            definingClass = "Lcom/unity3d/player/UnityPlayerActivity;",
            name = "onCreate",
            returnType = "V",
            parameters = listOf("Landroid/os/Bundle;")
        ).method
        val implementation = unityActivity.implementation
            ?: error("Ski Safari Unlock: UnityPlayerActivity.onCreate senza bytecode.")
        val references = implementation.instructions.mapNotNull { instruction ->
            (instruction as? ReferenceInstruction)?.reference as? MethodReference
        }
        require(references.any {
            it.definingClass == "Lcom/unity3d/player/UnityPlayer;" && it.name == "<init>"
        } && references.any {
            it.definingClass == "Lcom/unity3d/player/UnityPlayerActivity;" &&
                it.name == "setContentView" && it.parameterTypes == listOf("Landroid/view/View;")
        } && references.any {
            it.definingClass == "Lcom/unity3d/player/UnityPlayer;" && it.name == "requestFocus"
        }) {
            "Ski Safari Unlock: UnityPlayerActivity non corrisponde alla build verificata; patch interrotta."
        }

        val returnIndex = implementation.instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
        require(returnIndex >= 0) { "Ski Safari Unlock: return di onCreate non trovato." }
        unityActivity.addInstructions(
            returnIndex,
            "invoke-static {p0}, Llab/skisafari/SkiSafariUnlock;->start(Landroid/app/Activity;)V"
        )
    }
}
