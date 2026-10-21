group = "lab.leghefc"

patches {
    about {
        name = "Leghe FC LAB Patches"
        description = "Patch sperimentali e locali per Leghe FC"
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
