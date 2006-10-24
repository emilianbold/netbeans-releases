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
import java.util.HashMap;


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
    HashMap<File,Archive> archives;

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
        archives = new HashMap<File,Archive>();
    }
    
    /** Gets archive for given file.
     */
    public synchronized Archive getArchive( File file, boolean cacheFile)  {
        
        File canonical = file;

        Archive archive = archives.get( canonical );

        if ( archive == null ) {
            archive = create( canonical, cacheFile );
            archives.put( canonical, archive );
        }

        return archive;
        
    }
    
    /** Gets archives for files
     */
    public synchronized Archive[] getArchives( File[] files, boolean cacheFile) {
        
        Archive[] archives = new Archive[ files.length ];        
        for( int i = 0; i < files.length; i++ ) {
            archives[i] = getArchive( files[i], cacheFile );
        }
        
        return archives;
    }       
    
    
    public synchronized void removeArchive (final File file) {
        final Archive archive = archives.remove(file);
        if (archive != null) {
            archive.clear();
        }
    }
    
    public synchronized void clearArchive (final File file) {
        Archive archive = archives.get(file);
        if (archive != null) {
            archive.clear();
        }
    }
        
    // Private methods ---------------------------------------------------------
    
    /** Creates proper archive for given file.
     */
    private static Archive create( File file, boolean cacheFile ) {
        
        if ( !file.canRead() ) {
            throw new IllegalArgumentException( "Can't read file " + file );
        }
        
        if ( file.isDirectory() ) {
            return new FolderArchive( file );
        }        
        else {
            //todo: check if the file is really an archive
            return new CachingArchive( file, cacheFile );
        }
 
    }
            
          
}
