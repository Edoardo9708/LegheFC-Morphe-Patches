group = "lab.leghefc"

patches {
    about {
        name = "Edoardo9708 Patches"
        description = "Patch per Prezzi Benzina: routing token Maps via microG e disattivazione annunci banner/interstitial"
        source = "https://github.com/Edoardo9708/LegheFC-Morphe-Patches"
        author = "Edoardo9708"
        contact = "https://github.com/Edoardo9708/LegheFC-Morphe-Patches/issues"
        website = "https://github.com/Edoardo9708/LegheFC-Morphe-Patches"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
    // L'implementazione ufficiale di Change installer source, senza duplicare il codice.
    implementation("app.morphe:morphe-patches-library:1.9.0-dev.4")
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
