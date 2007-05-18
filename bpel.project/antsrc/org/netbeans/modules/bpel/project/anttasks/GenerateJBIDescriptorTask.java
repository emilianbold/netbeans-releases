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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.lang.reflect.Method;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Ant task wrapper which invokes the JBI Generation task
 * @author Sreenivasan Genipudi
 */
public class GenerateJBIDescriptorTask extends org.apache.tools.ant.Task{
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
     * Custom classloader used to invoke the JBI Generation task
     */
    private AntClassLoader m_myClassLoader = null;
    /**
     * Classpath Reference
     */
    private Reference m_ref = null;
    
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(GenerateJBIDescriptorTask.class.getName());    
    /**
     * Constructor
     */
    public GenerateJBIDescriptorTask() {
    }
    
    /**
     * Set the classpath reference
     * @param ref Classpath Reference
     */
    public void setClasspathRef(Reference ref) {
        this.m_ref = ref;
    }
    
    /**
     * Set the build directory
     * @param buildDir build directory
     */
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
     * Get the source directory
     * @return String value of the source directory
     */
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    /**
     * Set the project classpath
     * @param projectClassPath Project classpath
     */
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
        
    
    /**
     * Invoke the task that generates the JBI.xml
     */
    public void execute() throws BuildException { 
        try {
            m_myClassLoader = new AntClassLoader(); 
            initClassLoader();
             Class antTaskClass =  Class.forName("org.netbeans.modules.bpel.project.anttasks.GenerateJBIDescriptor", true,m_myClassLoader );
             Thread.currentThread().setContextClassLoader(m_myClassLoader);
             // m_myClassLoader.forceLoadClass("org.netbeans.modules.bpel.project.anttasks.BPELCatalogModel");
         //   m_myClassLoader.forceLoadClass("org.openide.util.Lookup");
             Object genJBIInstObj = antTaskClass.newInstance();

             Method driver = antTaskClass.getMethod("setBuildDirectory",
                            new Class[] { java.lang.String.class });
             Object[] param = new Object[] {
                            this.mBuildDirectory
                        };
            driver.invoke(genJBIInstObj,
                        param);
                        
            driver = antTaskClass.getMethod("setSourceDirectory",
                           new Class[] { java.lang.String.class });
            param = new Object[] {
                           this.mSourceDirectory
                       };
            driver.invoke(genJBIInstObj,
                       param);   
                       
            driver = antTaskClass.getMethod("setProjectClassPath",
                           new Class[] { java.lang.String.class });
            param = new Object[] {
                           this.mProjectClassPath
                       };
            driver.invoke(genJBIInstObj,
                       param);                          
                     
            driver = antTaskClass.getMethod("execute",
                            null);
            driver.invoke(genJBIInstObj, null);

        }catch (Exception ex) {
            logger.log(Level.FINE, "Compilation Errors found", ex);
            throw new BuildException("Compilation Errors found");
        }
    }
    
    /**
     * Set the custom classloader and make the parent first reference 
     * by the classloader to False
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
