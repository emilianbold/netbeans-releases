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


package org.netbeans.modules.form;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.java.JavaDataLoader;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
 * there is their source and form file.
 *
 * @author Ian Formanek
 */
public class FormDataLoader extends JavaDataLoader {
    /* The standard extensions of the recognized files */
    public static final String FORM_EXTENSION = "form"; // NOI18N

    static final long serialVersionUID =7259146057404524013L;
    /** Constructs a new FormDataLoader */
    public FormDataLoader() {
        super("org.netbeans.modules.form.FormDataObject"); // NOI18N
    }

    
    /** Gets default display name. Overides superclass method. */
    protected String defaultDisplayName() {
        return org.openide.util.NbBundle.getBundle(FormDataLoader.class)
                 .getString("PROP_FormLoader_Name"); // NOI18N
    }

    /** Gets default actions. Overrides superclass method. */
    protected SystemAction[] defaultActions() {
        DataLoader javaLoader = getLoader(JavaDataLoader.class);          
        SystemAction[] javaActions = javaLoader.getActions();
        SystemAction[] formActions = new SystemAction[javaActions.length+2];
        formActions[0] = javaActions[0]; // OpenAction            
        formActions[1] = SystemAction.get(EditAction.class);
        formActions[2] = null;
        System.arraycopy(javaActions, 1, formActions, 3, javaActions.length-1);
        return formActions;
    }

    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    protected FileObject findPrimaryFile(FileObject fo) {
        String ext = fo.getExt();
        if (ext.equals(FORM_EXTENSION))
            return FileUtil.findBrother(fo, JAVA_EXTENSION);

        FileObject javaFile = super.findPrimaryFile(fo);
        return javaFile != null
                    && FileUtil.findBrother(javaFile, FORM_EXTENSION) != null ?
            javaFile : null;
    }

    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, java.io.IOException
    {
        return new FormDataObject(FileUtil.findBrother(primaryFile, FORM_EXTENSION),
                                  primaryFile,
                                  this);
    }

    // from JavaDataLoader
    // [?] Probably needed in case FormDataObject is deserialized, then the
    // secondary entry is created additionally.
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
                                                         FileObject secondaryFile)
    {
        if (secondaryFile.getExt().equals(FORM_EXTENSION)) {
            secondaryFile.setImportant(true);
            FileEntry formEntry = new FileEntry(obj, secondaryFile);
            ((FormDataObject)obj).formEntry = formEntry;
            return formEntry;
        }
        return super.createSecondaryEntry(obj, secondaryFile);
    }
}
