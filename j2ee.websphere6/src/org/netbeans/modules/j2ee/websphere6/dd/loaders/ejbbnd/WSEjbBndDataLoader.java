package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbbnd;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSEjbBndDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-ejbbnd+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSEjbBndDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.ejbbnd.WSEjbBndDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSEjbBndDataLoader.class, "LBL_WSEjbBnd_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSEjbBndDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
