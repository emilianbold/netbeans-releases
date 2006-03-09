package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSEjbExtDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-ejbext+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSEjbExtDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.ejbext.WSEjbExtDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSEjbExtDataLoader.class, "LBL_WSEjbExt_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSEjbExtDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
