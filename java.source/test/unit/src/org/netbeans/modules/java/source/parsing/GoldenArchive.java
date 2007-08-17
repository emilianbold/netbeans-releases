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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.util.Factory;

/** Class which takes a folder and behaves like an archive should behave.
 * It does not apply any optimalizations. However it is good for comparing
 * the results from real archives. Also contains some utility methods for 
 * comparing with other archives.
 *
 * @author Petr Hrebejk
 */    
class GoldenArchive implements Archive {

    private File rootFolder;
    private int rootLength;
    
    // Comparison methods ------------------------------------------------------
    
//    public String getFoldersDiff( Archive archive ) {        
//        return TestUtil.collectionDiff( getFolders(), archive.getFolders() );
//    }
//    
//    public String getFilesDiff( Archive archive ) {
//        return TestUtil.collectionDiff( getFiles(), archive.getFiles() );
//    }
//    
//    public String getFoldersDiff( String folderName, Archive archive ) {
//        return TestUtil.collectionDiff( getFolders( folderName ), archive.getFolders( folderName ) );        
//    }
    
    public String getFilesDiff( String folderName, Archive archive ) throws IOException {
        return TestUtil.collectionDiff( getFiles( folderName, null, null, null), archive.getFiles( folderName, null, null,null) );        
    }

    // Implementation of Archive -----------------------------------------------
    
    public GoldenArchive( File rootFolder ) {
        if ( !rootFolder.isDirectory() ) {
            throw new IllegalArgumentException( "Root folder has to be a directory." );
        }
        if ( !rootFolder.canRead() ) {
            throw new IllegalArgumentException( "Root folder has to be readable." );
        }

        this.rootFolder = rootFolder;
        this.rootLength = rootFolder.getPath().length();
    }


    public Iterable<JavaFileObject> getFiles( String folderName, ClassPath.Entry e, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter ) {

        File folder = new File( rootFolder, folderName );
        
        if ( !folder.exists() ) {
            return null;
        }
                
        File[] files = folder.listFiles();
        List<JavaFileObject> entries = new ArrayList<JavaFileObject>();
        
        for( File f : files ) {
            if ( !f.isDirectory() ) {
                entries.add( FileObjects.fileFileObject (f, rootFolder, null));
            }
        }
        
        Collections.sort( entries, new Comparator<JavaFileObject> () {
            public int compare (JavaFileObject o1, JavaFileObject o2) {
                return o1.toUri().toString().compareTo(o2.toUri().toString());
            }
        });

        return  entries;
    }
    
    public void clear () {
    }
    
    public void add (String pkg, String name) {
        
    }
    
    public void delete (String pkg, String name) {
        
    }
    
            
    // Private methods -----------------------------------------------------

    /** Finds all subfoders of given folder
     */
    private void storeFolders( Collection<File> dest, File folder, boolean isRoot ) {

        File files[] = folder.listFiles();

        boolean hasFiles = false;
        for (File f : files) {
            if ( f.isDirectory() ) {
                storeFolders( dest, f, false );
            }
            else {
                hasFiles = true;
            }
        }
        
        if ( hasFiles && !isRoot ) {
            dest.add( folder );
        }
        
    }

    /** Find all files in given folder 
     */
    private void storeFiles( Collection<File> dest, File folder ) {

        File files[] = folder.listFiles();

        for ( File f : files) {
            if ( f.isDirectory() ) {
                storeFiles( dest, f );
            }
            else {
                dest.add( f );
            }
        }                        
    }

    
}