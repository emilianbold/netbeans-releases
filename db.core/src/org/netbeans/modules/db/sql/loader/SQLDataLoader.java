/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.loader;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 7673892611992320469L;
    
    private static final String SQL_EXTENSION = "sql"; // NOI18N
    
    public SQLDataLoader() {
        super("org.netbeans.modules.db.sql.loader.SQLDataObject"); // NOI18N
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new SQLDataObject(primaryFile, this);
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(SQLDataLoader.class, "LBL_LoaderName");
    }

    protected void initialize() {
        super.initialize();
        ExtensionList extensions = new ExtensionList();
        extensions.addExtension(SQL_EXTENSION);
        setExtensions(extensions);
    }
    
    protected String actionsContext() {
        return "Loaders/text/x-sql/Actions/"; // NOI18N    
    }
}
