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

package org.netbeans.modules.java.j2seproject.copylibstask;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class CopyLibs extends Jar {
    
    private static final String LIB = "lib";    //NOI18N
    
    Path runtimePath;
    
    /** Creates a new instance of CopyLibs */
    public CopyLibs () {
    }
    
    public void setRuntimeClassPath (final Path path) {
        assert path != null;
        this.runtimePath = path;
    }
    
    public Path getRuntimeClassPath () {
        return this.runtimePath;
    }
    
    public void execute() throws BuildException {
        if (this.runtimePath == null) {
            throw new BuildException ("RuntimeClassPath must be set.");
        }
        final String[] pathElements = this.runtimePath.list();
        File[] filesToCopy = new File[pathElements.length];
        for (int i=0; i< pathElements.length; i++) {
            File f = new File (pathElements[i]);
            if (f.isDirectory() || !f.canRead()) {
                filesToCopy = null;
                break;
            }
            else {
                filesToCopy[i] = f;
            }
        }        
        super.execute();
        
        if (filesToCopy != null) {
            final File destFile = this.getDestFile();
            final File destFolder = destFile.getParentFile();
            assert destFolder != null && destFolder.canWrite();
            final File libFolder = new File (destFolder,LIB);
            if (!libFolder.exists()) {
                libFolder.mkdir ();
            }
            assert libFolder.canWrite();            
            FileUtils utils = FileUtils.newFileUtils();
            for (int i=0; i<filesToCopy.length; i++) {
                try {
                    File libFile = new File (libFolder,filesToCopy[i].getName());
                    utils.copyFile(filesToCopy[i],libFile);
                } catch (IOException ioe) {
                    throw new BuildException (ioe);
                }
            }
        }
        
    }
    
}
