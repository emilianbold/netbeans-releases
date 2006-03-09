package org.netbeans.modules.j2ee.websphere6.dd.loaders.appbnd;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSAppBndDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-appbnd+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSAppBndDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.appbnd.WSAppBndDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSAppBndDataLoader.class, "LBL_WSAppBnd_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSAppBndDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
