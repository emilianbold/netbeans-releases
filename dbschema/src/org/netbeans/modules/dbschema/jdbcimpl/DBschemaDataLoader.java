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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

public class DBschemaDataLoader extends UniFileLoader {

    static final long serialVersionUID = -8808468937919122876L;

    public DBschemaDataLoader () {
        super("org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject");
    }

    protected void initialize() {
        super.initialize();
        ExtensionList extensions = new ExtensionList ();
        extensions.addExtension ("dbschema"); //NOI18N
        setExtensions (extensions);
    }

    protected String defaultDisplayName() {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N
        return bundle.getString("ObjectName");
    }
    
    protected String actionsContext() {
        return "Loaders/text/x-dbschema/Actions"; // NOI18N
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new DBschemaDataObject (primaryFile, this);
    }
}
