/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2me.cdc.project;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 */
public class FatCopyLibs  {
       
    private Path runtimePath;
    private File destFolder;
    
    /** Creates a new instance of CopyLibs */
    public FatCopyLibs () {
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
            filesToCopy[i] = f;
        }        
        
        assert getDestFolder() != null && getDestFolder().canWrite();
            
        if (filesToCopy != null && filesToCopy.length>0) {            
            FileUtils utils = FileUtils.newFileUtils();
            for (File f : filesToCopy ) {
                try {
                    File libFile = new File (getDestFolder(), f.getName());
                    utils.copyFile(f,libFile);
                } catch (IOException ioe) {
                    throw new BuildException (ioe);
                }
            }
        }        
    }    

    public File getDestFolder() {
        return destFolder;
    }

    public void setDestFolder(File destFolder) {
        this.destFolder = destFolder;
    }
}
