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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import java.lang.reflect.Method;

/**
 * Ant task wrapper which invokes the JBI Generation task
 * 
 * @author Sreenivasan Genipudi
 * @author Kirill Sorokin
 */
public class CliGenerateCatalogTask extends Task {

    private String mSourceDirectory = null;
    private String mBuildDirectory = null;
    private String mProjectClassPath = null;
    private AntClassLoader m_myClassLoader = null;
    private Reference m_ref = null;

    public CliGenerateCatalogTask() {
        // Does nothing
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

    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }

    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }

    @Override
    public void execute() throws BuildException {
        try {
            m_myClassLoader = new AntClassLoader();
            initClassLoader();
            Class antTaskClass = Class.forName(
                    "org.netbeans.modules.bpel.project.anttasks.cli." + // NOI18N
                    "CliGenerateCatalogDelegate", true, m_myClassLoader); // NOI18N
            Thread.currentThread().setContextClassLoader(m_myClassLoader);
            Object genJBIInstObj = antTaskClass.newInstance();
            
            Method driver = antTaskClass.getMethod(
                    "setBuildDirectory", new Class[] {String.class}); // NOI18N
            driver.invoke(genJBIInstObj, new Object[] {this.mBuildDirectory});
            
            driver = antTaskClass.getMethod(
                    "setSourceDirectory", new Class[] {String.class}); // NOI18N
            driver.invoke(genJBIInstObj, new Object[] {this.mSourceDirectory});
            
            driver = antTaskClass.getMethod("execute", (Class[]) null); // NOI18N
            driver.invoke(genJBIInstObj, (Object[]) null);
        } catch (Exception e) {
            throw new BuildException("Errors found.", e); // NOI18N
        }
    }

    private void initClassLoader() {
        Path path = new Path(getProject());
        path.setRefid(m_ref);
        
        final Path parentPath = new Path(getProject());
        final ClassLoader loader = this.getClass().getClassLoader();
        if (loader instanceof AntClassLoader) {
            final AntClassLoader antLoader = (AntClassLoader) loader;
            
            parentPath.setPath(antLoader.getClasspath());
            antLoader.setParent(null);
            parentPath.add(path);
            path = parentPath;
        }
        
        m_myClassLoader.setClassPath(path);
        m_myClassLoader.setParent(null);
        m_myClassLoader.setParentFirst(false);
    }
}
