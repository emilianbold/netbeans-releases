package org.netbeans.modules.sql.project.ui;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class SQLDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-sql";
    
    private static final long serialVersionUID = 1L;
    
    public SQLDataLoader() {
        super("org.netbeans.modules.sql.project.ui.SQLDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(SQLDataLoader.class, "LBL_SQL_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new SQLDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
