/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.j2seproject.copylibstask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
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
                this.log(f.toString() + " is a directory or can't be read. Not copying the libraries.");
                break;
            }
            else {
                filesToCopy[i] = f;
            }
        }        
        final File destFile = this.getDestFile();
        final File destFolder = destFile.getParentFile();
        assert destFolder != null && destFolder.canWrite();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.java.j2seproject.copylibstask.Bundle");  //NOI18N
            assert bundle != null;            
            final File readme = new File (destFolder,bundle.getString("TXT_README_FILE_NAME"));
            if (!readme.exists()) {
                readme.createNewFile();
            }
            final PrintWriter out = new PrintWriter (new FileWriter (readme));            
            try {
                final String content = bundle.getString("TXT_README_FILE_CONTENT");                
                out.println (MessageFormat.format(content,new Object[] {destFile.getName()}));
            } finally {
                out.close ();
            }
        } catch (IOException ioe) {
            this.log("Cannot generate readme file.",Project.MSG_VERBOSE);
        }        
        
        if (filesToCopy != null && filesToCopy.length>0) {            
            final File libFolder = new File (destFolder,LIB);
            if (!libFolder.exists()) {
                libFolder.mkdir ();
                this.log("Create lib folder " + libFolder.toString() + ".", Project.MSG_VERBOSE);
            }
            assert libFolder.canWrite();            
            FileUtils utils = FileUtils.getFileUtils();
            this.log("Copy libraries to " + libFolder.toString() + ".");
            for (int i=0; i<filesToCopy.length; i++) {
                this.log("Copy " + filesToCopy[i].getName() + " to " + libFolder + ".", Project.MSG_VERBOSE);
                try {
                    File libFile = new File (libFolder,filesToCopy[i].getName());
                    utils.copyFile(filesToCopy[i],libFile);
                } catch (IOException ioe) {
                    throw new BuildException (ioe);
                }
            }
            final FileSet fs = new FileSet();
            fs.setDir(libFolder);
            final Path p = new Path(getProject());
            p.addFileset(fs);
            addConfiguredIndexJars(p);
        }
        else {
            this.log("Not copying the libraries.");
        }

        super.execute();
    }
}
