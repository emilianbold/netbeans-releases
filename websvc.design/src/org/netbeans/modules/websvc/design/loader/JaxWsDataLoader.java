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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.loader;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

/** Data loader which recognizes Java source files.
*
* @author Petr Hamernik
*/
public final class JaxWsDataLoader extends MultiFileLoader {
    
    public static final String JAVA_MIME_TYPE = "text/x-java";  //NOI18N
    
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    private static final String PACKAGE_INFO = "package-info";  //NOI18N
    
    static final long serialVersionUID =-6286836352608877232L;

    /** Create the loader.
    * Should <em>not</em> be used by subclasses.
    */
    public JaxWsDataLoader() {
        super("org.netbeans.modules.websvc.design.JaxWsDataObject"); // NOI18N
    }

    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }
    
    protected @Override String defaultDisplayName() {
        return NbBundle.getMessage(JaxWsDataLoader.class, "PROP_JaxWsLoader_Name");
    }
    
    /** Create the <code>JaxWsDataObject</code>.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        if (primaryFile.getExt().equals(JAVA_EXTENSION))
            return new JaxWsDataObject(primaryFile, this);
        return null;
    }

    /** For a given file find the primary file.
    * Subclasses should override this, but still look for the {@link #JAVA_EXTENSION},
    * as the Java source file should typically remain the primary file for the data object.
    * @param fo the file to find the primary file for
    *
    * @return the primary file for this file or <code>null</code> if this file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {
	// never recognize folders.
        if (fo.isFolder()) return null;
        
        // ignore templates using scripting
        if (fo.getAttribute("template") != null && fo.getAttribute("javax.script.ScriptEngine") != null) // NOI18N
            return null;
        
        if (fo.getExt().equals(JAVA_EXTENSION) && JaxWsDataObject.findService(fo)!=null) {
            return fo;
        }
        return null;
    }

    //TODO  delete following methods?
    /** Create the primary file entry.
    * Subclasses may override {@link JavaDataLoader.JavaFileEntry} and return a new instance
    * of the overridden entry type.
    *
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        if (JAVA_EXTENSION.equals(primaryFile.getExt())) {
            return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
        }
        else {
            return new FileEntry(obj, primaryFile);
        }
    }

    /** Create a secondary file entry.
    * By default, {@link FileEntry.Numb} is used for the class files; subclasses wishing to have useful
    * secondary files should override this for those files, typically to {@link FileEntry}.
    *
    * @param secondaryFile secondary file to create entry for
    * @return the entry
    */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        //The JavaDataObject itself has no secondary entries, but its subclasses have.
        //So we have to keep it as MultiFileLoader
        ErrorManager.getDefault().log ("Subclass of JavaDataLoader ("+this.getClass().getName()
                +") has secondary entries but does not override createSecondaryEntries (MultidataObject, FileObject) method."); // NOI18N
        return new FileEntry.Numb(obj, secondaryFile);
    }   
    
}
