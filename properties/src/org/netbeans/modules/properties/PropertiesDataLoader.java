/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.io.SafeException;

/** 
 * Data loader which recognizes properties files.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 * @author Ian Formanek, Petr Jiricka
 * @author Marian Petras
 */
public final class PropertiesDataLoader extends MultiFileLoader {

    /** Extension for properties files. */
    static final String PROPERTIES_EXTENSION = "properties"; // NOI18N

    /** Character used to separate parts of bundle properties file name */
    public static final char PRB_SEPARATOR_CHAR = '_';

    /** Generated serial version UID. */
    static final long serialVersionUID =4384899552891479449L;
    
    /** name of property with extensions */
    public static final String PROP_EXTENSIONS = "extensions"; // NOI18N
    
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

    
    /** */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(PropertiesDataLoader.class,
                                   "PROP_PropertiesLoader_Name");       //NOI18N
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/text/x-properties/Actions/";                    //NOI18N
    }
    
    /**
     * This method returns <code>null</code> because it uses method
     * {@link #actionsContext}.
     *
     * @return  <code>null</code>
     */
    protected SystemAction[] defaultActions() {
        return null;
    }

    /**
     * @return  <code>PropertiesDataObject</code> for the specified
     *          <code>FileObject</code>
     */
    protected MultiDataObject createMultiObject(final FileObject fo)
            throws IOException {
        return new PropertiesDataObject(fo, this);
    }

    /** */
    protected FileObject findPrimaryFile (FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }
        if (fo.getExt().equalsIgnoreCase(PROPERTIES_EXTENSION)) {
            
            /*
             * returns a file whose name is the shortest valid prefix
             * corresponding to an existing file
             */
            String fName = fo.getName();
            int index = fName.indexOf(PRB_SEPARATOR_CHAR);
            while (index != -1) {
                FileObject candidate = fo.getParent().getFileObject(
                        fName.substring(0, index), fo.getExt());
                if (candidate != null) {
                    return candidate;
                }
                index = fName.indexOf(PRB_SEPARATOR_CHAR, index + 1);
            }
            return fo;
        } else {
            return getExtensions().isRegistered(fo) ? fo : null;
        }
    }

    /**
     * @return  <code>PropertiesFileEntry</code> for the given file
     */
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj,
                                                       FileObject primaryFile) {
        return new PropertiesFileEntry(obj, primaryFile);
    }

    /**
     * @return  <code>PropertiesFileEntry</code> for the given file
     */
    protected MultiDataObject.Entry createSecondaryEntry(
            MultiDataObject obj,
            FileObject secondaryFile) {
        return new PropertiesFileEntry(obj, secondaryFile);
    }
    

    /**
     * Sets the extension list for this data loader.
     * This data loader will then recognize all files having any extension
     * of the given list.
     *
     * @param  extList  list of extensions
     */
    public void setExtensions(ExtensionList ext) {
        putProperty(PROP_EXTENSIONS, ext, true);
    }

    /**
     * Get the extension list for this data loader.
     *
     * @return  list of extensions
     * @see  #setExtensions
     */
    public ExtensionList getExtensions() {
        ExtensionList l = (ExtensionList) getProperty(PROP_EXTENSIONS);
        if (l == null) {
            l = new ExtensionList();
            putProperty(PROP_EXTENSIONS, l, false);
        }
        return l;
    }

    /** Writes extensions to the stream.
    * @param oo ignored
    */
    public void writeExternal (java.io.ObjectOutput oo) throws IOException {
        super.writeExternal (oo);

        oo.writeObject (getProperty (PROP_EXTENSIONS));
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    public void readExternal (java.io.ObjectInput oi)
    throws IOException, ClassNotFoundException {
        SafeException se;
        try {
            super.readExternal (oi);
            se = null;
        } catch (SafeException se2) {
            se = se2;
        }

        setExtensions ((ExtensionList)oi.readObject ());
        if (se != null) throw se;
    }
    
}
