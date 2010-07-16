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

package org.netbeans.modules.iep.project.anttasks.cli;


import java.lang.reflect.Method;


import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;



/**
 *
 * @author blu
 */
public class GenerateAsaArtifacts extends org.netbeans.modules.iep.project.anttasks.GenerateAsaArtifacts {
    
    private AntClassLoader m_myClassLoader = null;
    
        
    @Override
    public void execute() throws BuildException { 
        try {
            m_myClassLoader = new AntClassLoader(); 
            initClassLoader();
            Class antTaskClass =  Class.forName("org.netbeans.modules.iep.project.anttasks.cli.CliGenerateAsaArtifacts", true, m_myClassLoader);
            Thread.currentThread().setContextClassLoader(m_myClassLoader);
            Object genJBIInstObj = antTaskClass.newInstance();

            Method driver = antTaskClass.getMethod("setBuildDirectoryLocation", new Class[] {String.class});
            Object[] param = new Object[] {this.getBuildDirectoryLocation()};
            driver.invoke(genJBIInstObj, param);
                       
            driver = antTaskClass.getMethod("setSrcDirectoryLocation", new Class[] {String.class});
            param = new Object[] {this.getSrcDirectoryLocation()};
            driver.invoke(genJBIInstObj, param);   
                      
            driver = antTaskClass.getMethod("setJbiDescriptorFileLocation", new Class[] {String.class});
            param = new Object[] {this.getJbiDescriptorFileLocation()};
            driver.invoke(genJBIInstObj, param);                          
            
            driver = antTaskClass.getMethod("setValidate", new Class[] {String.class});
            param = new Object[] {this.getValidate()};
            driver.invoke(genJBIInstObj, param);
            
            driver = antTaskClass.getMethod("setAllowBuildWithError", new Class[] {String.class});
            param = new Object[] {this.getAllowBuildWithError()};
            driver.invoke(genJBIInstObj, param);                          
            
            driver = antTaskClass.getMethod("execute", (Class[]) null);
            driver.invoke(genJBIInstObj, (Object[]) null);
        }
        catch (Exception e) {
            throw new BuildException("Errors found: " + e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
        GenerateAsaArtifacts tsk = new GenerateAsaArtifacts();
        tsk.setJbiDescriptorFileLocation("c:/temp/portMap.xml");
        tsk.setSrcDirectoryLocation("c:/temp");
        tsk.execute();
    }    
    
    
    private void initClassLoader() {
        Path path = new Path(getProject());
        path.setRefid(getClasspathRef());
        
        Path parentPath = new Path(getProject());
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof AntClassLoader) {
            parentPath.setPath(((AntClassLoader)cl).getClasspath());
            ((AntClassLoader)cl).setParent(null);
            parentPath.add(path);
            path = parentPath;
        }        
        
        m_myClassLoader = new AntClassLoader(); 
        m_myClassLoader.setClassPath(path);
        m_myClassLoader.setParent(null);
        m_myClassLoader.setParentFirst(false);
    }
}
