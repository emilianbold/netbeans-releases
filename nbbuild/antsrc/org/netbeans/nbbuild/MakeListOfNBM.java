/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
 * Create an update tracking file automatically.
 * Requires a build script target containing one (and only one)
 * occurrence of the <code>&lt;makenbm&gt;</code> task.
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
    public void setOutputfiledir(File s) {
        outputFile = s;
    }


    public void execute () throws BuildException {
        if ( targetName == null ) new BuildException( "You have to specify target used to make NBMs" );
        if ( outputFile == null ) new BuildException( "You have to specify output directoty" );
        log ("Generating information for Auto Update...");
        
/*        if (!outputFile.exists()) {
            outputFile.mkdirs();
        }
*/        
        Task nbms[] = ((Target) this.getProject().getTargets().get(targetName)).getTasks();
        UpdateTracking track = new UpdateTracking( outputFile.getAbsolutePath() );
        for( int i=0; i < nbms.length; i++) {
            if (nbms[i].getClass().getName().endsWith("MakeNBM")) {
                
                nbms[i].maybeConfigure();
                
                FileSet fs;
                try {
                    Method m = nbms[i].getClass().getDeclaredMethod("getFileSet",null);
                    m.setAccessible(true);
                    fs = (FileSet)m.invoke(nbms[i],null);
                } catch (Exception ex) {
                    throw new BuildException( "Can't get fileset of NBM", ex, location );
                }
                Attributes attr;
                try {
                    Method m = nbms[i].getClass().getDeclaredMethod("getAttributes",null);
                    m.setAccessible(true);
                    attr = (Attributes)m.invoke(nbms[i],null);
                } catch (Exception ex) {
                    throw new BuildException( "Can't get manifest attributes", ex, location );
                }
                
		String codename = attr.getValue ("OpenIDE-Module");
		if (codename == null)
                    throw new BuildException ("invalid manifest, does not contain OpenIDE-Module", location);
                
		String versionSpecNum = attr.getValue ("OpenIDE-Module-Specification-Version");
		if (versionSpecNum == null) {
                    log ("manifest does not contain OpenIDE-Module-Specification-Version");
                    return;
                }
                
                UpdateTracking.Version version = track.addNewModuleVersion( codename, versionSpecNum );
                
                DirectoryScanner ds = fs.getDirectoryScanner( this.getProject() );
                ds.scan();
 
//                log ("Module: " + codenamebase);
//                log ("Specification Version: " + versionSpecNum);
                
                String include[] = ds.getIncludedFiles();
                for( int j=0; j < include.length; j++ ){
                    if (include[j].equals("Info/info.xml") || 
			include[j].startsWith("main/") || 
			include[j].startsWith("netbeans/update_tracking/")) 
			    continue;

                    try {
                        File inFile = new File( ds.getBasedir(), include[j] );
                        FileInputStream inFileStream = new FileInputStream( inFile );
                        byte array[] = new byte[ (int) inFile.length() ];
                        CRC32 crc = new CRC32();
                        inFileStream.read( array );
			inFileStream.close();
                        crc.update( array );
                        String abs = inFile.getAbsolutePath();
                        String prefix = ds.getBasedir().getAbsolutePath() + File.separatorChar + "netbeans" + File.separatorChar;
                        if (! abs.startsWith(prefix)) throw new IllegalStateException(abs);
                        version.addFileWithCrc(abs.substring(prefix.length()), Long.toString( crc.getValue() ) );
//                        log( "File : " + inFile.getAbsolutePath().substring((ds.getBasedir().getAbsolutePath() + "/netbeans/").length() ) + " has CRC " + crc.getValue() );
                    } catch (IOException ex) {
                        log ( ex.toString() );
                    }
                }
		track.write();		    
            }
        }
    }
}
