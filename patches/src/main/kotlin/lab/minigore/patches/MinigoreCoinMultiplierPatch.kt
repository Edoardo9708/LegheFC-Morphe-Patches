package lab.minigore.patches

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.intOption
import app.morphe.patcher.patch.rawResourcePatch
import java.io.File

/**
 * Minigore 2 1.28 (versionCode 28), ARMv7 and x86.
 * Configurable GameUtility::addScores multiplier; no save, shop or IAP edits.
 * Screen compatibility and APK signing stay separate.
 */
private const val ORIGINAL_PACKAGE = "net.mountainsheep.minigore2zombies"
private const val PARALLEL_PACKAGE = "net.mountainsheep.minigore2zombiex"

private data class NativeSite(
    val path: String,
    val fileSize: Int,
    val offset: Int,
    val length: Int,
    val prefix: String,
    val suffix: String,
    val oldBytes: String,
    val isArm: Boolean
)

private val sites = listOf(
    NativeSite(
        "lib/armeabi-v7a/libminigore2.so", 2518520, 0xE46EE, 2,
        "7944c0f21f0299460c682168", "02fb00f501f5fb1000f53f60", "8a58", true
    ),
    NativeSite(
        "lib/x86/libminigore2.so", 3767904, 0x112AD0, 6,
        "5f1a28008bb32cf5ffff8b06", "0faf7d0805f06b1f0083ec0c", "8bb88c601f00", false
    )
)

private fun String.toHexBytes(): ByteArray =
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()

private fun ByteArray.matchesAt(offset: Int, expected: ByteArray): Boolean =
    offset >= 0 && offset + expected.size <= size &&
        expected.indices.all { this[offset + it] == expected[it] }

private fun prepare(site: NativeSite, original: ByteArray, multiplier: Int): ByteArray {
    require(original.size == site.fileSize &&
        original.matchesAt(site.offset - 12, site.prefix.toHexBytes()) &&
        original.matchesAt(site.offset + site.length, site.suffix.toHexBytes())) {
        "Minigore 2: native library differs from v1.28: ${site.path}"
    }
    val current = original.copyOfRange(site.offset, site.offset + site.length)
    val isUnmodified = current.contentEquals(site.oldBytes.toHexBytes())
    val isAlreadyPatched = if (site.isArm) {
        current.size == 2 && current[1] == 0x22.toByte()
    } else {
        current.size == 6 && current[0] == 0xbf.toByte() &&
            current[2] == 0.toByte() && current[3] == 0.toByte() &&
            current[4] == 0.toByte() && current[5] == 0x90.toByte()
    }
    require(isUnmodified || isAlreadyPatched) {
        "Minigore 2: unexpected instructions at 0x${site.offset.toString(16)} in ${site.path}"
    }
    val replacement = if (site.isArm) {
        byteArrayOf(multiplier.toByte(), 0x22) // Thumb movs r2, #N.
    } else {
        byteArrayOf(0xbf.toByte(), multiplier.toByte(), 0, 0, 0, 0x90.toByte()) // x86 mov edi, N; nop.
    }
    return original.copyOf().apply { replacement.copyInto(this, site.offset) }
}

@Suppress("unused")
val minigoreCoinMultiplierPatch = rawResourcePatch(
    name = "Minigore 2 - Moltiplicatore monete",
    description = "Moltiplicatore dei guadagni configurabile (1-255) in Minigore 2 v1.28.",
    default = false
) {
    compatibleWith(
        Compatibility(
            name = "Minigore 2",
            packageName = ORIGINAL_PACKAGE,
            targets = listOf(AppTarget(version = "1.28"))
        ),
        Compatibility(
            name = "Minigore 2 Morphe",
            packageName = PARALLEL_PACKAGE,
            targets = listOf(AppTarget(version = "1.28"))
        )
    )

    val coinMultiplier by intOption(
        key = "coinMultiplier",
        default = 10,
        title = "Moltiplicatore monete",
        description = "Numero intero da 1 a 255. Per cambiarlo rigenera l'APK.",
        required = true,
        validator = { it != null && it in 1..255 }
    )

    execute {
        val factor = requireNotNull(coinMultiplier)
        require(factor in 1..255) { "Moltiplicatore non valido: $factor" }
        require(packageMetadata.versionName == "1.28" &&
            packageMetadata.versionCode == "28" &&
            packageMetadata.packageName in setOf(ORIGINAL_PACKAGE, PARALLEL_PACKAGE)) {
            "Minigore 2 v1.28 (28) originale o Morphe parallelo richiesto."
        }

        // Validate both native sites before writing either.
        val updates: List<Pair<File, ByteArray>> = sites.map { site ->
            val file = get(site.path)
            require(file.isFile) { "Minigore 2: missing native library ${site.path}" }
            file to prepare(site, file.readBytes(), factor)
        }
        updates.forEach { (file, data) -> file.writeBytes(data) }
        println("Minigore 2: score multiplier x$factor patched on ARMv7 and x86.")
    }
}
