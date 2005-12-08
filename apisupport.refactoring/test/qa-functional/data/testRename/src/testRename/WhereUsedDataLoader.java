package testRename;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WhereUsedDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-whu";
    
    private static final long serialVersionUID = 1L;
    
    public WhereUsedDataLoader() {
        super("testRename.WhereUsedDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WhereUsedDataLoader.class, "LBL_WhereUsed_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WhereUsedDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
