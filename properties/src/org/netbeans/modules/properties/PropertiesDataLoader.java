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


package org.netbeans.modules.properties;


import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;


/** 
 * Data loader which recognizes properties files.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 * @author Ian Formanek, Petr Jiricka
 */
public final class PropertiesDataLoader extends UniFileLoader {

    /** Extension for properties files. */
    static final String PROPERTIES_EXTENSION = "properties"; // NOI18N

    /** Character used to separate parts of bundle properties file name */
    public static final char PRB_SEPARATOR_CHAR = '_';

    /** Generated serial version UID. */
    static final long serialVersionUID =4384899552891479449L;
    
    
    /** Creates new PropertiesDataLoader. */
    public PropertiesDataLoader() {
        super("org.netbeans.modules.properties.PropertiesDataObject"); // NOI18N
        
        // Set extentions. Due performance reasons do it here instead in initialize method.
        // During startup it's in findPrimaryFile method called getExtensions method. If the 
        // extentions list was not set in constructor the initialize method would be called
        // during startup, but we want to avoid the initialize call since we don't need
        // actions and display name initialized during startup time.
        ExtensionList extList = new ExtensionList();
        extList.addExtension(PROPERTIES_EXTENSION);
        // Add .impl for CORBA module.
        extList.addExtension("impl"); // NOI18N
        setExtensions(extList);
    }

    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getBundle(PropertiesDataLoader.class).getString("PROP_PropertiesLoader_Name");
    }
    
    /** Gets default system actions. Overrides superclass method. */
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(EditAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            null,
            SystemAction.get(NewAction.class),
            SystemAction.get(SaveAsTemplateAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class)
        };
    }

    /** Creates new PropertiesDataObject for this FileObject.
     * @param fo FileObject
     * @return new PropertiesDataObject
     */
    protected MultiDataObject createMultiObject(final FileObject fo)
    throws IOException {
        return new PropertiesDataObject(fo, this);
    }

    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        if (fo.getExt().equalsIgnoreCase(PROPERTIES_EXTENSION)) {
            // returns a file whose name is the shortest valid prefix corresponding to an existing file
            String fName = fo.getName();
            int index = fName.indexOf(PRB_SEPARATOR_CHAR);
            while (index != -1) {
                FileObject candidate = fo.getParent().getFileObject(fName.substring(0, index), fo.getExt());
                if (candidate != null) {
                    return candidate;
                }
                index = fName.indexOf(PRB_SEPARATOR_CHAR, index + 1);
            }
            return fo;
        } else
            return getExtensions().isRegistered(fo) ? fo : null;
    }

    /** 
     * Creates the right primary entry for given primary file.
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new PropertiesFileEntry(obj, primaryFile);
    }

    /** Creates right secondary entry for given file. The file is said to
     * belong to an object created by this loader.
     *
     * @param secondaryFile secondary file for which we want to create entry
     * @return the entry
     */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        PropertiesFileEntry pfe = new PropertiesFileEntry(obj, secondaryFile);
        return pfe;
    }
}
