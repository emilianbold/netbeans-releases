/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */
 
/* $Id$ */

package org.netbeans.modules.form;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
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
        super(FormDataObject.class);
        setDisplayName(NbBundle.getBundle(FormDataLoader.class).getString("PROP_FormLoader_Name"));

        setActions(new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(CustomizeBeanAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CompileAction.class),
            null,
            SystemAction.get(ExecuteAction.class),
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
        });

    }

    /** finds file with the same name and specified extension in the same folder as param javaFile */
    static public FileObject findFile(FileObject javaFile, String ext) {
        if (javaFile == null) return null;
        String name = javaFile.getName();
        int indx = name.indexOf('$');
        if (indx > 0) {
            name = name.substring(0, indx);
        }
        if (javaFile.getParent() == null) return null; // handle critical situation
        return javaFile.getParent().getFileObject(name, ext);

    }

    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    protected FileObject findPrimaryFile(FileObject fo) {
        String ext = fo.getExt();
        if (ext.equals(JAVA_EXTENSION))
            return(findFile(fo, FORM_EXTENSION) == null) ? null : fo;

        if (ext.equals(FORM_EXTENSION)) {
            return findFile(fo, JAVA_EXTENSION);
        }

        if (ext.equals(CLASS_EXTENSION)) {
            if (findFile(fo, FORM_EXTENSION) != null)
                return findFile(fo, JAVA_EXTENSION);
            else
                return null;
        }

        return null;
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
        throws DataObjectExistsException, java.io.IOException {
        //    Thread.dumpStack();
        return new FormDataObject(findFile(primaryFile, FORM_EXTENSION), primaryFile, this);
    }

    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     * /
     protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
     return new JavaLoader.JavaEntry(obj, primaryFile);
     }

     /** Creates right secondary entry for given file. The file is said to
     * belong to an object created by this loader.
     *
     * @param secondaryFile secondary file for which we want to create entry
     * @return the entry
     */
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
        //    Thread.dumpStack();
        String ext = secondaryFile.getExt();
        if (ext.equals(CLASS_EXTENSION)) {
            secondaryFile.setImportant(false);
            return new FileEntry.Numb(obj, secondaryFile);             // entries for .class file
        } else if (ext.equals(FORM_EXTENSION)) {
            secondaryFile.setImportant(true);
            FileEntry formEntry = new FileEntry(obj, secondaryFile);   // entries for .form files
            ((FormDataObject)obj).formEntry = formEntry;
            return formEntry;
        } else return null;
    }
}
