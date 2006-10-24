/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
 * there is their source and form file.
 *
 * @author Ian Formanek
 */
public class FormDataLoader extends MultiFileLoader {
    /** The standard extensions of the recognized files */
    public static final String FORM_EXTENSION = "form"; // NOI18N
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    static final long serialVersionUID =7259146057404524013L;
    /** Constructs a new FormDataLoader */
    public FormDataLoader() {
        super("org.netbeans.modules.form.FormDataObject"); // NOI18N
    }

    
    /** Gets default display name. Overides superclass method. */
    @Override
    protected String defaultDisplayName() {
        return org.openide.util.NbBundle.getBundle(FormDataLoader.class)
                 .getString("PROP_FormLoader_Name"); // NOI18N
    }

    @Override
    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }

    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
	// never recognize folders.
        if (fo.isFolder()) return null;
        String ext = fo.getExt();
        if (ext.equals(FORM_EXTENSION))
            return FileUtil.findBrother(fo, JAVA_EXTENSION);

        FileObject javaFile = findJavaPrimaryFile(fo);
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
    @Override
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
    @Override
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
                                                         FileObject secondaryFile)
    {
        assert FORM_EXTENSION.equals(secondaryFile.getExt());
        
        FileEntry formEntry = new FileEntry(obj, secondaryFile);
        ((FormDataObject)obj).formEntry = formEntry;
        return formEntry;
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
    }

    private FileObject findJavaPrimaryFile(FileObject fo) {
        if (fo.getExt().equals(JAVA_EXTENSION))
            return fo;
        return null;
    }
}
