/*
 * MakeListOfNBM.java
 *
 * Created on July 31, 2001, 10:19 AM
 */

package org.netbeans.nbbuild;

// IMPORTANT! You may need to mount ant.jar before this class will
// compile. So mount the JAR modules/ext/ant.jar (NOT modules/ant.jar)
// from your IDE installation directory in your Filesystems before
// continuing to ensure that it is in your classpath.

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.zip.CRC32;
import java.lang.reflect.Method;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author  Michal Zlamal
 * @version 
 */
public class MakeListOfNBM extends Task {
    String targetName = null;
    String outputFile = null;
    
    /** Sets target which contains the <makenbm> tasks */
    public void setTargetname(String s) {
        targetName = s;
    }
    
    /** Sets the directory used to create the NBM list file */
    public void setOutputfileDir(String s) {
        outputFile = s;
    }


    public void execute () throws BuildException {
        if ( targetName == "null" ) new BuildException( "You have to specify target used to make NBMs" );
        if ( outputFile == "null" ) new BuildException( "You have to specify output directoty" );
        log ("Generating information about the NBMs");
        
        Task nbms[] = ((Target) this.getProject().getTargets().get(targetName)).getTasks();
        UpdateTracking track = new UpdateTracking( outputFile );
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
		if (versionSpecNum == null)
                    throw new BuildException ("invalid manifest, does not contain OpenIDE-Module-Specification-Version", location);
                
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
