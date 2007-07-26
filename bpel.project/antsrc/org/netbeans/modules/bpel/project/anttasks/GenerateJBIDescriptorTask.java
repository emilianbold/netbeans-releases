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

/**
 * Ant task wrapper which invokes the JBI Generation task
 * @author Sreenivasan Genipudi
 */
public class GenerateJBIDescriptorTask extends org.apache.tools.ant.Task{
    private String mSourceDirectory = null;
    private String mBuildDirectory = null;    
    private String mProjectClassPath= null;
    private AntClassLoader m_myClassLoader = null;
    private Reference m_ref = null;

    public GenerateJBIDescriptorTask() {}
    
    public void setClasspathRef(Reference ref) {
        this.m_ref = ref;
    }
    
    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }

    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }
    
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
        
    public void execute() throws BuildException { 
        try {
            m_myClassLoader = new AntClassLoader(); 
            initClassLoader();
            Class antTaskClass =  Class.forName("org.netbeans.modules.bpel.project.anttasks.GenerateJBIDescriptor", true,m_myClassLoader );
            Thread.currentThread().setContextClassLoader(m_myClassLoader);
            Object genJBIInstObj = antTaskClass.newInstance();

            Method driver = antTaskClass.getMethod("setBuildDirectory", new Class[] { java.lang.String.class });
            Object[] param = new Object[] { this.mBuildDirectory};
            driver.invoke(genJBIInstObj, param);
                       
            driver = antTaskClass.getMethod("setSourceDirectory", new Class[] { java.lang.String.class });
            param = new Object[] { this.mSourceDirectory };
            driver.invoke(genJBIInstObj, param);   
                      
            driver = antTaskClass.getMethod("setProjectClassPath", new Class[] { java.lang.String.class });
            param = new Object[] { this.mProjectClassPath};
            driver.invoke(genJBIInstObj, param);                          
                    
            driver = antTaskClass.getMethod("execute", null);
            driver.invoke(genJBIInstObj, null);
        }
        catch (Exception e) {
            throw new BuildException("Errors found");
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
