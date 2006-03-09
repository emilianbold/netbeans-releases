package org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSWebBndDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-webbnd+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSWebBndDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.webbnd.WSWebBndDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSWebBndDataLoader.class, "LBL_WSWebBnd_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSWebBndDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
