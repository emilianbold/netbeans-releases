package org.netbeans.modules.j2ee.websphere6.dd.loaders.appext;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSAppExtDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-appext+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSAppExtDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.appext.WSAppExtDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSAppExtDataLoader.class, "LBL_WSAppExt_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSAppExtDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
