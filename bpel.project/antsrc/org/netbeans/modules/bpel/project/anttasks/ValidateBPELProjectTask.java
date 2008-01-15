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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Ant task wrapper invokes Validate BPEL Model
 * @author Sreenivasan Genipudi
 */
public class ValidateBPELProjectTask extends Task {
    public ValidateBPELProjectTask() {}
    
    private static final String BPEL_EXT = ".bpel";
    private static final String WSDL_EXT = ".wsdl";
    private static final String XSD_EXT = ".xsd";

    private String mSourceDirectory = null;
    private String mBuildDirectory = null;    
    private String mProjectClassPath= null;
    private List mDependentProjectDirs;
    private AntClassLoader m_myClassLoader = null;
    private Reference m_ref = null;
    private String mBuildDependentProjectFilesDirectory; 
    private boolean mbRunValidation = true;
    private boolean mAllowBuildWithError = false;
    
    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
        this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }

    public void setClasspathRef(Reference ref) {
        this.m_ref = ref;
    }
    
    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }

    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }

    public void setRunValidation(String flag) {
        setAllowBuildWithError(flag);
        mAllowBuildWithError = !mAllowBuildWithError;
    }

    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
           
    public void setAllowBuildWithError(String flag) {
        if (flag != null) {
            if (flag.equals("false")) {
                mAllowBuildWithError = false;
            } else if (flag.equals("true")) {
                mAllowBuildWithError = true;
            }
        }
    }  
    
    public void execute() throws BuildException {
        Boolean isErrors = null;

        try {
//System.out.println("1111");
            m_myClassLoader = new AntClassLoader(); 
            initClassLoader(); 
//System.out.println("2222");

            Class antTaskClass =  Class.forName("org.netbeans.modules.bpel.project.anttasks.ValidateBPELProject", true,m_myClassLoader );
            Thread.currentThread().setContextClassLoader(m_myClassLoader);
            Object validateBPELObj = antTaskClass.newInstance();
//System.out.println("3333");
            
            Method driver = antTaskClass.getMethod("setBuildDirectory", new Class[] { java.lang.String.class });
            Object[] param = new Object[] { this.mBuildDirectory };
            driver.invoke(validateBPELObj, param);
//System.out.println("4444");
                       
            driver = antTaskClass.getMethod("setSourceDirectory", new Class[] { java.lang.String.class });
            param = new Object[] { this.mSourceDirectory};
            driver.invoke(validateBPELObj, param);

            driver = antTaskClass.getMethod("setAllowBuildWithError", new Class[] { java.lang.String.class });
            param = new Object[] { "" + this.mAllowBuildWithError};
            driver.invoke(validateBPELObj, param);
//System.out.println("5555");
                      
            driver = antTaskClass.getMethod("setProjectClassPath", new Class[] { java.lang.String.class });
            param = new Object[] { this.mProjectClassPath };
            driver.invoke(validateBPELObj, param);
//System.out.println("6666");
                      
            driver = antTaskClass.getMethod("setBuildDependentProjectDir", new Class[] { java.lang.String.class });
            param = new Object[] { this.mBuildDependentProjectFilesDirectory};
            driver.invoke(validateBPELObj, param);
//System.out.println("7777");
            
            driver = antTaskClass.getMethod("execute", null);
            driver.invoke(validateBPELObj, null);                    
//System.out.println("8888");
            
            driver = antTaskClass.getMethod("isFoundErrors", null);
            isErrors = (Boolean) driver.invoke(validateBPELObj, null);
//System.out.println("isErrors: " + isErrors);
         }
         catch (Throwable e) {
           throw new BuildException("Exception occurs.");
         }
         if (isErrors != null && isErrors.booleanValue()) {
             if ( !mAllowBuildWithError) {
                 throw new BuildException(Util.FOUND_VALIDATION_ERRORS);
             }
         }
    }
    
    private void initClassLoader() {
        Path path = new Path(getProject());
        path.setRefid(m_ref);
        Path parentPath = new Path(getProject());
        ClassLoader cl = this.getClass().getClassLoader();

        if (cl instanceof AntClassLoader) {
            parentPath.setPath(((AntClassLoader)cl).getClasspath());
            ((AntClassLoader)cl).setParent(null);
            parentPath.add(path);
            path = parentPath;
        }        
        m_myClassLoader.setClassPath(path);
        m_myClassLoader.setParent(null);
        m_myClassLoader.setParentFirst(false);
    }        
}
