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


package org.netbeans.modules.image;


import java.util.HashSet;
import java.util.Set;
import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import java.util.Iterator;
 

/** 
 * Data loader which recognizes image files.
 * @author Petr Hamernik, Jaroslav Tulach
 */
public class ImageDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-8188309025795898449L;
    
    private static Set notSupportedExtensions = new HashSet ();
    
    /** Creates new image loader. */
    public ImageDataLoader() {
        // Set the representation class.
        super("org.netbeans.modules.image.ImageDataObject"); // NOI18N
        
        // List of recognized extensions.
        ExtensionList ext = new ExtensionList();
        ext.addExtension("jpg"); // NOI18N
        ext.addExtension("jpeg"); // NOI18N
        ext.addExtension("jpe"); // NOI18N
        ext.addExtension("gif"); // NOI18N
        // PhotoShop frequently saves image files with capital extensions:
        ext.addExtension("JPG"); // NOI18N
        ext.addExtension("JPEG"); // NOI18N
        ext.addExtension("JPE"); // NOI18N
        ext.addExtension("GIF"); // NOI18N

        // Note: PNG requires 1.3 or higher.
        ext.addExtension("png"); // NOI18N
        ext.addExtension("PNG"); // NOI18N
        
        setExtensions(ext);
    }
    
    // Michael Wever 11/01/2002
    protected FileObject findPrimaryFile(FileObject fo){
        FileObject retValue = super.findPrimaryFile( fo );
        if( retValue == null  && !fo.isFolder() ){
            /* Check through for new extensions */
            String ext = fo.getExt();
            
            if (!notSupportedExtensions.contains (ext)) {
                Iterator it = javax.imageio.ImageIO.getImageReadersBySuffix(ext);
                if( it.hasNext() ){
                    /* Use the first available ImageIO loader */
                    retValue = fo;
                    getExtensions().addExtension(ext);
                } else
                    notSupportedExtensions.add (ext);
            }
        }
        return retValue;
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getBundle(ImageDataLoader.class).getString("PROP_ImageLoader_Name");
    }
    
    /** Gets default system actions. Overrides superclass method. */
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(FileSystemAction.class),
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
            SystemAction.get(PropertiesAction.class)
        };
    }

    /** Create the image data object.
     * @param primaryFile the primary file (e.g. <code>*.gif</code>)
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     * @exception java.io.IOException should not be thrown
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new ImageDataObject(primaryFile, this);
    }

}
