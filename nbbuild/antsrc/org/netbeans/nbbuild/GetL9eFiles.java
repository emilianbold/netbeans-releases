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

package org.netbeans.nbbuild;

import org.apache.tools.ant.*;
import org.apache.tools.ant.util.*;
import org.apache.tools.ant.taskdefs.*;
import java.io.*;
import java.util.*;

/** This task copies the localizable files to a directory.
 * This task uses the L10nTask's getLocalizableFiles() function
 * to get the list of localizable files for each module.
 */
public class GetL9eFiles extends Task {
    
    /** The name of file that contains the localizable file
     * regular expressions.
     * <p>Default: <samp>l10n.list</samp>
     */
    protected String listFile = "l10n.list";
    
    /** The grandparent directory of the l10n.list files.
     * <p>Default: <samp>..</samp>
     */
    protected String baseDir = "..";
    protected File grandParent = null ;
    
    /** The target directory to copy all translatable files to.
     * <p>Default: <samp>src-todo</samp>
     */
    protected File targetDir = null ;
    
    /** List of exclude patterns that override the listFiles.
     * <p>Default: Ja localized files
     */
    protected String excludes = "**/ja/,**/*_ja.*" ;
    
    /** Used internally. */
    protected FileUtils fileUtils = null ;
    
    public void setBaseDir(String s) {
        File f ;
        
        baseDir = s;
        f = new File( antBaseDir() + baseDir) ;
        try {
            grandParent = new File( f.getCanonicalPath()) ;
        } catch( Exception e) {
            e.printStackTrace();
            throw new BuildException() ;
        }
    }
    public void setListFile( String s) {
        listFile = s ;
    }
    public void setTargetDir( File f) {
        targetDir = f ;
    }
    public void setExcludes( String s) {
        excludes = s ;
    }
    
    /** A file filter that accepts only directories
     */
    class DirectoryFilter implements FileFilter {
        public boolean accept(File f) {
            return( f.isDirectory()) ;
        }
    }
    
    class TarFileFilter implements FileFilter {
        public boolean accept(File f) {
            return( f.getName().endsWith( ".tar")) ;
        }
    }
    
    public void execute() throws BuildException {
        if( targetDir == null) {
            targetDir = new File( antBaseDir() + "src-todo") ;
        }
        
        // If needed, setup the grandParent variable. //
        if( grandParent == null) {
            setBaseDir( baseDir) ;
        }
        
        // For each module with a list file. //
        for (File module : getModulesWithListFiles()) {
            // Copy the module's localizable files. //
            copyL9eFiles( module) ;
        }
    }
    
    protected void copyL9eFiles( File module) {
        String[] l9eFiles, changedFiles ;
        int i ;
        File fromFile, toFile ;
        L10nTask l10nTask ;
        
        // Setup the file utils. //
        if( fileUtils == null) {
            fileUtils = FileUtils.getFileUtils() ;
        }
        
        // Use the l10n task to read the list file and get a list of the //
        // localizable files.					     //
        getProject().addTaskDefinition("l10nTask", L10nTask.class);
        l10nTask = (L10nTask) getProject().createTask("l10nTask");
        l10nTask.init() ;
        l10nTask.setLocalizableFile( listFile) ;
        l10nTask.setExcludePattern( excludes) ;
        l9eFiles = l10nTask.getLocalizableFiles( grandParent, module.getName()) ;
        if( l9eFiles != null) {
            
            // Get a list of the files that have changed. //
            changedFiles = getChangedFiles( l9eFiles) ;
            
            // Copy the localizable files that have changed. //
            if( changedFiles != null && changedFiles.length > 0) {
                log( "Copying " + changedFiles.length + " files to " + targetDir.getPath()) ;
                for( i = 0; i < changedFiles.length; i++) {
                    fromFile = new File( changedFiles[i]) ;
                    toFile = new File( mapL9eFile( changedFiles[i],
                            targetDir.getPath(),
                            grandParent.getPath())) ;
                    try {
                        //log("Copying " + fromFile.getPath() + " to " + toFile.getPath()) ;
                        fileUtils.copyFile( fromFile, toFile) ;
                    } catch (IOException ioe) {
                        String msg = "Failed to copy " + fromFile.getPath() + " to " +
                                toFile.getPath() + " due to " + ioe.getMessage();
                        throw new BuildException(msg, ioe, getLocation());
                    }
                }
            }
        }
    }
    
    protected String[] getChangedFiles( String[] files) {
        L9eMapper mapper = new L9eMapper() ;
        mapper.setFrom( grandParent.getPath()) ;
        mapper.setTo( targetDir.getPath()) ;
        
        SourceFileScanner ds = new SourceFileScanner( this);
        return( ds.restrict( files, null, null, mapper)) ;
    }
    
    protected class L9eMapper implements FileNameMapper {
        
        protected String m_grandParent ;
        protected String m_toDir ;
        
        public void setFrom(String from) {
            m_grandParent = from ;
        }
        
        public void setTo(String to) {
            m_toDir = to ;
        }
        
        // Returns an one-element array containing the destination file //
        // name.							    //
        public String[] mapFileName(String file) {
            return new String[] { GetL9eFiles.mapL9eFile( file, m_toDir, m_grandParent) } ;
        }
    }
    
    protected static String mapL9eFile( String file, String toDir,
            String grandParentName) {
        return toDir + file.substring( grandParentName.length()) ;
    }
    
    protected List<File> getModulesWithListFiles() {
        List<File> modules = new LinkedList<File>();
        for (File module : grandParent.listFiles(new DirectoryFilter())) {
            File list = new File(module.getPath() + File.separator + listFile);
            if (list.exists()) {
                modules.add( module) ;
            }
        }
        return modules;
    }
    
    protected String antBaseDir() {
        return( getProject().getBaseDir().getAbsolutePath() + File.separator) ;
    }
    
}
