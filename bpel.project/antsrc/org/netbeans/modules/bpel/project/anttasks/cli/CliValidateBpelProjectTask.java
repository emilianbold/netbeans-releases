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
import java.lang.reflect.Method;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.netbeans.modules.bpel.project.anttasks.util.Util;
import org.netbeans.modules.xml.misc.Xml;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.bpel.project.anttasks.util.Util;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.bpel.project.anttasks.util.Util;

public class CliValidateBpelProjectTask extends Task {

    public void setClasspathRef(Reference reference) {
        myReference = reference;
    }
    
    public void setBuildDirectory(String buildDirectory) {
        myBuildDirectory = buildDirectory;
    }
    
    public void setSourceDirectory(String sourceDirectory) {
        mySourceDirectory = sourceDirectory;
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
//System.out.println("2 type: " + myValidationType);
    }

    @Override
    public void execute() throws BuildException {
        Boolean isErrors = null;
        
        try {
            myClassLoader = new AntClassLoader();
            initClassLoader();
            
            Class antTaskClass = Class.forName("org.netbeans.modules.bpel.project.anttasks.cli.CliValidateBpelProjectTask", true, myClassLoader);
            Thread.currentThread().setContextClassLoader(myClassLoader);
            Object validateObj = antTaskClass.newInstance();
            
            Method driver = antTaskClass.getMethod("setBuildDirectory", new Class[] {String.class});
            Object[] param = new Object[] {myBuildDirectory};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setSourceDirectory", new Class[] {String.class});
            param = new Object[] {mySourceDirectory};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setAllowBuildWithError", new Class[] {String.class});
            param = new Object[] {"" + myAllowBuildWithError};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setValidation", new Class[] {String.class});
            String type = "complete";

            if (myValidationType == null) {
                type = "none";
            }
            else if (myValidationType == ValidationType.PARTIAL) {
                type = "partial";
            }
            param = new Object[] { type };
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("validate", (Class[]) null);
            driver.invoke(validateObj, (Object[]) null);

            driver = antTaskClass.getMethod("isFoundErrors", (Class[]) null);
            isErrors = (Boolean) driver.invoke(validateObj, (Object[]) null);
        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new BuildException("Exception occured.", e);
        }
        if (isErrors != null && isErrors.booleanValue()) {
            if ( !myAllowBuildWithError) {
                throw new BuildException(Xml.FOUND_VALIDATION_ERRORS);
            }
        }
    }
    
    private void initClassLoader() {
        Path path = new Path(getProject());
        path.setRefid(myReference);
        
        Path parentPath = new Path(getProject());
        ClassLoader locader = getClass().getClassLoader();
        
        if (locader instanceof AntClassLoader) {
            parentPath.setPath(((AntClassLoader) locader).getClasspath());
            ((AntClassLoader) locader).setParent(null);
            parentPath.add(path);
            path = parentPath;
        }
        myClassLoader.setClassPath(path);
        myClassLoader.setParent(null);
        myClassLoader.setParentFirst(false);
    }

    public boolean isFoundErrors() {
        return myIsFoundErrors;
    }

    public void validate() throws BuildException {
        if (mySourceDirectory == null) {
            throw new BuildException("No directory is set for source files.");
        }
        if (myBuildDirectory == null) {
            throw new BuildException("No build directory is set.");
        }
        try {
            mySourceDir = new File(mySourceDirectory);
            CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(mySourceDirectory);
        } catch (Exception ex) {
            throw new BuildException("Failed to get File object for project source directory " + mySourceDirectory, ex);
        }
        try {
            myBuildDir = new File(myBuildDirectory);
        } catch (Exception ex) {
            throw new BuildException("Failed to get File object for project build directory " + myBuildDirectory, ex);
        }
        processBuildDir(myBuildDir);
        processSourceDir(mySourceDir);
    }
    
    private void processBuildDir(File folder) {
        final File files[] = folder.listFiles(new Util.BpelFileFilter());
        
        if (files == null) return;
        
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            
            if (file.isFile()) {
                myFileNamesToFileInBuildDir.put(Util.getRelativePath(myBuildDir, file), file);
            } else {
                processBuildDir(file);
            }
        }
    }
    
    private void validateFile(File file) throws BuildException {
      try {
        Model model = CliBpelCatalogModel.getDefault().getBPELModel(file.toURI());

        if (new Controller(model).cliValidate(file, myValidationType)) {
          myIsFoundErrors = true;
        }
      }
      catch (Exception e) {
        throw new BuildException(e);
      }
    }

    private boolean isModified(File file) {
        boolean modified = true;
        String relativePath = Util.getRelativePath(mySourceDir, file);
        File fileInBuildDir = (File) myFileNamesToFileInBuildDir.get(relativePath);

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

    private File myBuildDir;
    private File mySourceDir;
    private AntClassLoader myClassLoader;
    private Reference myReference;
    private boolean myIsFoundErrors;
    private boolean myAllowBuildWithError;
    private String mySourceDirectory;
    private String myBuildDirectory;
    private Map myFileNamesToFileInBuildDir = new HashMap();
    private ValidationType myValidationType = ValidationType.COMPLETE;
}
