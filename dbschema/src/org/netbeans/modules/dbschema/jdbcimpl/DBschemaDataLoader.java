/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

public class DBschemaDataLoader extends UniFileLoader {
    
    static final long serialVersionUID = -8808468937919122876L;
  
    public DBschemaDataLoader () {
        super("org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject");
    }
  
    public DBschemaDataLoader (Class recognizedObject) {
        super (recognizedObject);
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

    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get (FileSystemAction.class),
            SystemAction.get (RecaptureSchemaAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            SystemAction.get (RenameAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            null,
            SystemAction.get (PropertiesAction.class),
        };
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new DBschemaDataObject (primaryFile, this);
    }

}
