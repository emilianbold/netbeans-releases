/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.zip.CRC32;
import java.lang.reflect.Method;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author  Michal Zlamal
 */
public class MakeListOfNBM extends Task {
    String targetName = null;
    File outputFile = null;
    
    /** Sets target which contains the <makenbm> tasks */
    public void setTargetname(String s) {
        targetName = s;
    }
    
    /** Sets the directory used to create the NBM list file */
    public void setOutputfileDir(File s) {
        outputFile = s;
    }


    public void execute () throws BuildException {
        if ( targetName == null ) new BuildException( "You have to specify target used to make NBMs" );
        if ( outputFile == null ) new BuildException( "You have to specify output directoty" );
        log ("Generating information for Auto Update...");
        
        if (!outputFile.exists()) {
            outputFile.mkdirs();
        }
        
        Task nbms[] = ((Target) this.getProject().getTargets().get(targetName)).getTasks();
        UpdateTracking track = new UpdateTracking( outputFile.getAbsolutePath() );
        for( int i=0; i < nbms.length; i++) {
            if (nbms[i].getClass().getName().endsWith("MakeNBM")) {
                
                nbms[i].maybeConfigure();
                
                FileSet fs = null;
                try {
                    fs = (FileSet)nbms[i].getClass().getMethod("getFileSet",null).invoke(nbms[i],null);
                } catch (Exception ex) {
                    throw new BuildException( "Can't get fileset of NBM", ex, location );
                }
                File manifest = null;
                try {
                    manifest = (File)nbms[i].getClass().getMethod("getManifest",null).invoke(nbms[i],null);
                } catch (Exception ex) {
                    throw new BuildException( "Can't get name of manifest file", ex, location );
                }
                
                Attributes attr = null;
        	try {
                    InputStream manifestStream = new FileInputStream (manifest);
                    try {
                        attr = new Manifest (manifestStream).getMainAttributes ();
                    } finally {
                        manifestStream.close ();
                    }
                } catch (IOException e) {
                    throw new BuildException ("exception when reading manifest " + manifest, e, location);
                }
                
		String codenamebase = attr.getValue ("OpenIDE-Module");
		if (codenamebase == null)
                    throw new BuildException ("invalid manifest, does not contain OpenIDE-Module", location);
                
		String versionSpecNum = attr.getValue ("OpenIDE-Module-Specification-Version");
		if (versionSpecNum == null) {
                    log ("manifest does not contain OpenIDE-Module-Specification-Version");
                    return;
                }
                
                UpdateTracking.Version version = track.addNewModuleVersion( codenamebase, versionSpecNum );
                
                DirectoryScanner ds = fs.getDirectoryScanner( this.getProject() );
                ds.scan();
 
//                log ("Module: " + codenamebase);
//                log ("Specification Version: " + versionSpecNum);
                
                String include[] = ds.getIncludedFiles();
                for( int j=0; j < include.length; j++ ){
                    try {
                        File inFile = new File( ds.getBasedir(), include[j] );
                        FileInputStream inFileStream = new FileInputStream( inFile );
                        byte array[] = new byte[ (int) inFile.length() ];
                        CRC32 crc = new CRC32();
                        inFileStream.read( array );
			inFileStream.close();
                        crc.update( array );

                        version.addFileWithCrc( inFile.getAbsolutePath().substring((ds.getBasedir().getAbsolutePath() + "/netbeans/").length() ), Long.toString( crc.getValue() ) );
//                        log( "File : " + inFile.getAbsolutePath().substring((ds.getBasedir().getAbsolutePath() + "/netbeans/").length() ) + " has CRC " + crc.getValue() );
                    } catch (IOException ex) {
                        log ( ex.toString() );
                    }
                }
            }
        }
        track.write();
    }
}
