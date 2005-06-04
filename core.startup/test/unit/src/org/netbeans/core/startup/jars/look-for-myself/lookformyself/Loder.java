package lookformyself;

import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class Loder extends org.openide.util.actions.CallableSystemAction {
    
    public static boolean foundEarly;
    
    public Loder() {
        initialize();
    }
    
    protected void initialize() {
        // Now the real stuff:
        foundEarly = foundNow();
    }
    
    public static boolean foundNow() {
        Iterator it = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class)).allInstances().iterator();
        while (it.hasNext()) {
            ModuleInfo m = (ModuleInfo)it.next();
            if (m.getCodeNameBase().equals("lookformyself")) {
                return true;
            }
        }
        return false;
    }
    
    public void performAction () {
        throw new IllegalStateException("Never called");
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return "LoderAction";
    }
    
}
