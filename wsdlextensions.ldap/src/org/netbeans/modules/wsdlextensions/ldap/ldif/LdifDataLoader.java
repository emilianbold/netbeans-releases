package org.netbeans.modules.wsdlextensions.ldap.ldif;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class LdifDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-ldif";

    private static final long serialVersionUID = 1L;

    public LdifDataLoader() {
        super("org.netbeans.modules.wsdlextensions.ldap.ldif.LdifDataObject");
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(LdifDataLoader.class, "LBL_Ldif_loader_name");
    }

    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new LdifDataObject(primaryFile, this);
    }

    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }

}
