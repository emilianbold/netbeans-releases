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
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.project.CommandlineXsltProjectXmlCatalogProvider;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.modules.soa.validation.util.LineUtil;

public class IdeValidateProjectTask extends Task {

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

    public void setClasspathRef(Reference ref) {}

    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }

    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
        this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }

    public boolean isFoundErrors() {
        return this.isFoundErrors;
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
            CommandlineXsltProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);
        } catch (Exception ex) {
            throw new BuildException("Failed to get File object for project source directory " + this.mSourceDirectory, ex);
        }
        
        try {
            this.mBuildDir = new File(this.mBuildDirectory);
        } catch (Exception ex) {
            throw new BuildException("Failed to get File object for project build directory " + this.mBuildDirectory, ex);
        }
        processFilesFolderInBuildDir(this.mBuildDir);
        processSourceDirs(Arrays.asList(this.mSourceDir));
    }

    private void processFilesFolderInBuildDir(File folder) {
        File files[] = folder.listFiles(new Util.XsltFileFilter());

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isFile()) {
                processFilesInBuildDir(file);
            } else {
                processFilesFolderInBuildDir(file);
            }
        }
    }

    private void processFilesInBuildDir(File file) {
        String relativePath = Util.getRelativePath(this.mBuildDir, file);
        this.myFileNamesToFileInBuildDir.put(relativePath, file);
    }

    private void processSourceDirs(List sourceDirs) {
        Iterator iterator = sourceDirs.iterator();

        while (iterator.hasNext()) {
            File sourceDir = (File) iterator.next();
            processSourceDir(sourceDir);
        }
    }

    private void processSourceDir(File sourceDir) {
        processFileObject(sourceDir);
    }

    private void processFileObject(File file) {
        if (file.isDirectory()) {
            processFolder(file);
        }
    }

    private void processFolder(File fileDir) {
        File[] files = fileDir.listFiles(new Util.XsltFileFilter());
        processFiles(files);
    }

    private void processFiles(File[] files) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                processFile(files[i]);
            } else {
                processFolder(files[i]);
            }
        }
    }

    private void processFile(File file) throws BuildException {
//System.out.println();
//System.out.println("see: " + file);
//System.out.println();
        if (isFileModified(file)) {
            try {
                validateFile(file);
            }
            catch (Throwable ex) {
                if ( !myAllowBuildWithError) {
                    throw new BuildException(ex);
                }
            }
        }
    }

    private boolean isFileModified(File file) {
        boolean modified = true;
        String relativePath = Util.getRelativePath(this.mSourceDir, file);
        File mFileInBuildDir = (File) this.myFileNamesToFileInBuildDir.get(relativePath);

        if (mFileInBuildDir != null) {
            if (mFileInBuildDir.lastModified() == file.lastModified()) {
                modified = false;
            }
        }
        return modified;
    }

    private void validateFile(File file) throws BuildException {
//System.out.println();
//System.out.println("validate: " + file);
//System.out.println();
      Model model = null;
      
      try {
        model = IDETMapCatalogModel.getDefault().getTMapModel(file);
      }
      catch (Exception e) {
        model = null;
      }
      validateModel(file, model);

      try {
        model = IdeXslCatalogModel.getDefault().getXslModel(file);
      }
      catch (Exception e) {
        model = null;
      }
      validateModel(file, model);
    }

    private void validateModel(File file, Model model) throws BuildException {
//System.out.println("   model: " + file);
      if (model == null) {
        return;
      }
      if (new Controller(model).ideValidate(file)) {
        throw new BuildException(LineUtil.FOUND_VALIDATION_ERRORS);
      }
    }

    private String mSourceDirectory;
    private String mProjectClassPath;
    private String mBuildDirectory;
    private String mBuildDependentProjectFilesDirectory;
    private File mSourceDir;
    private File mBuildDir;
    private Map myFileNamesToFileInBuildDir = new HashMap();
    private boolean isFoundErrors = false;
    private boolean myAllowBuildWithError = false;
}
