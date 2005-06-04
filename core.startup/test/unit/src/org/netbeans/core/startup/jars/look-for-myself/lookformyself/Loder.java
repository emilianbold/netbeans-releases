package lookformyself;

import java.io.IOException;
import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class Loder extends UniFileLoader {
    
    public static boolean foundEarly;
    
    public Loder() {
        super(DataObject.class);
    }
    
    protected void initialize() {
        super.initialize();
        ExtensionList el = new ExtensionList();
        el.addExtension("nonexistent");
        setExtensions(el);
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
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        throw new IOException("Never called");
    }
    
}
