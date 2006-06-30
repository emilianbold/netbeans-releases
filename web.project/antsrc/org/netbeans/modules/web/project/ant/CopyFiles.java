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
