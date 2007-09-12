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
package org.netbeans.modules.xslt.project.anttasks;

import java.lang.reflect.InvocationTargetException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Ant task wrapper which invokes the JBI Generation task
 * @author Vitaly Bychkov
 * @author Sreenivasan Genipudi
 */
public class GenerateJBIDescriptorTask extends org.apache.tools.ant.Task {

    /**
     * Source directory
     */
    private String mSourceDirectory = null;
    /**
     * Build directory
     */
    private String mBuildDirectory = null;    
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
     * Get the build directory
     * @return String value of the build directory
     */
    public String getBuildDirectory() {
        return mBuildDirectory;
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
    /**
     * Invoke validate BPEL Model
     */
    public void execute() throws BuildException { 
        try {
            m_myClassLoader = new AntClassLoader(); 
            initClassLoader(); 
            Class antTaskClass =  Class.forName("org.netbeans.modules.xslt.project.anttasks.GenerateJBIDescriptor", true,m_myClassLoader );
            Thread.currentThread().setContextClassLoader(m_myClassLoader);
            
            Object generateJBIDecsriptorObj = antTaskClass.newInstance();
            
            
            Method driver = antTaskClass.getMethod("setBuildDirectory",
                           new Class[] { java.lang.String.class });
            Object[] param = new Object[] {
                           this.mBuildDirectory
                       };
            driver.invoke(generateJBIDecsriptorObj,
                       param);
                       
            driver = antTaskClass.getMethod("setSourceDirectory",
                          new Class[] { java.lang.String.class });
            param = new Object[] {
                          this.mSourceDirectory
                      };
            driver.invoke(generateJBIDecsriptorObj,
                      param);   
                      
            driver = antTaskClass.getMethod("setProjectClassPath",
                          new Class[] { java.lang.String.class });
            param = new Object[] {
                          this.mProjectClassPath
                      };
            driver.invoke(generateJBIDecsriptorObj,
                      param);   
                      
            driver = antTaskClass.getMethod("execute",
                            null);
            driver.invoke(generateJBIDecsriptorObj, null);                    
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
         //   ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex) {
            ex.getCause().printStackTrace();
            throw new RuntimeException(ex);
        } catch(InstantiationException ex) {
            throw new RuntimeException(ex);
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
    

    public static void main(String[] args) {
        GenerateJBIDescriptorTask ddt = new GenerateJBIDescriptorTask();

    }
}
