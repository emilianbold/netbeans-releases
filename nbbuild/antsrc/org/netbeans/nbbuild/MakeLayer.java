/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.UpToDate;

/** Create a fragment of a module's XML layer.
 *
 * @author  Michal Zlamal
 */
public class MakeLayer extends MatchingTask {

    private File dest = null;
    private File topdir = null;
    private boolean absolutePath = false;

    /** Target file containing list of all classes. */
    public void setDestfile(File f) {
        dest = f;
    }

    /** Set the top directory.
     * There should be subdirectories under this matching pacgages.
     */
    public void setTopdir (File t) {
        topdir = t;
    }
    
    /** Set wheather there is absolute path in top dir or not
     * default value is false
     */
    public void setAbsolutePath( boolean absolutePath ) {
        this.absolutePath = absolutePath;
    }

    
    public void execute()  throws BuildException {
        if (topdir == null) {
            throw new BuildException("You must set at topdir attribute", getLocation());
        }
        if (dest == null) {
            throw new BuildException("You must specify output file", getLocation());
        }
        UpToDate upToDate = (UpToDate) this.getProject().createTask( "uptodate" );
        fileset.setDir( topdir );
        upToDate.addSrcfiles( fileset );
        upToDate.setTargetFile( dest );
        upToDate.setProperty(dest.getAbsolutePath() + ".property");
        upToDate.execute();
        if (this.getProject().getProperty(dest.getAbsolutePath() + ".property") != null)
            return;
        
        int lengthAdjust = (absolutePath) ? 0 : 1;
        FileWriter layerFile;
        try {
            layerFile = new FileWriter(dest);
        }
        catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
        
        FileScanner scanner = getDirectoryScanner (topdir);
        String[] files = scanner.getIncludedFiles ();
        for (int i=0; i <files.length; i++) {
            File aFileName = new File(topdir, files[i]);
            try {
                layerFile.write(("<file name=\""+aFileName.getName()+"\"\n").replace(File.separatorChar,'/'));
                layerFile.write(("  url=\""+aFileName.getAbsolutePath().substring(topdir.getAbsolutePath().length()+lengthAdjust)+"\"/>\n").replace(File.separatorChar,'/'));
            }
            catch(IOException ex) {
                throw new BuildException(ex, getLocation());
            }
        }
        
        try {
            layerFile.close();
        }
        catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
}



