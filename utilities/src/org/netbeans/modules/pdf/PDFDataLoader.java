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


package org.netbeans.modules.pdf;


import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


/** Loader for PDF files (Portable Document Format).
 * Permits simple viewing of them.
 * @author Jesse Glick
 */
public class PDFDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -4354042385752587850L;

    
    /** Creates loader. */
    public PDFDataLoader() {
        super("org.netbeans.modules.pdf.PDFDataObject"); // NOI18N
    }

    // PENDING who needs this constructor?
    /** Creates loader for specified recognized class. */
    public PDFDataLoader(Class recognizedObject) {
        super(recognizedObject);
    }

    
    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    protected void initialize () {
        ExtensionList extensions = new ExtensionList ();
        extensions.addExtension ("pdf"); // NOI18N
        setExtensions (extensions);
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getMessage (PDFDataLoader.class, "LBL_loaderName");
    }
    
    /** Gets default system actions. Overrides superclass method. */
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
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
        };
    }

    /** Creates multi data objcte for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new PDFDataObject (primaryFile, this);
    }

}
