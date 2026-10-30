import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import java.io.File;

/** Read-only check of the patched DEX hook and runtime extension. */
public final class VerifyApk {
    public static void main(String[] args) throws Exception {
        var dex = DexFileFactory.loadDexContainer(new File(args[0]), Opcodes.getDefault());
        int hooks = 0;
        boolean extension = false;
        for (String name : dex.getDexEntryNames()) {
            for (var cls : dex.getEntry(name).getDexFile().getClasses()) {
                if (cls.getType().equals("Llab/skisafari/SkiSafariUnlock;")) extension = true;
                if (!cls.getType().equals("Lcom/unity3d/player/UnityPlayerActivity;")) continue;
                for (var method : cls.getMethods()) {
                    if (!method.getName().equals("onCreate") || method.getImplementation() == null) continue;
                    for (var instruction : method.getImplementation().getInstructions()) {
                        if (instruction instanceof ReferenceInstruction ref && ref.getReference() instanceof MethodReference target
                                && target.getDefiningClass().equals("Llab/skisafari/SkiSafariUnlock;")
                                && target.getName().equals("start")) hooks++;
                    }
                }
            }
        }
        if (hooks != 1 || !extension) throw new IllegalStateException("Expected one onCreate hook and extension: hooks=" + hooks + ", extension=" + extension);
        System.out.println("PASS: one Unity Activity onCreate hook and runtime extension present.");
    }
}
