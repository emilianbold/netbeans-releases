/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xsl;

import java.io.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.xml.core.XMLDataLoader;
import org.netbeans.modules.xml.core.actions.CollectXMLAction;

/**
 * XSL object loader. It is mime type based.
 *
 * @author Libor Kramolis
 */
public final class XSLDataLoader extends UniFileLoader {
       
    private static final long serialVersionUID = 6494980346565290872L;
    
    /** Creates a new instance of SchemaLoader */
    public XSLDataLoader() {
        super ("org.netbeans.modules.xsl.XSLDataObject"); // NOI18N
    }
    
    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addMimeType (XSLDataObject.MIME_TYPE);
        setExtensions (ext);
    }
    
    /**
     * Lazy init actions.
     */
    protected SystemAction[] defaultActions () {    
        return new SystemAction[] {
            SystemAction.get (EditAction.class),
            SystemAction.get (FileSystemAction.class),
            null,
            SystemAction.get (CollectXMLAction.class),
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
    
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName () {
        return Util.THIS.getString ("NAME_XSLDataLoader");
    }
    
    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new XMLDataLoader.XMLFileEntry (obj, primaryFile);  //adds smart templating
    }

    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException {
        return new XSLDataObject (primaryFile, this);
    }
    
}
