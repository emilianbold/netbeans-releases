/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.hibernate.loaders.reveng;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class HibernateRevengDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-hibernate-reveng+xml";
    private static final long serialVersionUID = 1L;

    public HibernateRevengDataLoader() {
        super("org.netbeans.modules.hibernate.loaders.reveng.HibernateRevengDataObject");
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(HibernateRevengDataLoader.class, "LBL_HibernateReveng_loader_name");
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new HibernateRevengDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
}
