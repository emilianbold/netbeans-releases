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

package org.netbeans.modules.java;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Data loader which recognizes Java source files.
*
* @author Petr Hamernik
*/
public final class JavaDataLoader extends MultiFileLoader {
    
    public static final String JAVA_MIME_TYPE = "text/x-java";  //NOI18N
    
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    static final long serialVersionUID =-6286836352608877232L;

    /** Create the loader.
    * Should <em>not</em> be used by subclasses.
    */
    public JavaDataLoader() {
        super("org.netbeans.modules.java.JavaDataObject"); // NOI18N
    }

    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }
    
    protected @Override String defaultDisplayName() {
        return NbBundle.getMessage(JavaDataLoader.class, "PROP_JavaLoader_Name");
    }
    
    /** Create the <code>JavaDataObject</code>.
    * Subclasses should rather create their own data object type.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has a data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        if (primaryFile.getExt().equals(JAVA_EXTENSION))
            return new JavaDataObject(primaryFile, this);
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
        if (fo.getExt().equals(JAVA_EXTENSION))
            return fo;
        return null;
    }

    /** Create the primary file entry.
    * Subclasses may override {@link JavaDataLoader.JavaFileEntry} and return a new instance
    * of the overridden entry type.
    *
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        if (JAVA_EXTENSION.equals(primaryFile.getExt())) {
//            return new JavaFileEntry (obj, primaryFile);
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
    
    /** Create the map of replaceable strings which is used
    * in the <code>JavaFileEntry</code>. This method may be extended in subclasses
    * to provide the appropriate map for other loaders.
    * This implementation gets the map from the Java system option;
    * subclasses may add other key/value pairs which may be created without knowledge of the
    * file itself.
    *
    * @return the map of string which are replaced during instantiation
    *        from template
    */
    static Map createStringsMap() {
//XXX: Fix when there will be JavaSettings:        Map map = JavaSettings.getDefault().getReplaceableStringsProps();
        Map map = new HashMap ();
        map.put("USER", System.getProperty("user.name"));
        map.put("DATE", DateFormat.getDateInstance(DateFormat.LONG).format(new Date())); // NOI18N
        map.put("TIME", DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())); // NOI18N
        return map;
    }
    
    
    /** This entry defines the format for replacing text during
    * instantiation the data object.
    * Used to substitute keys in the source file.
    */
    public static class JavaFileEntry extends IndentFileEntry {
        static final long serialVersionUID =8244159045498569616L;

        /** Creates new entry. */
        public JavaFileEntry(MultiDataObject obj, FileObject file) {
            super(obj, file);
        }

        /** Provide suitable format for substitution of lines.
        * Should not typically be overridden.
        * @param target the target folder of the installation
        * @param n the name the file will have
        * @param e the extension the file will have
        * @return format to use for formating lines
        */
        protected java.text.Format createFormat (FileObject target, String n, String e) {
            Map map = createStringsMap();

            modifyMap(map, target, n, e);

            JMapFormat format = new JMapFormat(map);
            format.setLeftBrace("__"); // NOI18N
            format.setRightBrace("__"); // NOI18N
            format.setCondDelimiter("$"); // NOI18N
            format.setExactMatch(false);
            return format;
        }

        /** Modify the replacement map.
        * May be extended in subclasses to provide additional key/value
        * pairs sensitive to the details of instantiation.
        * @param map the map to add to
        * @param target the destination folder for instantiation
        * @param n the new file name
        * @param e the new file extension
        */
        protected void modifyMap(Map map, FileObject target, String n, String e) {
            ClassPath cp = ClassPath.getClassPath(target, ClassPath.SOURCE);
            String resourcePath = "";
            if (cp != null) {
                resourcePath = cp.getResourceName(target);
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "No classpath was found for folder: "+target);
            }
            map.put("NAME", n); // NOI18N
            // Yes, this is package sans filename (target is a folder).
            map.put("PACKAGE", resourcePath.replace('/', '.')); // NOI18N
            map.put("PACKAGE_SLASHES", resourcePath); // NOI18N
	    // Fully-qualified name:
	    if (target.isRoot ()) {
		map.put ("PACKAGE_AND_NAME", n); // NOI18N
		map.put ("PACKAGE_AND_NAME_SLASHES", n); // NOI18N
	    } else {
		map.put ("PACKAGE_AND_NAME", resourcePath.replace('/', '.') + '.' + n); // NOI18N
		map.put ("PACKAGE_AND_NAME_SLASHES", resourcePath + '/' + n); // NOI18N
	    }
            // No longer needed due to #6025. (You can just put in quotes, they will not
            // prevent things from being escaped.) But leave the token here for
            // compatibility with old templates. --jglick 26/08/00
            map.put("QUOTES","\""); // NOI18N
        }


        // XXX below are methods formly placed in JavaDataObject. It is
        // a question if rename and copy should be here at all or they should be
        // part of refactoring stuff only.
        
        @Override
        public FileObject rename(String name) throws IOException {
            if (!"package-info".equals(name) && !Utilities.isJavaIdentifier(name)) // NOI18N
                throw new IOException(NbBundle.getMessage(JavaDataObject.class, "FMT_Not_Valid_FileName", name));
            
            FileObject fo = super.rename(name);
            return fo;
        }
        
        @Override
        public FileObject copy(FileObject f, String suffix) throws IOException {
            String origName = getFile().getName();
            FileObject fo = super.copy(f, suffix);
            ClassPath cp = ClassPath.getClassPath(fo,ClassPath.SOURCE);
            if (cp != null) {
                String pkgName = cp.getResourceName(f, '.', false);
                JavaDataObject.renameFO(fo, pkgName, fo.getName(), origName);
            }
            return fo;
        }
        
        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            if (name == null) {
                // special case: name is null (unspecified or from one-parameter createFromTemplate)
                name = FileUtil.findFreeFileName(f, f.getName(), "java"); // NOI18N
            } else if (!Utilities.isJavaIdentifier(name)) {
                throw new IOException(NbBundle.getMessage(JavaDataObject.class, "FMT_Not_Valid_FileName", name));
            }
            
            this.initializeIndentEngine();
            FileObject fo = super.createFromTemplate(f, name);
            
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            String pkgName;
            if (cp != null) {
                pkgName = cp.getResourceName(f, '.', false);
            } else {
                pkgName = "";   //NOI18N
            }
            JavaDataObject.renameFO(fo, pkgName, name, getFile().getName());
            
            return fo;
        }

    }
}
