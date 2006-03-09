package org.netbeans.modules.j2ee.websphere6.dd.loaders.webext;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSWebExtDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-webext+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSWebExtDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.webext.WSWebExtDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSWebExtDataLoader.class, "LBL_WSWebExt_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSWebExtDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
