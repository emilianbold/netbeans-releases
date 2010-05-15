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
package org.netbeans.modules.bpel.project.anttasks.ide;

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
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.bpel.project.anttasks.util.Util;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.misc.Xml;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

public class IdeValidateBpelProjectTask extends Task {

    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }

    public void setBuildDirectory(String buildDir) {
        this.mBuildDirectory = buildDir;
    }

    public void setAllowBuildWithError(String flag) {
        if (flag == null) {
            return;
        }
        if (flag.equals("false")) {
            myAllowBuildWithError = false;
        }
        else if (flag.equals("true")) {
            myAllowBuildWithError = true;
        }
    }

    public void setValidation(String flag) {
        myValidationType = Util.getValidationType(flag);
    }

    public void setClasspathRef(Reference ref) {}

    public void setProjectClassPath(String projectClassPath) {}

    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {}

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
        
        myMyFiles = new ArrayList<MyFile>();
        processFilesFolderInBuildDir(this.mBuildDir);
        processSourceDirs(Arrays.asList(this.mSourceDir));
    }

    private void processFilesFolderInBuildDir(File folder) {
        File files[] = folder.listFiles(new Util.BpelFileFilter());

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
        File[] files = fileDir.listFiles(new Util.BpelFileFilter());
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

    // # 100036
    private void processFile(File file) throws BuildException {
        BpelModel model = null;

        try {
            model = IdeBpelCatalogModel.getDefault().getBPELModel(file);
        } catch (Exception e) {
            throw new RuntimeException("Error while trying to get Model", e);
        }
        Process process = model.getProcess();

        if (process != null) {
            String qName = process.getName() + ", " + process.getTargetNamespace(); // NOI18N
            MyFile current = new MyFile(file, mSourceDir, qName);

            for (MyFile mFile : myMyFiles) {
                if (mFile.getQName().equals(qName)) {
                    if ( !myAllowBuildWithError) { // # 106342
                        throw new BuildException(
                                " \n" +
                                "BPEL files " + mFile.getName() + " and " + current.getName() + "\n" +
                                "have the same bpel process name and targetname space:\n" +
                                qName + " \n \n");
                    }
                }
            }
            myMyFiles.add(current);
        }
        if (isModified(file)) {
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

    private void validateFile(File file) throws BuildException {
      try {
        Model model = IdeBpelCatalogModel.getDefault().getBPELModel(file);

        if (new Controller(model).ideValidate(file, myValidationType)) {
          throw new BuildException(Xml.FOUND_VALIDATION_ERRORS);
        }
      }
      catch (Exception e) {
        throw new BuildException(e);
      }
    }

    // --------------------------
    private static class MyFile {

        public MyFile(File file, File project, String qName) {
            myFile = file;
            myProject = project;
            myQName = qName;
        }

        public String getQName() {
            return myQName;
        }

        public String getName() {
            String file = myFile.toString();
            String path = myProject.toString();

            if (file.startsWith(path)) {
                return file.substring(path.length() + 1);
            }
            return file;
        }

        private File myFile;
        private File myProject;
        private String myQName;
    }

    private String mSourceDirectory;
    private String mBuildDirectory;
    private File mSourceDir;
    private File mBuildDir;
    private List<MyFile> myMyFiles;
    private Map myFileNamesToFileInBuildDir = new HashMap();
    private boolean isFoundErrors = false;
    private boolean myAllowBuildWithError = false;
    private ValidationType myValidationType = ValidationType.COMPLETE;
}
