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


package org.netbeans.modules.url;


import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;


/**
 * Data loader which recognizes URL files.
 *
 * @author Ian Formanek
 */
public class URLDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-7407252842873642582L;
    
    
    /** Creates a new URLDataLoader without the extension. */
    public URLDataLoader() {
        super("org.netbeans.modules.url.URLDataObject");                //NOI18N
    }

    
    /**
     * Initializes this loader. This method is called only once the first time
     * this loader is used (not for each instance).
     */
    protected void initialize () {
        super.initialize();

        ExtensionList ext = new ExtensionList();
        ext.addExtension("url");                                        //NOI18N
        setExtensions(ext);
    }

    /** */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(URLDataLoader.class,
                                   "PROP_URLLoader_Name");              //NOI18N
    }
    
    /** */
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(EditAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            null,
            SystemAction.get(SaveAsTemplateAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }
    
    /**
     * @return  <code>URLDataObject</code> for the specified file
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException {
        return new URLDataObject(primaryFile, this);
    }

}
