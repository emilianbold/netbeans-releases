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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/** Implementation of file manager for given classpath.
 *
 * @author Petr Hrebejk
 */
public class CachingFileManager implements JavaFileManager {
    
    protected final CachingArchiveProvider provider;
    protected final JavaFileFilterImplementation filter;
    protected final ClassPath cp;
    protected final boolean cacheFile;
    protected final boolean ignoreExcludes;
    
    
    public CachingFileManager( CachingArchiveProvider provider, final ClassPath cp, boolean cacheFile, boolean ignoreExcludes) {
        this (provider, cp, null, cacheFile, ignoreExcludes);
    }
    
    /** Creates a new instance of CachingFileManager */
    public CachingFileManager( CachingArchiveProvider provider, final ClassPath cp, final JavaFileFilterImplementation filter, boolean cacheFile, boolean ignoreExcludes) {
        this.provider = provider;
        this.cp = cp;
        this.cacheFile = cacheFile;
        this.filter = filter;
        this.ignoreExcludes = ignoreExcludes;
    }
    
    // FileManager implementation ----------------------------------------------
    
    // XXX omit files not of given kind
    public Iterable<JavaFileObject> list( Location l, String packageName, Set<JavaFileObject.Kind> kinds, boolean recursive ) {
     
        if (recursive) {
            throw new UnsupportedOperationException ("Recursive listing is not supported in archives");
        }
        
//        long start = System.currentTimeMillis();
        
        String folderName = FileObjects.convertPackage2Folder( packageName );
                        
        List<Iterator<JavaFileObject>> idxs = new LinkedList<Iterator<JavaFileObject>>();
        for(ClassPath.Entry entry : this.cp.entries()) {
            try {
                Archive archive = provider.getArchive( entry.getURL(), cacheFile );
                if (archive != null) {
                    Iterable<JavaFileObject> entries = archive.getFiles( folderName, ignoreExcludes?null:entry, kinds, filter);
                    idxs.add( entries.iterator() );
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        // System.out.println("  LIST TIME for " + packageName + " = " + ( System.currentTimeMillis() - start ) );
        return Iterators.toIterable( Iterators.chained( idxs ) );
    }
       
    public javax.tools.FileObject getFileForInput( Location l, String pkgName, String relativeName ) {        
        
        for( ClassPath.Entry root : this.cp.entries()) {
            try {
                Archive  archive = provider.getArchive (root.getURL(), cacheFile);
                if (archive != null) {
                    Iterable<JavaFileObject> files = archive.getFiles(FileObjects.convertPackage2Folder(pkgName), ignoreExcludes?null:root, null, filter);
                    for (JavaFileObject e : files) {
                        if (relativeName.equals(e.getName())) {
                            return e;
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }
    
    public JavaFileObject getJavaFileForInput (Location l, String className, JavaFileObject.Kind kind) {
        String[] namePair = FileObjects.getParentRelativePathAndName(className);
        if (namePair == null) {
            return null;
        }
        namePair[1] = namePair[1] + kind.extension;
        for( ClassPath.Entry root : this.cp.entries()) {
            try {
                Archive  archive = provider.getArchive (root.getURL(), cacheFile);
                if (archive != null) {
                    Iterable<JavaFileObject> files = archive.getFiles(namePair[0], ignoreExcludes?null:root, null, filter);
                    for (JavaFileObject e : files) {
                        if (namePair[1].equals(e.getName())) {
                            return e;
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }

        
    public javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ();
    }
    
    public JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ();
    }        
    
    public void flush() throws IOException {
        // XXX Do nothing?
    }

    public void close() throws IOException {
        // XXX Do nothing?
    }
    
    public int isSupportedOption(String string) {
        return -1;
    }
    
    public boolean handleOption (final String head, final Iterator<String> tail) {
        return false;
    }

    public boolean hasLocation(Location location) {
        return true;
    }
    
    public ClassLoader getClassLoader (final Location l) {
        return null;
    }    
    
    public String inferBinaryName (Location l, JavaFileObject javaFileObject) {        
        if (javaFileObject instanceof FileObjects.Base) {
            final FileObjects.Base base = (FileObjects.Base) javaFileObject;
            final StringBuilder sb = new StringBuilder ();
            sb.append (base.getPackage());
            sb.append('.'); //NOI18N
            sb.append(base.getNameWithoutExtension());
            return sb.toString();
        }
        else if (javaFileObject instanceof SourceFileObject) {
            org.openide.filesystems.FileObject fo = ((SourceFileObject)javaFileObject).file;
            for (org.openide.filesystems.FileObject root : this.cp.getRoots()) {
                if (FileUtil.isParentOf(root,fo)) {
                    String relativePath = FileUtil.getRelativePath(root,fo);
                    int index = relativePath.lastIndexOf('.');                                    //NOI18N
                    assert index > 0;                    
                    final String result = relativePath.substring(0,index).replace('/','.');       //NOI18N
                    return result;
                }
            }
        }
        return null;
    }                
    
    //Static helpers - temporary
    
    public static URL[] getClassPathRoots (final ClassPath cp) {
       assert cp != null;
       final List<ClassPath.Entry> entries = cp.entries();
       final List<URL> result = new ArrayList<URL>(entries.size());
       for (ClassPath.Entry entry : entries) {
           result.add (entry.getURL());
       }
       return result.toArray(new URL[result.size()]);
    }            

    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {        
        return fileObject instanceof FileObjects.FileBase 
               && fileObject0 instanceof FileObjects.FileBase 
               && ((FileObjects.FileBase)fileObject).getFile().equals(((FileObjects.FileBase)fileObject).getFile());
    }
}
