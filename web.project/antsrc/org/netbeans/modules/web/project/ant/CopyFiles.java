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

package org.netbeans.modules.web.project.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that copies multiple files specified by one property (separated by ';').
 * It merely delegates to copy task for every file in the files attribute.
 * The files attribute is parsed as a Path structure.
 *
 * This is a workaround for http://nagoya.apache.org/bugzilla/show_bug.cgi?id=18128.
 *
 * @author Pavel Buzek
 */
public class CopyFiles extends Task {
    
    private File todir;
    private String files;

    public void execute() throws BuildException {
            
            PathTokenizer tokenizer = new PathTokenizer (getFiles ());
            while (tokenizer.hasMoreTokens ()) {
                File f = getProject().resolveFile(tokenizer.nextToken());
                Copy cp = (Copy) getProject ().createTask ("copy");
                cp.setTodir (getToDir ());
                if (f.isDirectory ()) {
                    FileSet fset = new FileSet ();
                    fset.setDir (f);
                    cp.addFileset (fset);
                } else {
                    cp.setFile (f);
                }
                cp.execute ();
            }
    }

    public String getFiles() {
        return this.files;
    }
    
    public void setFiles (String files) {
        this.files = files;
    }
    
    public File getToDir() {
        return this.todir;
    }
    
    public void setToDir (File todir) {
        this.todir = todir;
    }
}
