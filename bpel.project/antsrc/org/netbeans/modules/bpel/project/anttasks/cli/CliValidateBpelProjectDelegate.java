/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.project.anttasks.cli;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.bpel.project.anttasks.util.Util;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.soa.validation.core.Controller;

public class CliValidateBpelProjectDelegate extends Task {
    
    private String mSourceDirectory;
    private String mProjectClassPath;
    private String mBuildDirectory;
    private String mBuildDependentProjectFilesDirectory;
    private File mSourceDir;
    private File mBuildDir;
    private Map myFileNamesToFileInBuildDir = new HashMap();
    private boolean myIsFoundErrors = false;
    private boolean myAllowBuildWithError = false;
    
    public CliValidateBpelProjectDelegate() {}
    
    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }
    
    public void setBuildDirectory(String buildDir) {
        this.mBuildDirectory = buildDir;
    }
    
    public void setRunValidation(String flag) {
        setAllowBuildWithError(flag);
        myAllowBuildWithError = !myAllowBuildWithError;
    }
    
    public void setAllowBuildWithError(String flag) {
        if (flag != null) {
            if (flag.equals("false")) {
                myAllowBuildWithError = false;
            } else if (flag.equals("true")) {
                myAllowBuildWithError = true;
            }
        }
    }
    
    public void setClasspathRef(Reference ref) {
    }
    
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
    
    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
        this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }
    
    public boolean isFoundErrors() {
        return myIsFoundErrors;
    }
    
    @Override
    public void execute() throws BuildException {
        if (this.mSourceDirectory == null) {
            throw new BuildException("No directory is set for source files.");
        }

        if (this.mBuildDirectory == null) {
            throw new BuildException("No build directory is set.");
        }

        if (this.mBuildDependentProjectFilesDirectory == null) {
            throw new BuildException("No dependentProjectFiles directory is set.");
        }

        try {
            this.mSourceDir = new File(this.mSourceDirectory);
            CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);

        } catch (Exception ex) {
            throw new BuildException("Failed to get File object for project source directory " + this.mSourceDirectory, ex);
        }

        try {
            this.mBuildDir = new File(this.mBuildDirectory);
        } catch (Exception ex) {
            throw new BuildException("Failed to get File object for project build directory " + this.mBuildDirectory, ex);
        }

        processBuildDir(this.mBuildDir);
        processSourceDir(this.mSourceDir);
    }
    
    private void processBuildDir(File folder) {
        final File files[] = folder.listFiles(new Util.BpelFileFilter());
        
        if (files == null) return;
        
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            
            if (file.isFile()) {
                this.myFileNamesToFileInBuildDir.put(Util.getRelativePath(this.mBuildDir, file), file);
            } else {
                processBuildDir(file);
            }
        }
    }
    
    private void validateFile(File file) throws BuildException {
      try {
        Model model = CliBpelCatalogModel.getDefault().getBPELModel(file.toURI());

        if (new Controller(model).cliValidate(file)) {
          myIsFoundErrors = true;
        }
      }
      catch (Exception e) {
        throw new BuildException(e);
      }
    }

    private boolean isModified(File file) {
        boolean modified = true;
        String relativePath = Util.getRelativePath(this.mSourceDir, file);
        File fileInBuildDir = (File) this.myFileNamesToFileInBuildDir.get(relativePath);

        if (fileInBuildDir != null) {
            if (fileInBuildDir.lastModified() == file.lastModified()) {
                modified = false;
            }
        }
        return modified;
    }

    private void processSourceDir(File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles(new Util.BpelFileFilter());
            
            if (children == null) return;
            
            for (int i = 0; i < children.length; i++) {
                processSourceDir(children[i]);
            }
        } else {
            if (isModified(file)) {
                validateFile(file);
            }
        }
    }
}
