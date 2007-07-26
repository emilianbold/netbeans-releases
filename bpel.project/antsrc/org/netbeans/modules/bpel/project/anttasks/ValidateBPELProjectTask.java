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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
//System.out.println("=========");
            
            driver = antTaskClass.getMethod("execute", null);
            driver.invoke(validateBPELObj, null);                    
//System.out.println("8888");
            
            driver = antTaskClass.getMethod("isFoundErrors", null);
            isErrors = (Boolean) driver.invoke(validateBPELObj, null);
//System.out.println("isErrors: " + isErrors);
         }
         catch (Throwable e) {
//e.printStackTrace();
                 throw new BuildException("Found error: " + e.getMessage());
         }
         if (isErrors != null && isErrors.booleanValue()) {
             if ( !mAllowBuildWithError) {
                 throw new BuildException("Found validation error(s).");
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
