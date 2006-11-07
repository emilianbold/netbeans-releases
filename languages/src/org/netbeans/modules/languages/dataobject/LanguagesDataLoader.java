package org.netbeans.modules.languages.dataobject;

import org.netbeans.modules.languages.LanguagesManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

import java.io.IOException;
import java.util.Iterator;

public class LanguagesDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 1L;

    public LanguagesDataLoader() {
        super("org.netbeans.modules.languages.dataobject.LanguagesDataObject");
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(LanguagesDataLoader.class, "LBL_mf_loader_name");
    }

    protected void initialize() {
        super.initialize();
        Iterator it = LanguagesManager.getDefault ().getSupportedMimeTypes ().
            iterator ();
        while (it.hasNext ()) {
            String mimeType = (String) it.next ();
            if (mimeType.equals ("text/xml")) continue;
            getExtensions().addMimeType (mimeType);
        }
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new LanguagesDataObject(primaryFile, this);
    }

    protected String actionsContext() {
        return "Loaders/Languages/Actions";
    }
}
