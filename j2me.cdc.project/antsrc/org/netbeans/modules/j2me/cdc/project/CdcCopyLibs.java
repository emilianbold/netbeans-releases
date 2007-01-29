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

package org.netbeans.modules.j2me.cdc.project;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class CdcCopyLibs extends Jar {
    
    private static final String LIB = "lib";    //NOI18N
    
    Path runtimePath;
    
    /** Creates a new instance of CopyLibs */
    public CdcCopyLibs () {
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
        
        final File destFile = this.getDestFile();
        final File destFolder = destFile.getParentFile();
        assert destFolder != null && destFolder.canWrite();
        
        if (filesToCopy != null && filesToCopy.length>0) {            
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
