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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;


/** Global cache for Archives (zip files and folders).
 *
 * XXX-Use j.net.URL rather than j.io.File
 * XXX-Perf Add swapping for lower memory usage
 *
 * @author Petr Hrebejk
 */
public class CachingArchiveProvider {

    private static CachingArchiveProvider instance;

    // Names to caching zip files
    // XXX-PERF Consider swapping
    HashMap<URL,Archive> archives;

    public static synchronized CachingArchiveProvider getDefault () {
        if (instance == null) {
            instance = new CachingArchiveProvider ();
        }
        return instance;
    }
        
    /** Creates a new instance CachingArchiveProvider 
     *  Can be caleed only from UnitTests or {@link CachingArchiveProvider#getDefault} !!!!!
     */
    CachingArchiveProvider() {
        archives = new HashMap<URL,Archive>();
    }
    
    /** Gets archive for given file.
     */
    public synchronized Archive getArchive( URL root, boolean cacheFile)  {
                
        Archive archive = archives.get(root);

        if (archive == null) {
            archive = create(root, cacheFile);
            if (archive != null) {
                archives.put(root, archive );
            }
        }
        return archive;
        
    }
    
    /** Gets archives for files
     */
    public synchronized Iterable<Archive> getArchives( URL[] roots, boolean cacheFile) {
        
        List<Archive> archives = new ArrayList<Archive>(roots.length);        
        for( int i = 0; i < roots.length; i++ ) {
            Archive a = getArchive( roots[i], cacheFile );
            if (a != null) {
                archives.add(a);
            }            
        }
        return archives;
    }       
    
    
    /** Gets archives for files
     */
    public synchronized Iterable<Archive> getArchives( ClassPath cp, boolean cacheFile) {        
        final List<ClassPath.Entry> entries = cp.entries();
        final List<Archive> archives = new ArrayList<Archive> (entries.size());
        for (ClassPath.Entry entry : entries) {
            Archive a = getArchive(entry.getURL(), cacheFile);
            if (a != null) {
                archives.add (a);
            }
        }        
        return archives;
    }
    
    public synchronized void removeArchive (final URL root) {
        final Archive archive = archives.remove(root);
        if (archive != null) {
            archive.clear();
        }
    }
    
    public synchronized void clearArchive (final URL root) {
        Archive archive = archives.get(root);
        if (archive != null) {
            archive.clear();
        }
    }
        
    // Private methods ---------------------------------------------------------
    
    /** Creates proper archive for given file.
     */
    private static Archive create( URL root, boolean cacheFile ) {
        String protocol = root.getProtocol();
        if ("file".equals(protocol)) {
            File f = new File (URI.create(root.toExternalForm()));
            if (f.isDirectory()) {
                return new FolderArchive (f);
            }
            else {
                return null;
            }
        }
        if ("jar".equals(protocol)) {
            URL inner = FileUtil.getArchiveFile(root);
            protocol = inner.getProtocol();
            if ("file".equals(protocol)) {
                File f = new File (URI.create(inner.toExternalForm()));
                if (f.isFile()) {
                    return new CachingArchive( f, cacheFile );
                }
                else {
                    return null;
                }
            }
        }                
        //Slow
        FileObject fo = URLMapper.findFileObject(root);
        if (fo != null) {
            return new FileObjectArchive (fo);
        }
        else {
            return null;
        }
    }
            
          
}
