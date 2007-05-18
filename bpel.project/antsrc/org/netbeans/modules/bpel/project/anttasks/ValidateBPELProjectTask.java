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
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Ant task wrapper invokes Validate BPEL Model
 * @author Sreenivasan Genipudi
 */
public class ValidateBPELProjectTask extends Task {
    /**
     * Constructor
     */
    public ValidateBPELProjectTask() {
    }
    
    /**
     * Constants
     */
    private static final String BPEL_EXT = ".bpel";
    private static final String WSDL_EXT = ".wsdl";
    private static final String XSD_EXT = ".xsd";
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(ValidateBPELProjectTask.class.getName());
    // Member variable representing source directory
    /**
     * Source directory
     */
    private String mSourceDirectory = null;
    // Member variable representing build directory
    /**
     * Build directory
     */
    private String mBuildDirectory = null;    
    // Member variable representing project classpath
    /**
     * Project classpath
     */
    private String mProjectClassPath= null;
    /**
     * Dependent project directories
     */
    private List mDependentProjectDirs;
    /**
     * Custom classloader to invoke Validate BPEL
     */
    private AntClassLoader m_myClassLoader = null;
    /**
     * classpath reference
     */
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
    /**
     * Set the source directory
     * @param srcDir source directory
     */
    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }
    /**
     * Run validation
     * @param srcDir source directory
     */
    public void setRunValidation(String flag) {
        setAllowBuildWithError(flag);
        mAllowBuildWithError = !mAllowBuildWithError;
    }
        
    /**
     * Get the source directory
     * @return String value of the source directory
     */
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    /**
     * Set the project classpath
     * @param projectClassPath
     */
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
           
    /**
     * Run validation
     * @param srcDir source directory
     */
    public void setAllowBuildWithError(String flag) {
        if (flag != null) {
            if (flag.equals("false")) {
                mAllowBuildWithError = false;
            } else if (flag.equals("true")) {
                mAllowBuildWithError = true;
            }
        }
        
    }  

    
    /**
     * Invoke validate BPEL Model
     */
    public void execute() throws BuildException { 
        try {
            m_myClassLoader = new AntClassLoader(); 
            initClassLoader(); 
            Class antTaskClass =  Class.forName("org.netbeans.modules.bpel.project.anttasks.ValidateBPELProject", true,m_myClassLoader );
            Thread.currentThread().setContextClassLoader(m_myClassLoader);
            
            Object validateBPELObj = antTaskClass.newInstance();
            
            
            Method driver = antTaskClass.getMethod("setBuildDirectory",
                           new Class[] { java.lang.String.class });
            Object[] param = new Object[] {
                           this.mBuildDirectory
                       };
            driver.invoke(validateBPELObj,
                       param);
                       
            driver = antTaskClass.getMethod("setSourceDirectory",
                          new Class[] { java.lang.String.class });
            param = new Object[] {
                          this.mSourceDirectory
                      };
            driver.invoke(validateBPELObj,
                      param);   
                      
            driver = antTaskClass.getMethod("setProjectClassPath",
                          new Class[] { java.lang.String.class });
            param = new Object[] {
                          this.mProjectClassPath
                      };
            driver.invoke(validateBPELObj,
                      param);   
                      
            driver = antTaskClass.getMethod("setBuildDependentProjectDir",
                          new Class[] { java.lang.String.class });
            param = new Object[] {
                          this.mBuildDependentProjectFilesDirectory
                      };
            driver.invoke(validateBPELObj,
                      param);                        
            
             driver = antTaskClass.getMethod("execute",
                            null);
            driver.invoke(validateBPELObj, null);                    
            
            driver = antTaskClass.getMethod("isFoundErrors",
                    null);
            
            Boolean isErrors = (Boolean) driver.invoke(validateBPELObj, null);
            if(isErrors.booleanValue()) {
            	throw new BuildException("Found validation errors.");
            }
            
        }catch (Throwable ex) {
         //   ex.printStackTrace();
            if (!mAllowBuildWithError ) {
                logger.log(Level.FINE, "Validation has errors!!",ex );
                throw new BuildException("Found compilation errors.");
            }
        }
    }
    
    /**
     * Create custom classloader ( Ant classloader) with 
     * parentFirst = false
     */
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
