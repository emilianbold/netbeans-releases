/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html;

import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.awt.BorderLayout;

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.awt.HtmlBrowser;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.OpenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;


/**
* Loader for Html DataObjects.
*
* @author Jan Jancura
*/
public class HtmlLoader extends UniFileLoader {


    static final long serialVersionUID =-5809935261731217882L;
    public HtmlLoader() {
        super (HtmlDataObject.class);
    }

    protected void initialize () {
        setDisplayName(NbBundle.getBundle(HtmlLoader.class).
                       getString("PROP_HtmlLoader_Name"));
        getExtensions ().addExtension ("html"); // NOI18N
        getExtensions ().addExtension ("htm"); // NOI18N
        getExtensions ().addExtension ("shtml"); // NOI18N

        setActions (new SystemAction[] {
                        SystemAction.get (ViewAction.class),
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        SystemAction.get (RenameAction.class),
                        null,
                        SystemAction.get (SaveAsTemplateAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class),
                    });

/*ExtensionList ext = new ExtensionList();
        ext.addExtension("txt"); // NOI18N
        setExtensions(ext);
*/
    }

    
    protected MultiDataObject createMultiObject (final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new HtmlDataObject (primaryFile, this);
    }
}
