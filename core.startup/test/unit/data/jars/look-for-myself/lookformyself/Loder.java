package lookformyself;

import org.openide.modules.ModuleInfo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;

public class Loder extends CallableSystemAction {
    
    public static boolean foundEarly;
    
    public Loder() {
        initialize();
    }
    
    @Override
    protected void initialize() {
        // Now the real stuff:
        foundEarly = foundNow();
    }
    
    public static boolean foundNow() {
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.getCodeNameBase().equals("lookformyself")) {
                return true;
            }
        }
        return false;
    }
    
    public void performAction () {
        throw new IllegalStateException("Never called");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return "LoderAction";
    }
    
}
