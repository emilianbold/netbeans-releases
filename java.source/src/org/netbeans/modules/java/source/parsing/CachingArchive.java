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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.tools.JavaFileObject;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.util.Factory;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Exceptions;

public class CachingArchive implements Archive {



    private final File archiveFile;
    private final boolean keepOpened;
    private ZipFile zipFile;
        
    // Cache
    private Map<String,List<ZipRecord>> folders2files;
    
    // Constructors ------------------------------------------------------------    
    
    /** Creates a new instance of archive from zip file */
    public CachingArchive( File archiveFile, boolean keepOpened) {
        this.archiveFile = archiveFile;
        this.keepOpened = keepOpened;
    }
        
    // Archive implementation --------------------------------------------------
   
    
    /** Gets all files in given folder
     */
    public Iterable<JavaFileObject> getFiles( String folderName, JavaFileFilterImplementation filter) throws IOException {
        doInit();        
        List<ZipRecord> files = folders2files.get( folderName );        
        if (files == null) {
            return Collections.<JavaFileObject>emptyList();
        }
        else {
            assert !keepOpened || zipFile != null;
            return Iterators.translating(files, new JFOFactory(folderName, archiveFile, zipFile));
        }
    }          
    
    
    public synchronized void clear () {
        this.folders2files = null;
    }
                      
    // ILazzy implementation ---------------------------------------------------
    
    public synchronized boolean isInitialized() {
        return folders2files != null;
    }
    
    public synchronized void initialize() {
        folders2files = createMap( archiveFile );
    }
    
    // Private methods ---------------------------------------------------------
    
    private synchronized void doInit() {
        if ( !isInitialized() ) {
            initialize();
        }
    }
    
    private Map<String,List<ZipRecord>> createMap(File file ) {        
        if (file.canRead()) {
            try {
                ZipFile zip = new ZipFile (file);
                try {
                    final Map<String,List<ZipRecord>> map = new HashMap<String,List<ZipRecord>>();

                    for ( Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                        ZipEntry entry = e.nextElement();
                        String name = entry.getName();
                        int i = name.lastIndexOf('/');
                        String dirname = i == -1 ? "" : name.substring(0, i /* +1 */);
                        String basename = name.substring(i+1);
                        if (basename.length() == 0) {
                            basename = null;
                        }
                        List<ZipRecord> list = map.get(dirname);
                        if (list == null) {
                            list = new ArrayList<ZipRecord>();                
                            map.put(dirname, list);
                        }

                        if ( basename != null ) {
                            list.add( new ZipRecord (basename, entry.getTime()));
                        }

                    }
                    return map;
                } finally {
                    if (keepOpened) {
                        this.zipFile = zip;
                    }
                    else {
                        try {
                            zip.close();
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }
            } catch (IOException ioe) {
                
            }
        }
        return Collections.<String,List<ZipRecord>>emptyMap();
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class JFOFactory implements Factory<JavaFileObject,ZipRecord>  {
        
        private final String pkg;
        private final File archiveFile;
        private final ZipFile zipFile;
        
        JFOFactory( String pkg, File archiveFile, ZipFile zipFile ) {
            this.pkg = pkg;
            this.archiveFile = archiveFile;
            this.zipFile = zipFile;
        } 
        
        public JavaFileObject create(final ZipRecord parameter) {
            if (zipFile == null) {
                return FileObjects.zipFileObject(archiveFile, pkg, parameter.baseName, parameter.mtime);
            }
            else {
                return FileObjects.zipFileObject( zipFile, pkg, parameter.baseName, parameter.mtime);
            }
        }
    };
    
    
    private static class ZipRecord {
        private final long mtime;
        private final String baseName;
        
        public ZipRecord (final String baseName, final long mtime) {
            assert baseName != null;
            this.mtime = mtime;
            this.baseName = baseName;
        }
    }
    
        
}
