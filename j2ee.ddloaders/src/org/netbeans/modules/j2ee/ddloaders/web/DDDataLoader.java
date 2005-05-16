/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web;

import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Recognizes deployment descriptors of web application (web.xml file).
 *
 * @author Milan Kuchtiak
 */
public class DDDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213886L;
    private static final String REQUIRED_MIME_1 = "text/x-dd-servlet2.4"; // NOI18N
    private static final String REQUIRED_MIME_2 = "text/x-dd-servlet2.3"; // NOI18N
    private static final String REQUIRED_MIME_3 = "text/x-dd-servlet2.2"; // NOI18N

    public DDDataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.web.DDDataObject");  // NOI18N
    }
    
    protected void initialize () {
        super.initialize ();
        getExtensions().addMimeType(REQUIRED_MIME_1);
        getExtensions().addMimeType(REQUIRED_MIME_2);
        getExtensions().addMimeType(REQUIRED_MIME_3);
    }
    
    protected String defaultDisplayName () {
        return NbBundle.getMessage (DDDataLoader.class, "LBL_loaderName");
    }
    
    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
            SystemAction.get (OpenAction.class),
            SystemAction.get (EditAction.class),
            null,
            SystemAction.get (org.netbeans.modules.j2ee.ddloaders.web.actions.CheckXmlAction.class),
            SystemAction.get (org.netbeans.modules.j2ee.ddloaders.web.actions.ValidateXmlAction.class),
            null,
            SystemAction.get (FileSystemAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            null,
            SystemAction.get (SaveAsTemplateAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
        throws DataObjectExistsException, IOException {
            
        return new DDDataObject (primaryFile, this);
    }

}
