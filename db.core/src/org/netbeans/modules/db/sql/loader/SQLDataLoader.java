/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
