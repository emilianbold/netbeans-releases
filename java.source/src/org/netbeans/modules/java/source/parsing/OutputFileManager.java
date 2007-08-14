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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class OutputFileManager extends CachingFileManager {
    
    
    /**
     * Exception used to signal that the sourcepath is broken (project is deleted)
     */
    public class InvalidSourcePath extends IllegalStateException {
        
    }

    private static boolean debug = Boolean.getBoolean("org.netbeans.modules.java.source.parsing.OutputFileManager.debug");      //NOI18N

    private ClassPath scp;
    
    /** Creates a new instance of CachingFileManager */
    public OutputFileManager(CachingArchiveProvider provider, final ClassPath outputClassPath, final ClassPath sourcePath) {
        super (provider, outputClassPath, false, true);
	assert sourcePath != null && outputClassPath != null;        
	this.scp = sourcePath;
    }
    
    
    public @Override JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        
        
        if (kind != JavaFileObject.Kind.CLASS) {
            throw new IllegalArgumentException ();
        }
        else { 
            int index;
            if (sibling != null) {
                index = getActiveRoot (sibling);
            }
            else {
                index = getActiveRoot (FileObjects.convertPackage2Folder(className));
            }            
            if (index == -1) {
                //Deleted project
                throw new InvalidSourcePath ();
            }
            assert index >= 0 : "class: " + className +" sibling: " + sibling +" srcRoots: " + this.scp + " cacheRoots: "  + this.cp;
            assert index < this.cp.entries().size() : "index "+ index +" class: " + className +" sibling: " + sibling +" srcRoots: " + this.scp + " cacheRoots: " + this.cp;
            File activeRoot = new File (URI.create(this.cp.entries().get(index).getURL().toExternalForm()));
            String baseName = className.replace('.', File.separatorChar);       //NOI18N
            String nameStr = baseName + '.' + FileObjects.SIG;
            int nameComponentIndex = nameStr.lastIndexOf(File.separatorChar);            
            if (nameComponentIndex != -1) {
                String pathComponent = nameStr.substring(0, nameComponentIndex);
                new File (activeRoot, pathComponent).mkdirs();
            }
            else {
                activeRoot.mkdirs();
            }                                                            
            File f = FileUtil.normalizeFile(new File (activeRoot, nameStr));
            return OutputFileObject.create (activeRoot, f);
        }
    }    
        
    public @Override javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling )
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        assert pkgName != null;
        assert relativeName != null;
        if (sibling == null) {
            throw new IllegalArgumentException ("sibling == null");
        }        
        final int index = getActiveRoot (sibling);
        if (index == -1) {
            //Deleted project
            throw new InvalidSourcePath ();
        }
        assert index >= 0 && index < this.cp.entries().size();
        File activeRoot = new File (URI.create(this.cp.entries().get(index).getURL().toExternalForm()));
        File folder;
        if (pkgName.length() == 0) {
            folder = activeRoot;
        }
        else {
            folder = new File (activeRoot,FileObjects.convertPackage2Folder(pkgName));
        }
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException ();
            }
        }
        File file = new File (folder,relativeName);
        return OutputFileObject.create (activeRoot,file);
    }
        
        
    
    private int getActiveRoot (final javax.tools.FileObject file) throws IOException {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if ( eSize == 1) {
            return 0;
        }        
        if (eSize == 0) {
            return -1;
        }
        Iterator<ClassPath.Entry> it = entries.iterator();
        for (int i = 0; it.hasNext(); i++) {
            URL rootUrl = it.next().getURL();
            if (isParentOf(rootUrl, file.toUri().toURL())) {
                return i;
            }
        }
        return -2;
    }
    
    private boolean isParentOf (URL folder, final URL file) throws IOException {
        assert folder != null && file != null;
        return file.toExternalForm().startsWith(folder.toExternalForm());
    }
    
    private int getActiveRoot (String baseName) {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if (eSize == 1) {
            return 0;
        }
        if (eSize == 0) {
            return -1;
        }
        String name, parent = null;
	int index = baseName.lastIndexOf('/');              //NOI18N        
	if (index<0) {
            name = baseName;	    
	}
	else {
            parent = baseName.substring(0, index);
            name = baseName.substring(index+1);
	}
        index = name.indexOf('$');                          //NOI18N
	if (index > 0) {
	    name = name.substring(0,index);
	}
        Iterator<ClassPath.Entry> it = entries.iterator();
        for (int i=0; it.hasNext(); i++) {            
            FileObject root = it.next().getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parent);
                if (parentFile != null) {
                    if (parentFile.getFileObject(name, FileObjects.JAVA) != null) {
                        return i;
                    }
                }
            }
        }        
	return -2;
    }
    
    private static boolean debug (String message) {
        if (debug) {
            Logger.getLogger("global").log(Level.INFO, message);
        }
        return true;
    }        
    
}
